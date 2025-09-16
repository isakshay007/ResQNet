package com.resqnet.service;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.dto.NotificationDTO;
import com.resqnet.model.Contribution;
import com.resqnet.model.ResourceRequest;
import com.resqnet.model.User;
import com.resqnet.producer.NotificationProducer;
import com.resqnet.repository.ContributionRepository;
import com.resqnet.repository.ResourceRequestRepository;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final ResourceRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final NotificationProducer notificationProducer;

    public ContributionService(ContributionRepository contributionRepository,
                               ResourceRequestRepository requestRepository,
                               UserRepository userRepository,
                               NotificationProducer notificationProducer) {
        this.contributionRepository = contributionRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.notificationProducer = notificationProducer;
    }

    // ---------------- CREATE ----------------
    @Transactional
    public ContributionDTO createContribution(ContributionDTO dto, String responderEmail) {
        ResourceRequest request = requestRepository.findByIdForUpdate(dto.getRequestId())
                .orElseThrow(() -> new EntityNotFoundException("Resource Request not found"));

        User responder = userRepository.findByEmail(responderEmail)
                .orElseThrow(() -> new EntityNotFoundException("Responder not found"));

        if (responder.getRole() != User.Role.RESPONDER) {
            throw new AccessDeniedException("Only RESPONDER users can contribute to requests");
        }

        int pending = request.getRequestedQuantity() - request.getFulfilledQuantity();
        if (dto.getContributedQuantity() > pending) {
            throw new IllegalArgumentException("Contribution exceeds pending quantity. Pending: " + pending);
        }

        Contribution contribution = new Contribution();
        contribution.setContributedQuantity(dto.getContributedQuantity());
        contribution.setRequest(request);
        contribution.setResponder(responder);
        contribution.setCategory(dto.getCategory());

        // Save responder’s permanent location if not already set
        if (responder.getLatitude() == null || responder.getLongitude() == null) {
            responder.setLatitude(dto.getLatitude());
            responder.setLongitude(dto.getLongitude());
            userRepository.save(responder);
        }

        // Always record location on contribution
        contribution.setLatitude(dto.getLatitude());
        contribution.setLongitude(dto.getLongitude());

        // Update request fulfillment
        request.addFulfilledQuantity(dto.getContributedQuantity());
        if (request.getFulfilledQuantity() >= request.getRequestedQuantity()) {
            request.setStatus(ResourceRequest.Status.FULFILLED);
        }

        requestRepository.save(request);
        Contribution saved = contributionRepository.save(contribution);

        sendContributionNotifications(saved);

        return mapToDTO(saved);
    }

    // ---------------- READ: ROLE-FILTERED ----------------
    public List<ContributionDTO> getAllContributionsForUser(String userEmail) {
        User loggedInUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        switch (loggedInUser.getRole()) {
            case ADMIN:
                return getAllContributions(); // delegate to raw method
            case RESPONDER:
                return contributionRepository.findByResponder_Email(userEmail).stream()
                        .map(this::mapToDTO).toList();
            case REPORTER:
                return contributionRepository.findAll().stream()
                        .filter(c -> c.getRequest().getReporter().getEmail().equalsIgnoreCase(userEmail))
                        .map(this::mapToDTO).toList();
            default:
                throw new AccessDeniedException("Unsupported role");
        }
    }

    public List<ContributionDTO> getByRequestWithSecurity(Long requestId, String userEmail) {
        ResourceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        User loggedInUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (loggedInUser.getRole() == User.Role.REPORTER &&
            !request.getReporter().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Not authorized to view this request’s contributions");
        }

        return contributionRepository.findByRequestId(requestId).stream()
                .map(this::mapToDTO).toList();
    }

    public List<ContributionDTO> getByResponderWithSecurity(String responderEmail, String loggedInEmail) {
        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (loggedInUser.getRole() == User.Role.RESPONDER &&
            !responderEmail.equalsIgnoreCase(loggedInEmail)) {
            throw new AccessDeniedException("Responders can only view their own contributions");
        }

        if (loggedInUser.getRole() == User.Role.REPORTER) {
            throw new AccessDeniedException("Reporters cannot view responder-specific contributions");
        }

        return contributionRepository.findByResponder_Email(responderEmail).stream()
                .map(this::mapToDTO).toList();
    }

    // ---------------- READ: RAW ADMIN ----------------
    public List<ContributionDTO> getAllContributions() {
        return contributionRepository.findAll().stream()
                .map(this::mapToDTO).toList();
    }

    public List<ContributionDTO> getByRequest(Long requestId) {
        return contributionRepository.findByRequestId(requestId).stream()
                .map(this::mapToDTO).toList();
    }

    public List<ContributionDTO> getByResponder(String responderEmail) {
        return contributionRepository.findByResponder_Email(responderEmail).stream()
                .map(this::mapToDTO).toList();
    }

    // ---------------- DELETE ----------------
    @Transactional
    public void deleteContribution(Long id) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contribution not found"));

        ResourceRequest request = contribution.getRequest();
        request.setFulfilledQuantity(request.getFulfilledQuantity() - contribution.getContributedQuantity());
        request.updateStatus();
        requestRepository.save(request);

        contributionRepository.delete(contribution);

        sendContributionDeletionNotifications(contribution);
    }

    @Transactional
    public void deleteContributionWithSecurity(Long id, String userEmail) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contribution not found"));

        User loggedInUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (loggedInUser.getRole() == User.Role.RESPONDER &&
            !contribution.getResponder().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Responders can only delete their own contributions");
        }

        deleteContribution(id); // reuse logic
    }

    // ---------------- NOTIFICATIONS ----------------
    private void sendContributionNotifications(Contribution contribution) {
        String reporterEmail = contribution.getRequest().getReporter().getEmail();
        String responderEmail = contribution.getResponder().getEmail();

        int contributed = contribution.getContributedQuantity();
        int fulfilled = contribution.getRequest().getFulfilledQuantity();
        int requested = contribution.getRequest().getRequestedQuantity();

        // Reporter
        NotificationDTO reporterNotif = new NotificationDTO();
        reporterNotif.setRecipientEmail(reporterEmail);
        if (fulfilled >= requested) {
            reporterNotif.setMessage("Your request #" + contribution.getRequest().getId()
                    + " has been fully fulfilled! 🎉 (+" + contributed + " units of " + contribution.getCategory() + ")");
            reporterNotif.setType("CONTRIBUTION_FULFILLED");
        } else {
            reporterNotif.setMessage("Your request #" + contribution.getRequest().getId()
                    + " received " + contributed + " units of " + contribution.getCategory() +
                    ". Pending: " + (requested - fulfilled));
            reporterNotif.setType("CONTRIBUTION_PARTIAL");
        }
        reporterNotif.setDeletable(true);
        notificationProducer.sendNotification(reporterNotif);

        // Responder
        NotificationDTO responderNotif = new NotificationDTO();
        responderNotif.setRecipientEmail(responderEmail);
        responderNotif.setMessage("You contributed " + contributed + " units of " +
                contribution.getCategory() + " to request #" + contribution.getRequest().getId());
        responderNotif.setType("CONTRIBUTION_CONFIRMATION");
        responderNotif.setDeletable(true);
        notificationProducer.sendNotification(responderNotif);

        // Admin
        NotificationDTO adminNotif = new NotificationDTO();
        adminNotif.setMessage("New contribution: " + contributed + " units of " +
                contribution.getCategory() + " by " + responderEmail +
                " to request #" + contribution.getRequest().getId() +
                " (Lat:" + contribution.getLatitude() + ", Lng:" + contribution.getLongitude() + ")");
        adminNotif.setType("ADMIN_LOG");
        adminNotif.setDeletable(false);
        adminNotif.setAdminBroadcast(true);
        notificationProducer.sendNotification(adminNotif);
    }

    private void sendContributionDeletionNotifications(Contribution contribution) {
        String reporterEmail = contribution.getRequest().getReporter().getEmail();
        String responderEmail = contribution.getResponder().getEmail();

        // Reporter
        NotificationDTO reporterNotif = new NotificationDTO();
        reporterNotif.setRecipientEmail(reporterEmail);
        reporterNotif.setMessage("A contribution of " + contribution.getContributedQuantity() +
                " units of " + contribution.getCategory() + " to your request #" +
                contribution.getRequest().getId() + " was removed. Pending: " +
                (contribution.getRequest().getRequestedQuantity() - contribution.getRequest().getFulfilledQuantity()));
        reporterNotif.setType("CONTRIBUTION_DELETE");
        reporterNotif.setDeletable(true);
        notificationProducer.sendNotification(reporterNotif);

        // Responder
        NotificationDTO responderNotif = new NotificationDTO();
        responderNotif.setRecipientEmail(responderEmail);
        responderNotif.setMessage("Your contribution of " + contribution.getContributedQuantity() +
                " units of " + contribution.getCategory() + " to request #" +
                contribution.getRequest().getId() + " was deleted.");
        responderNotif.setType("CONTRIBUTION_DELETE_CONFIRMATION");
        responderNotif.setDeletable(true);
        notificationProducer.sendNotification(responderNotif);

        // Admin
        NotificationDTO adminNotif = new NotificationDTO();
        adminNotif.setMessage("Contribution of " + contribution.getContributedQuantity() +
                " units of " + contribution.getCategory() + " by " + responderEmail +
                " to request #" + contribution.getRequest().getId() + " was deleted.");
        adminNotif.setType("ADMIN_LOG");
        adminNotif.setDeletable(false);
        adminNotif.setAdminBroadcast(true);
        notificationProducer.sendNotification(adminNotif);
    }

    // ---------------- MAPPER ----------------
    private ContributionDTO mapToDTO(Contribution c) {
        ContributionDTO dto = new ContributionDTO();
        dto.setId(c.getId());
        dto.setContributedQuantity(c.getContributedQuantity());
        dto.setRequestId(c.getRequest().getId());
        dto.setCategory(c.getCategory());
        dto.setResponderEmail(c.getResponder().getEmail());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setLatitude(c.getLatitude());
        dto.setLongitude(c.getLongitude());
        return dto;
    }
}

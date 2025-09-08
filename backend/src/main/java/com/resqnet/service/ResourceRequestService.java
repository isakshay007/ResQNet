package com.resqnet.service;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.model.Disaster;
import com.resqnet.model.ResourceRequest;
import com.resqnet.model.User;
import com.resqnet.producer.NotificationProducer;
import com.resqnet.repository.DisasterRepository;
import com.resqnet.repository.ResourceRequestRepository;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceRequestService {

    private final ResourceRequestRepository resourceRequestRepository;
    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;
    private final NotificationProducer notificationProducer;

    public ResourceRequestService(ResourceRequestRepository resourceRequestRepository,
                                  DisasterRepository disasterRepository,
                                  UserRepository userRepository,
                                  NotificationProducer notificationProducer) {
        this.resourceRequestRepository = resourceRequestRepository;
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
        this.notificationProducer = notificationProducer;
    }

    // --- CREATE request (Reporter only) ---
    @Transactional
    public ResourceRequestDTO createRequest(ResourceRequestDTO dto, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new EntityNotFoundException("Reporter not found"));

        if (reporter.getRole() != User.Role.REPORTER) {
            throw new AccessDeniedException("Only REPORTER users can create resource requests");
        }

        ResourceRequest request = new ResourceRequest();
        request.setCategory(dto.getCategory());
        request.setRequestedQuantity(dto.getRequestedQuantity());
        request.setFulfilledQuantity(0); // fresh requests always start unfulfilled

        if (dto.getDisasterId() != null) {
            Disaster disaster = disasterRepository.findById(dto.getDisasterId())
                    .orElseThrow(() -> new EntityNotFoundException("Disaster not found"));
            request.setDisaster(disaster);
        }

        request.setReporter(reporter);

        ResourceRequest saved = resourceRequestRepository.save(request);
        ResourceRequestDTO response = mapToDTO(saved);

        sendCreateRequestNotifications(saved);

        return response;
    }

    // --- READ all (Admin/Responder only) ---
    public List<ResourceRequestDTO> getAllRequests() {
        return resourceRequestRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- READ one (Admin/Responder) ---
    public ResourceRequestDTO getRequestById(Long id) {
        return resourceRequestRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));
    }

    // --- Reporter: READ all their own requests ---
    public List<ResourceRequestDTO> getRequestsForReporter(String reporterEmail) {
        return resourceRequestRepository.findByReporter_Email(reporterEmail).stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- Reporter: READ one of their own requests ---
    public ResourceRequestDTO getRequestByIdForReporter(Long id, String reporterEmail) {
        ResourceRequest req = resourceRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        if (!req.getReporter().getEmail().equalsIgnoreCase(reporterEmail)) {
            throw new AccessDeniedException("You are not authorized to view this request");
        }

        return mapToDTO(req);
    }

    // --- UPDATE request (Admin only) ---
    @Transactional
    public ResourceRequestDTO updateRequest(ResourceRequestDTO dto) {
        ResourceRequest request = resourceRequestRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        request.setCategory(dto.getCategory());
        request.setRequestedQuantity(dto.getRequestedQuantity());
        request.setFulfilledQuantity(dto.getFulfilledQuantity());

        // auto-update status from fulfilled vs requested
        request.updateStatus();

        if (dto.getDisasterId() != null) {
            Disaster disaster = disasterRepository.findById(dto.getDisasterId())
                    .orElseThrow(() -> new EntityNotFoundException("Disaster not found"));
            request.setDisaster(disaster);
        }

        ResourceRequest updated = resourceRequestRepository.save(request);

        sendUpdateRequestNotifications(updated);

        return mapToDTO(updated);
    }

    // --- DELETE request (Admin only) ---
    @Transactional
    public void deleteRequest(Long id) {
        ResourceRequest req = resourceRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource Request not found"));

        resourceRequestRepository.deleteById(id);

        sendDeleteRequestNotifications(req);
    }

    // --- Mapping helper ---
    private ResourceRequestDTO mapToDTO(ResourceRequest r) {
        ResourceRequestDTO dto = new ResourceRequestDTO();
        dto.setId(r.getId());
        dto.setCategory(r.getCategory());
        dto.setRequestedQuantity(r.getRequestedQuantity());
        dto.setFulfilledQuantity(r.getFulfilledQuantity());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setDisasterId(r.getDisaster() != null ? r.getDisaster().getId() : null);
        dto.setReporterEmail(r.getReporter() != null ? r.getReporter().getEmail() : null);
        return dto;
    }

    // --- Notification helpers ---
    private void sendCreateRequestNotifications(ResourceRequest request) {
        String reporterEmail = request.getReporter().getEmail();

        NotificationDTO reporterNotif = new NotificationDTO();
        reporterNotif.setRecipientEmail(reporterEmail);
        reporterNotif.setMessage("Your request for " + request.getRequestedQuantity() +
                " units of " + request.getCategory() + " has been created.");
        reporterNotif.setType("REQUEST");
        reporterNotif.setDeletable(true);
        notificationProducer.sendNotification(reporterNotif);

        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.RESPONDER)
                .forEach(responder -> {
                    NotificationDTO responderNotif = new NotificationDTO();
                    responderNotif.setRecipientEmail(responder.getEmail());
                    responderNotif.setMessage("📢 New request for " + request.getCategory() +
                            " (" + request.getRequestedQuantity() + " units).");
                    responderNotif.setType("REQUEST_ALERT");
                    responderNotif.setDeletable(true);
                    notificationProducer.sendNotification(responderNotif);
                });

        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .forEach(admin -> {
                    NotificationDTO adminNotif = new NotificationDTO();
                    adminNotif.setRecipientEmail(admin.getEmail());
                    adminNotif.setMessage("📌 New request created by " + reporterEmail +
                            " for " + request.getCategory() + " (" + request.getRequestedQuantity() + ")");
                    adminNotif.setType("ADMIN_LOG");
                    adminNotif.setDeletable(false);
                    notificationProducer.sendNotification(adminNotif);
                });
    }

    private void sendUpdateRequestNotifications(ResourceRequest request) {
        String reporterEmail = request.getReporter().getEmail();

        NotificationDTO reporterNotif = new NotificationDTO();
        reporterNotif.setRecipientEmail(reporterEmail);
        reporterNotif.setMessage("✏️ Your request #" + request.getId() + " has been updated by Admin.");
        reporterNotif.setType("REQUEST_UPDATE");
        reporterNotif.setDeletable(true);
        notificationProducer.sendNotification(reporterNotif);

        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .forEach(admin -> {
                    NotificationDTO adminNotif = new NotificationDTO();
                    adminNotif.setRecipientEmail(admin.getEmail());
                    adminNotif.setMessage("Request #" + request.getId() + " was updated.");
                    adminNotif.setType("ADMIN_LOG");
                    adminNotif.setDeletable(false);
                    notificationProducer.sendNotification(adminNotif);
                });
    }

    private void sendDeleteRequestNotifications(ResourceRequest request) {
        String reporterEmail = request.getReporter().getEmail();

        NotificationDTO reporterNotif = new NotificationDTO();
        reporterNotif.setRecipientEmail(reporterEmail);
        reporterNotif.setMessage("Your request #" + request.getId() + " was deleted by Admin.");
        reporterNotif.setType("REQUEST_DELETE");
        reporterNotif.setDeletable(true);
        notificationProducer.sendNotification(reporterNotif);

        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .forEach(admin -> {
                    NotificationDTO adminNotif = new NotificationDTO();
                    adminNotif.setRecipientEmail(admin.getEmail());
                    adminNotif.setMessage("Request #" + request.getId() + " was deleted.");
                    adminNotif.setType("ADMIN_LOG");
                    adminNotif.setDeletable(false);
                    notificationProducer.sendNotification(adminNotif);
                });
    }
}

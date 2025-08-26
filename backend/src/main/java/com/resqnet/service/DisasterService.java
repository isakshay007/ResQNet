package com.resqnet.service;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.dto.NotificationDTO;
import com.resqnet.model.Disaster;
import com.resqnet.model.User;
import com.resqnet.producer.NotificationProducer;
import com.resqnet.repository.DisasterRepository;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisasterService {

    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;
    private final NotificationProducer notificationProducer;

    public DisasterService(DisasterRepository disasterRepository,
                           UserRepository userRepository,
                           NotificationProducer notificationProducer) {
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
        this.notificationProducer = notificationProducer;
    }

    // --- CREATE disaster (only REPORTER) ---
    @Transactional
    public DisasterDTO createDisaster(DisasterDTO dto, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new EntityNotFoundException("Reporter not found"));

        if (reporter.getRole() != User.Role.REPORTER) {
            throw new AccessDeniedException("Only REPORTER users can create disaster reports");
        }

        Disaster disaster = new Disaster();
        disaster.setType(dto.getType());
        disaster.setSeverity(dto.getSeverity());
        disaster.setDescription(dto.getDescription());
        disaster.setLatitude(dto.getLatitude());
        disaster.setLongitude(dto.getLongitude());
        disaster.setReporter(reporter);

        Disaster saved = disasterRepository.save(disaster);

        // Reporter confirmation
        NotificationDTO reporterNotif = new NotificationDTO();
        reporterNotif.setRecipientEmail(reporter.getEmail());
        reporterNotif.setMessage("Your disaster report (" + saved.getType() + ") has been submitted.");
        reporterNotif.setType("DISASTER_CONFIRMATION");
        reporterNotif.setDeletable(true);
        notificationProducer.sendNotification(reporterNotif);

        // Notify all admins
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .forEach(admin -> {
                    NotificationDTO adminNotif = new NotificationDTO();
                    adminNotif.setRecipientEmail(admin.getEmail());
                    adminNotif.setMessage(" New disaster reported: " + saved.getType() +
                            " (" + saved.getSeverity() + ") by " + reporter.getEmail());
                    adminNotif.setType("ADMIN_LOG");
                    adminNotif.setDeletable(false);
                    notificationProducer.sendNotification(adminNotif);
                });

        // Notify all responders
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.RESPONDER)
                .forEach(responder -> {
                    NotificationDTO responderNotif = new NotificationDTO();
                    responderNotif.setRecipientEmail(responder.getEmail());
                    responderNotif.setMessage(" New disaster reported: " + saved.getType() +
                            " (" + saved.getSeverity() + ")");
                    responderNotif.setType("DISASTER_ALERT");
                    responderNotif.setDeletable(true);
                    notificationProducer.sendNotification(responderNotif);
                });

        return mapToDTO(saved);
    }

    // --- READ: Get all disasters ---
    public List<DisasterDTO> getAllDisasters() {
        return disasterRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- READ: Get single disaster ---
    public DisasterDTO getDisasterById(Long id) {
        return disasterRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Disaster not found"));
    }

    // --- UPDATE disaster (Admin only) ---
    @Transactional
    public DisasterDTO updateDisaster(DisasterDTO dto) {
        Disaster disaster = disasterRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Disaster not found"));

        disaster.setType(dto.getType());
        disaster.setSeverity(dto.getSeverity());
        disaster.setDescription(dto.getDescription());
        disaster.setLatitude(dto.getLatitude());
        disaster.setLongitude(dto.getLongitude());

        Disaster updated = disasterRepository.save(disaster);

        // Reporter notification
        if (updated.getReporter() != null) {
            NotificationDTO notif = new NotificationDTO();
            notif.setRecipientEmail(updated.getReporter().getEmail());
            notif.setMessage("Your disaster report (" + updated.getType() + ") was updated by Admin.");
            notif.setType("DISASTER_UPDATE");
            notif.setDeletable(true);
            notificationProducer.sendNotification(notif);
        }

        // Admin log
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .forEach(admin -> {
                    NotificationDTO adminNotif = new NotificationDTO();
                    adminNotif.setRecipientEmail(admin.getEmail());
                    adminNotif.setMessage("Disaster #" + updated.getId() + " (" + updated.getType() + ") was updated.");
                    adminNotif.setType("ADMIN_LOG");
                    adminNotif.setDeletable(false);
                    notificationProducer.sendNotification(adminNotif);
                });

        return mapToDTO(updated);
    }

    // --- DELETE disaster (Admin only) ---
    @Transactional
    public void deleteDisaster(Long id) {
        Disaster disaster = disasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Disaster not found"));

        // Reporter notification
        if (disaster.getReporter() != null) {
            NotificationDTO notif = new NotificationDTO();
            notif.setRecipientEmail(disaster.getReporter().getEmail());
            notif.setMessage(" Your disaster report (" + disaster.getType() + ") was deleted by Admin.");
            notif.setType("DISASTER_DELETE");
            notif.setDeletable(true);
            notificationProducer.sendNotification(notif);
        }

        // Admin log
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .forEach(admin -> {
                    NotificationDTO adminNotif = new NotificationDTO();
                    adminNotif.setRecipientEmail(admin.getEmail());
                    adminNotif.setMessage("Disaster #" + disaster.getId() + " (" + disaster.getType() + ") was deleted.");
                    adminNotif.setType("ADMIN_LOG");
                    adminNotif.setDeletable(false);
                    notificationProducer.sendNotification(adminNotif);
                });

        disasterRepository.deleteById(id);
    }

    // --- Mapping helper ---
    private DisasterDTO mapToDTO(Disaster disaster) {
        DisasterDTO dto = new DisasterDTO();
        dto.setId(disaster.getId());
        dto.setType(disaster.getType());
        dto.setSeverity(disaster.getSeverity());
        dto.setDescription(disaster.getDescription());
        dto.setLatitude(disaster.getLatitude());
        dto.setLongitude(disaster.getLongitude());
        dto.setReporterEmail(
                disaster.getReporter() != null ? disaster.getReporter().getEmail() : null
        );
        return dto;
    }
}

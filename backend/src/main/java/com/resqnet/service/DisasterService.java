package com.resqnet.service;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.model.Disaster;
import com.resqnet.model.User;
import com.resqnet.repository.DisasterRepository;
import com.resqnet.repository.UserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class DisasterService {

    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "disaster-reports"; // Kafka topic name

    public DisasterService(DisasterRepository disasterRepository,
                           UserRepository userRepository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // --- CREATE disaster (uses authenticated reporter email instead of trusting DTO) ---
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

        // Publish to Kafka after saving
        String message = String.format(
                "New Disaster Reported: [id=%d, type=%s, severity=%s, reporter=%s]",
                saved.getId(),
                saved.getType(),
                saved.getSeverity(),
                reporter.getEmail()
        );
        kafkaTemplate.send(TOPIC, message);

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

    // --- UPDATE disaster (Admin use only) ---
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
        return mapToDTO(updated);
    }

    // --- DELETE disaster (Admin use only) ---
    @Transactional
    public void deleteDisaster(Long id) {
        if (!disasterRepository.existsById(id)) {
            throw new EntityNotFoundException("Disaster not found");
        }
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

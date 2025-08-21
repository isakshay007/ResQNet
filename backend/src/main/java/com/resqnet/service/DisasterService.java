package com.resqnet.service;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.model.Disaster;
import com.resqnet.model.User;
import com.resqnet.repository.DisasterRepository;
import com.resqnet.repository.UserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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

    // Save disaster from DTO
    public DisasterDTO createDisaster(DisasterDTO dto) {
        Disaster disaster = new Disaster();
        disaster.setType(dto.getType());
        disaster.setSeverity(dto.getSeverity());
        disaster.setDescription(dto.getDescription());
        disaster.setLatitude(dto.getLatitude());
        disaster.setLongitude(dto.getLongitude());

        if (dto.getReporterEmail() != null) {
            User reporter = userRepository.findByEmail(dto.getReporterEmail())
                    .orElseThrow(() -> new RuntimeException("Reporter not found"));
            disaster.setReporter(reporter);
        }

        Disaster saved = disasterRepository.save(disaster);

        //  Publish to Kafka after saving
        String message = String.format(
                "New Disaster Reported: [id=%d, type=%s, severity=%s, reporter=%s]",
                saved.getId(),
                saved.getType(),
                saved.getSeverity(),
                saved.getReporter() != null ? saved.getReporter().getEmail() : "anonymous"
        );
        kafkaTemplate.send(TOPIC, message);

        return mapToDTO(saved);
    }

    // Get all disasters as DTOs
    public List<DisasterDTO> getAllDisasters() {
        return disasterRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Get single disaster as DTO
    public DisasterDTO getDisasterById(Long id) {
        return disasterRepository.findById(id)
                .map(this::mapToDTO)
                .orElse(null);
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

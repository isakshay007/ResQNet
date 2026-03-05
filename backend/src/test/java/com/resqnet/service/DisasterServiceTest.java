package com.resqnet.service;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.model.Disaster;
import com.resqnet.model.User;
import com.resqnet.producer.NotificationProducer;
import com.resqnet.repository.DisasterRepository;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisasterServiceTest {

    @Mock private DisasterRepository disasterRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationProducer notificationProducer;

    @InjectMocks private DisasterService disasterService;

    private User reporter;
    private Disaster sampleDisaster;

    @BeforeEach
    void setUp() {
        reporter = new User();
        reporter.setId(1L);
        reporter.setName("Jane");
        reporter.setEmail("jane@example.com");
        reporter.setRole(User.Role.REPORTER);
        reporter.setLatitude(28.6139);
        reporter.setLongitude(77.2090);

        sampleDisaster = new Disaster();
        sampleDisaster.setId(1L);
        sampleDisaster.setType("Flood");
        sampleDisaster.setSeverity("HIGH");
        sampleDisaster.setDescription("Heavy flooding in the area");
        sampleDisaster.setLatitude(28.6139);
        sampleDisaster.setLongitude(77.2090);
        sampleDisaster.setReporter(reporter);
    }

    @Test
    void createDisaster_withValidReporter_savesAndSendsKafkaNotifications() {
        DisasterDTO dto = new DisasterDTO();
        dto.setType("Flood");
        dto.setSeverity("HIGH");
        dto.setDescription("Heavy flooding in the area");
        dto.setLatitude(28.6139);
        dto.setLongitude(77.2090);

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(reporter));
        when(disasterRepository.save(any(Disaster.class))).thenReturn(sampleDisaster);
        when(userRepository.findAll()).thenReturn(List.of(reporter));

        DisasterDTO result = disasterService.createDisaster(dto, "jane@example.com");

        assertNotNull(result);
        assertEquals("Flood", result.getType());
        assertEquals("HIGH", result.getSeverity());
        assertEquals("jane@example.com", result.getReporterEmail());
        verify(disasterRepository).save(any(Disaster.class));
        verify(notificationProducer, atLeast(2)).sendNotification(any());
    }

    @Test
    void createDisaster_notifiesResponders() {
        User responder = new User();
        responder.setId(2L);
        responder.setEmail("responder@example.com");
        responder.setRole(User.Role.RESPONDER);

        DisasterDTO dto = new DisasterDTO();
        dto.setType("Earthquake");
        dto.setSeverity("MEDIUM");
        dto.setDescription("Earthquake detected");
        dto.setLatitude(19.0760);
        dto.setLongitude(72.8777);

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(reporter));
        when(disasterRepository.save(any(Disaster.class))).thenReturn(sampleDisaster);
        when(userRepository.findAll()).thenReturn(List.of(reporter, responder));

        disasterService.createDisaster(dto, "jane@example.com");

        // reporter confirmation + admin broadcast + 1 responder alert = 3
        verify(notificationProducer, times(3)).sendNotification(any());
    }

    @Test
    void getAllDisasters_returnsMappedList() {
        when(disasterRepository.findAll()).thenReturn(List.of(sampleDisaster));

        List<DisasterDTO> result = disasterService.getAllDisasters();

        assertEquals(1, result.size());
        assertEquals("Flood", result.get(0).getType());
        assertEquals("reported", result.get(0).getStatus());
    }

    @Test
    void getDisasterById_whenNotFound_throwsEntityNotFound() {
        when(disasterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> disasterService.getDisasterById(99L));
    }

    @Test
    void deleteDisaster_withExistingId_deletesAndNotifies() {
        when(disasterRepository.findById(1L)).thenReturn(Optional.of(sampleDisaster));

        disasterService.deleteDisaster(1L);

        verify(disasterRepository).deleteById(1L);
        verify(notificationProducer, atLeast(2)).sendNotification(any());
    }
}

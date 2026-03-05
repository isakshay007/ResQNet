package com.resqnet.service;

import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.model.Disaster;
import com.resqnet.model.ResourceRequest;
import com.resqnet.model.User;
import com.resqnet.producer.NotificationProducer;
import com.resqnet.repository.DisasterRepository;
import com.resqnet.repository.ResourceRequestRepository;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceRequestServiceTest {

    @Mock private ResourceRequestRepository resourceRequestRepository;
    @Mock private DisasterRepository disasterRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationProducer notificationProducer;

    @InjectMocks private ResourceRequestService service;

    private User reporter;
    private Disaster disaster;
    private ResourceRequest sampleRequest;

    @BeforeEach
    void setUp() {
        reporter = new User();
        reporter.setId(1L);
        reporter.setEmail("reporter@example.com");
        reporter.setName("Reporter");
        reporter.setRole(User.Role.REPORTER);

        disaster = new Disaster();
        disaster.setId(1L);
        disaster.setType("Flood");

        sampleRequest = new ResourceRequest();
        sampleRequest.setId(1L);
        sampleRequest.setCategory("water");
        sampleRequest.setRequestedQuantity(100);
        sampleRequest.setFulfilledQuantity(0);
        sampleRequest.setDisaster(disaster);
        sampleRequest.setReporter(reporter);
    }

    @Test
    void createRequest_withValidData_savesAndReturnsDTO() {
        ResourceRequestDTO dto = new ResourceRequestDTO();
        dto.setCategory("water");
        dto.setRequestedQuantity(100);
        dto.setDisasterId(1L);

        when(userRepository.findByEmail("reporter@example.com")).thenReturn(Optional.of(reporter));
        when(disasterRepository.findById(1L)).thenReturn(Optional.of(disaster));
        when(resourceRequestRepository.save(any(ResourceRequest.class))).thenReturn(sampleRequest);
        when(userRepository.findAll()).thenReturn(List.of(reporter));

        ResourceRequestDTO result = service.createRequest(dto, "reporter@example.com");

        assertNotNull(result);
        assertEquals("water", result.getCategory());
        assertEquals(100, result.getRequestedQuantity());
        assertEquals(0, result.getFulfilledQuantity());
        verify(resourceRequestRepository).save(any(ResourceRequest.class));
    }

    @Test
    void createRequest_whenReporterNotFound_throwsEntityNotFound() {
        ResourceRequestDTO dto = new ResourceRequestDTO();
        dto.setCategory("food");
        dto.setRequestedQuantity(50);

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.createRequest(dto, "unknown@example.com"));
    }

    @Test
    void getRequestsForReporter_returnsFilteredList() {
        when(resourceRequestRepository.findByReporter_Email("reporter@example.com"))
                .thenReturn(List.of(sampleRequest));

        List<ResourceRequestDTO> result = service.getRequestsForReporter("reporter@example.com");

        assertEquals(1, result.size());
        assertEquals("reporter@example.com", result.get(0).getReporterEmail());
    }

    @Test
    void getAllRequests_returnsMappedList() {
        when(resourceRequestRepository.findAll()).thenReturn(List.of(sampleRequest));

        List<ResourceRequestDTO> result = service.getAllRequests();

        assertEquals(1, result.size());
        assertEquals("water", result.get(0).getCategory());
    }

    @Test
    void deleteRequest_withExistingId_deletesAndNotifies() {
        when(resourceRequestRepository.findById(1L)).thenReturn(Optional.of(sampleRequest));

        service.deleteRequest(1L);

        verify(resourceRequestRepository).deleteById(1L);
        verify(notificationProducer, atLeast(2)).sendNotification(any());
    }
}

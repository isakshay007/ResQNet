package com.resqnet.service;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.model.Contribution;
import com.resqnet.model.ResourceRequest;
import com.resqnet.model.User;
import com.resqnet.producer.NotificationProducer;
import com.resqnet.repository.ContributionRepository;
import com.resqnet.repository.ResourceRequestRepository;
import com.resqnet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContributionServiceTest {

    @Mock private ContributionRepository contributionRepository;
    @Mock private ResourceRequestRepository requestRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationProducer notificationProducer;

    @InjectMocks private ContributionService service;

    private User responder;
    private User reporter;
    private ResourceRequest request;

    @BeforeEach
    void setUp() {
        reporter = new User();
        reporter.setId(1L);
        reporter.setEmail("reporter@example.com");
        reporter.setName("Reporter");
        reporter.setRole(User.Role.REPORTER);

        responder = new User();
        responder.setId(2L);
        responder.setEmail("responder@example.com");
        responder.setName("Responder");
        responder.setRole(User.Role.RESPONDER);

        request = new ResourceRequest();
        request.setId(1L);
        request.setCategory("water");
        request.setRequestedQuantity(100);
        request.setFulfilledQuantity(0);
        request.setReporter(reporter);
    }

    private Contribution buildSavedContribution(int quantity) {
        Contribution saved = new Contribution();
        saved.setId(1L);
        saved.setContributedQuantity(quantity);
        saved.setRequest(request);
        saved.setResponder(responder);
        saved.setCategory("water");
        saved.setLatitude(28.0);
        saved.setLongitude(77.0);
        return saved;
    }

    private ContributionDTO buildDTO(int quantity) {
        ContributionDTO dto = new ContributionDTO();
        dto.setRequestId(1L);
        dto.setContributedQuantity(quantity);
        dto.setCategory("water");
        dto.setLatitude(28.0);
        dto.setLongitude(77.0);
        return dto;
    }

    @Test
    void createContribution_updatesFulfilledQuantityCorrectly() {
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(userRepository.findByEmail("responder@example.com")).thenReturn(Optional.of(responder));
        when(requestRepository.save(any(ResourceRequest.class))).thenReturn(request);
        when(contributionRepository.save(any(Contribution.class))).thenReturn(buildSavedContribution(30));

        ContributionDTO result = service.createContribution(buildDTO(30), "responder@example.com");

        assertEquals(30, result.getContributedQuantity());
        assertEquals(30, request.getFulfilledQuantity());
        verify(requestRepository).save(request);
    }

    @Test
    void createContribution_transitionsStatusFromPendingToPartial() {
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(userRepository.findByEmail("responder@example.com")).thenReturn(Optional.of(responder));
        when(requestRepository.save(any(ResourceRequest.class))).thenReturn(request);
        when(contributionRepository.save(any(Contribution.class))).thenReturn(buildSavedContribution(30));

        assertEquals(ResourceRequest.Status.PENDING, request.getStatus());

        service.createContribution(buildDTO(30), "responder@example.com");

        assertEquals(ResourceRequest.Status.PARTIAL, request.getStatus());
    }

    @Test
    void createContribution_transitionsStatusFromPartialToFulfilledWhenFullyMet() {
        request.setFulfilledQuantity(70);
        assertEquals(ResourceRequest.Status.PARTIAL, request.getStatus());

        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(userRepository.findByEmail("responder@example.com")).thenReturn(Optional.of(responder));
        when(requestRepository.save(any(ResourceRequest.class))).thenReturn(request);
        when(contributionRepository.save(any(Contribution.class))).thenReturn(buildSavedContribution(30));

        service.createContribution(buildDTO(30), "responder@example.com");

        assertEquals(ResourceRequest.Status.FULFILLED, request.getStatus());
        assertEquals(100, request.getFulfilledQuantity());
    }

    @Test
    void deleteContribution_decreasesFulfilledQuantityAndTransitionsStatusBack() {
        request.setFulfilledQuantity(30);
        assertEquals(ResourceRequest.Status.PARTIAL, request.getStatus());

        Contribution contribution = new Contribution();
        contribution.setId(1L);
        contribution.setContributedQuantity(30);
        contribution.setRequest(request);
        contribution.setResponder(responder);
        contribution.setCategory("water");

        when(contributionRepository.findById(1L)).thenReturn(Optional.of(contribution));

        service.deleteContribution(1L);

        assertEquals(0, request.getFulfilledQuantity());
        assertEquals(ResourceRequest.Status.PENDING, request.getStatus());
        verify(requestRepository).save(request);
        verify(contributionRepository).delete(contribution);
    }

    @Test
    void createContribution_exceedingPendingQuantity_throwsIllegalArgument() {
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(userRepository.findByEmail("responder@example.com")).thenReturn(Optional.of(responder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createContribution(buildDTO(150), "responder@example.com"));
        assertTrue(ex.getMessage().contains("Contribution exceeds pending quantity"));
    }
}

package com.resqnet.service;

import com.resqnet.model.Disaster;
import com.resqnet.model.User;
import com.resqnet.repository.DisasterRepository;
import com.resqnet.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DisasterService {

    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;

    public DisasterService(DisasterRepository disasterRepository, UserRepository userRepository) {
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
    }

    public Disaster createDisaster(Disaster disaster) {
        // Ensure reporter is fully loaded from DB
        if (disaster.getReporter() != null && disaster.getReporter().getId() != null) {
            User reporter = userRepository.findById(disaster.getReporter().getId())
                    .orElseThrow(() -> new RuntimeException("Reporter not found with id " + disaster.getReporter().getId()));
            disaster.setReporter(reporter);
        }
        return disasterRepository.save(disaster);
    }

    public List<Disaster> getAllDisasters() {
        return disasterRepository.findAll();
    }

    public Disaster getDisasterById(Long id) {
        return disasterRepository.findById(id).orElse(null);
    }
}

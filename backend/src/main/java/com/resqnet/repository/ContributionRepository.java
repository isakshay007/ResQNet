package com.resqnet.repository;

import com.resqnet.model.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findByRequestId(Long requestId);

    List<Contribution> findByResponderId(Long responderId);

    List<Contribution> findByResponder_Email(String email);
}

package com.resqnet.repository;

import com.resqnet.model.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    // Fetch all contributions for a specific request
    List<Contribution> findByRequestId(Long requestId);

    // Fetch all contributions by responder (ID or email)
    List<Contribution> findByResponderId(Long responderId);
    List<Contribution> findByResponder_Email(String email);

    // Fetch all contributions with recorded locations
    @Query("SELECT c FROM Contribution c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<Contribution> findAllWithLocation();

    // Fetch contributions near a specific area (basic bounding box query)
    @Query("SELECT c FROM Contribution c " +
           "WHERE c.latitude BETWEEN :minLat AND :maxLat " +
           "AND c.longitude BETWEEN :minLon AND :maxLon")
    List<Contribution> findContributionsNear(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon
    );
}

package com.resqnet.repository;

import com.resqnet.model.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findByRequestId(Long requestId);

    List<Contribution> findByResponderId(Long responderId);

    List<Contribution> findByResponder_Email(String email);

    // 🔹 Optional: Fetch contributions with latitude/longitude (for responders nearby a request)
    @Query("SELECT c FROM Contribution c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<Contribution> findAllWithLocation();

    // 🔹 Optional: Find contributions near a specific disaster/request (basic bounding box)
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

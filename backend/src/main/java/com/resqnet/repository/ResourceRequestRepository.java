package com.resqnet.repository;

import com.resqnet.model.ResourceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {

    // Fetch all requests created by a specific reporter (by email)
    List<ResourceRequest> findByReporter_Email(String reporterEmail);

    // (Optional) If you want to fetch by reporter's user ID instead
    List<ResourceRequest> findByReporter_Id(Long reporterId);

    //  For concurrency safety: lock row during contribution update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ResourceRequest r WHERE r.id = :id")
    Optional<ResourceRequest> findByIdForUpdate(@Param("id") Long id);
}

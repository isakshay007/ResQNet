package com.resqnet.repository;

import com.resqnet.model.ResourceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {
}

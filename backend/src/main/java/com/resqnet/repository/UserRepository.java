package com.resqnet.repository;

import com.resqnet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    //  Fetch only users who have a saved map location (pin)
    @Query("SELECT u FROM User u WHERE u.latitude IS NOT NULL AND u.longitude IS NOT NULL")
    List<User> findAllWithLocation();
}

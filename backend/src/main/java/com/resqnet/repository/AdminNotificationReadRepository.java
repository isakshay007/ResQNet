package com.resqnet.repository;

import com.resqnet.model.AdminNotificationRead;
import com.resqnet.model.Notification;
import com.resqnet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminNotificationReadRepository extends JpaRepository<AdminNotificationRead, Long> {

    Optional<AdminNotificationRead> findByNotificationAndAdmin(Notification notification, User admin);

    @Query("SELECT anr.notification.id FROM AdminNotificationRead anr WHERE anr.admin.id = :adminId")
    List<Long> findReadNotificationIdsByAdminId(@Param("adminId") Long adminId);

    void deleteByNotification(Notification notification);
}

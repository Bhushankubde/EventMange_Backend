package com.event.EventManage.repository;

import com.event.EventManage.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByOrderByCreatedAtDesc();
}

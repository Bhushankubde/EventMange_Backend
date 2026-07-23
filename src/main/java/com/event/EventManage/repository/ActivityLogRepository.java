package com.event.EventManage.repository;

import com.event.EventManage.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {
    List<ActivityLog> findByOrderByTimestampDesc();
}

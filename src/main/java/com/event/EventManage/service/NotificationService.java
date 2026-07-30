package com.event.EventManage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.event.EventManage.handler.NotificationWebSocketHandler;
import com.event.EventManage.model.Notification;
import com.event.EventManage.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public Notification sendNotification(String message, String type) {
        log.info("Creating notification: {} (type: {})", message, type);
        Notification notification = Notification.builder()
                .message(message)
                .type(type)
                .readStatus(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Serialize and broadcast via WebSockets
        try {
            String jsonMessage = objectMapper.writeValueAsString(saved);
            webSocketHandler.broadcast(jsonMessage);
        } catch (Exception e) {
            log.error("Failed to serialize notification for broadcast", e);
        }

        return saved;
    }

    public void broadcastInventoryUpdate(String itemId, int availableQuantity) {
        log.info("Broadcasting inventory update: item={}, available={}", itemId, availableQuantity);
        try {
            java.util.Map<String, Object> payload = java.util.Map.of(
                "type", "INVENTORY_UPDATE",
                "itemId", itemId,
                "availableQuantity", availableQuantity
            );
            String jsonMessage = objectMapper.writeValueAsString(payload);
            webSocketHandler.broadcast(jsonMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast inventory update", e);
        }
    }
}

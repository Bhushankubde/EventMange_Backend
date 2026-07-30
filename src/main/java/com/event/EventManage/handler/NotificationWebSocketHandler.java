package com.event.EventManage.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        sessions.add(session);
        // Send a handshake confirmation message to the client
        try {
            session.sendMessage(new TextMessage("{\"type\":\"HANDSHAKE\",\"message\":\"Connected to notifications WebSocket\"}"));
        } catch (IOException e) {
            log.error("Failed to send handshake message to session {}", session.getId(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} (status: {})", session.getId(), status);
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
        sessions.remove(session);
        if (session.isOpen()) {
            session.close();
        }
    }

    public void broadcast(String message) {
        log.info("Broadcasting notification message to {} active sessions", sessions.size());
        List<WebSocketSession> deadSessions = new CopyOnWriteArrayList<>();
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("Error sending message to session {}", session.getId(), e);
                    deadSessions.add(session);
                }
            } else {
                deadSessions.add(session);
            }
        }
        sessions.removeAll(deadSessions);
    }
}

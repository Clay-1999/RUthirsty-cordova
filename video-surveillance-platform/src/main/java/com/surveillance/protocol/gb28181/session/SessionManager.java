package com.surveillance.protocol.gb28181.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session Manager for GB28181 Devices
 */
@Slf4j
@Component
public class SessionManager {

    private final Map<String, DeviceSession> deviceSessions = new ConcurrentHashMap<>();

    /**
     * Get device session
     */
    public DeviceSession getSession(String deviceId) {
        return deviceSessions.get(deviceId);
    }

    /**
     * Create or update device session
     */
    public DeviceSession createOrUpdateSession(String deviceId) {
        return deviceSessions.computeIfAbsent(deviceId, DeviceSession::new);
    }

    /**
     * Remove device session
     */
    public void removeSession(String deviceId) {
        DeviceSession session = deviceSessions.remove(deviceId);
        if (session != null) {
            log.info("Removed session for device: {}", deviceId);
        }
    }

    /**
     * Get all device sessions
     */
    public Map<String, DeviceSession> getAllSessions() {
        return deviceSessions;
    }

    /**
     * Check if device is online
     */
    public boolean isOnline(String deviceId) {
        DeviceSession session = deviceSessions.get(deviceId);
        return session != null && session.isOnline();
    }

    /**
     * Update device keepalive
     */
    public void updateKeepalive(String deviceId) {
        DeviceSession session = deviceSessions.get(deviceId);
        if (session != null) {
            session.updateKeepalive();
            log.debug("Updated keepalive for device: {}", deviceId);
        }
    }

    /**
     * Get online device count
     */
    public long getOnlineCount() {
        return deviceSessions.values().stream()
                .filter(DeviceSession::isOnline)
                .count();
    }
}

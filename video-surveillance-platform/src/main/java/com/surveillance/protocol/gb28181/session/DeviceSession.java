package com.surveillance.protocol.gb28181.session;

import com.surveillance.dao.entity.Device;
import lombok.Data;

import javax.sip.Dialog;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Device Session Manager
 * Manages SIP sessions for GB28181 devices
 */
@Data
public class DeviceSession {

    private String deviceId;
    private String ip;
    private Integer port;
    private String transport;
    private String hostAddress;
    private LocalDateTime registerTime;
    private LocalDateTime lastKeepaliveTime;
    private Integer expires;
    private String callId;
    private String fromTag;
    private String toTag;
    private Dialog dialog;
    private Map<String, String> channelMap = new ConcurrentHashMap<>();

    public DeviceSession(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isOnline() {
        if (lastKeepaliveTime == null) {
            return false;
        }
        LocalDateTime timeout = lastKeepaliveTime.plusSeconds(expires != null ? expires : 180);
        return LocalDateTime.now().isBefore(timeout);
    }

    public void updateKeepalive() {
        this.lastKeepaliveTime = LocalDateTime.now();
    }

    public Device toDevice() {
        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setIpAddress(ip);
        device.setPort(port);
        device.setTransport(transport);
        device.setHostAddress(hostAddress);
        device.setRegisterTime(registerTime);
        device.setLastKeepaliveTime(lastKeepaliveTime);
        device.setExpires(expires);
        device.setStatus(isOnline() ? "ONLINE" : "OFFLINE");
        return device;
    }
}

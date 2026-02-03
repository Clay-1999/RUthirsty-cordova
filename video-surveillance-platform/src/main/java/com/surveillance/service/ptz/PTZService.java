package com.surveillance.service.ptz;

import com.surveillance.dao.entity.Device;
import com.surveillance.exception.BusinessException;
import com.surveillance.protocol.gb28181.command.SipCommandSender;
import com.surveillance.protocol.gb28181.session.SessionManager;
import com.surveillance.service.device.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * PTZ Control Service
 */
@Slf4j
@Service
public class PTZService {

    private final DeviceService deviceService;
    private final SessionManager sessionManager;
    private final SipCommandSender sipCommandSender;

    public PTZService(DeviceService deviceService,
                      SessionManager sessionManager,
                      SipCommandSender sipCommandSender) {
        this.deviceService = deviceService;
        this.sessionManager = sessionManager;
        this.sipCommandSender = sipCommandSender;
    }

    /**
     * PTZ control
     */
    public void control(String deviceId, String channelId, String command, Integer speed) {
        // Check device
        Device device = deviceService.getDeviceByDeviceId(deviceId);
        if (device == null) {
            throw new BusinessException("Device not found");
        }

        if (!sessionManager.isOnline(deviceId)) {
            throw new BusinessException("Device is offline");
        }

        // Convert command to code
        int commandCode = convertCommand(command);
        if (speed == null) {
            speed = 50;
        }

        // Send PTZ control command
        boolean success = sipCommandSender.sendPtzControl(deviceId, channelId, commandCode, speed);
        if (!success) {
            throw new BusinessException("Failed to send PTZ control command");
        }

        log.info("PTZ control sent: device={}, channel={}, command={}, speed={}",
                deviceId, channelId, command, speed);
    }

    /**
     * Set preset
     */
    public void setPreset(String deviceId, String channelId, Integer presetId, String presetName) {
        // Check device
        Device device = deviceService.getDeviceByDeviceId(deviceId);
        if (device == null) {
            throw new BusinessException("Device not found");
        }

        if (!sessionManager.isOnline(deviceId)) {
            throw new BusinessException("Device is offline");
        }

        // TODO: Implement preset setting
        log.info("Set preset: device={}, channel={}, presetId={}, presetName={}",
                deviceId, channelId, presetId, presetName);
    }

    /**
     * Call preset
     */
    public void callPreset(String deviceId, String channelId, Integer presetId) {
        // Check device
        Device device = deviceService.getDeviceByDeviceId(deviceId);
        if (device == null) {
            throw new BusinessException("Device not found");
        }

        if (!sessionManager.isOnline(deviceId)) {
            throw new BusinessException("Device is offline");
        }

        // TODO: Implement preset calling
        log.info("Call preset: device={}, channel={}, presetId={}",
                deviceId, channelId, presetId);
    }

    /**
     * Delete preset
     */
    public void deletePreset(String deviceId, String channelId, Integer presetId) {
        // Check device
        Device device = deviceService.getDeviceByDeviceId(deviceId);
        if (device == null) {
            throw new BusinessException("Device not found");
        }

        if (!sessionManager.isOnline(deviceId)) {
            throw new BusinessException("Device is offline");
        }

        // TODO: Implement preset deletion
        log.info("Delete preset: device={}, channel={}, presetId={}",
                deviceId, channelId, presetId);
    }

    private int convertCommand(String command) {
        switch (command.toUpperCase()) {
            case "UP":
                return 1;
            case "DOWN":
                return 2;
            case "LEFT":
                return 3;
            case "RIGHT":
                return 4;
            case "ZOOM_IN":
                return 5;
            case "ZOOM_OUT":
                return 6;
            case "STOP":
                return 0;
            default:
                throw new BusinessException("Unknown PTZ command: " + command);
        }
    }
}

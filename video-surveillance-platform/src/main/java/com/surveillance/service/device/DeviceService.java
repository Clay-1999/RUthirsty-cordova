package com.surveillance.service.device;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.surveillance.dao.entity.Device;
import com.surveillance.dao.entity.DeviceChannel;
import com.surveillance.dao.mapper.DeviceChannelMapper;
import com.surveillance.dao.mapper.DeviceMapper;
import com.surveillance.exception.BusinessException;
import com.surveillance.protocol.gb28181.command.SipCommandSender;
import com.surveillance.protocol.gb28181.session.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Device Service
 */
@Slf4j
@Service
public class DeviceService {

    private final DeviceMapper deviceMapper;
    private final DeviceChannelMapper deviceChannelMapper;
    private final SessionManager sessionManager;
    private final SipCommandSender sipCommandSender;

    public DeviceService(DeviceMapper deviceMapper,
                         DeviceChannelMapper deviceChannelMapper,
                         SessionManager sessionManager,
                         SipCommandSender sipCommandSender) {
        this.deviceMapper = deviceMapper;
        this.deviceChannelMapper = deviceChannelMapper;
        this.sessionManager = sessionManager;
        this.sipCommandSender = sipCommandSender;
    }

    /**
     * Get device list
     */
    public List<Device> getDeviceList() {
        List<Device> devices = deviceMapper.selectList(null);

        // Update status from session
        devices.forEach(device -> {
            boolean online = sessionManager.isOnline(device.getDeviceId());
            device.setStatus(online ? "ONLINE" : "OFFLINE");
        });

        return devices;
    }

    /**
     * Get device by ID
     */
    public Device getDeviceById(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("Device not found");
        }

        // Update status from session
        boolean online = sessionManager.isOnline(device.getDeviceId());
        device.setStatus(online ? "ONLINE" : "OFFLINE");

        return device;
    }

    /**
     * Get device by device ID
     */
    public Device getDeviceByDeviceId(String deviceId) {
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId);
        Device device = deviceMapper.selectOne(queryWrapper);

        if (device != null) {
            boolean online = sessionManager.isOnline(deviceId);
            device.setStatus(online ? "ONLINE" : "OFFLINE");
        }

        return device;
    }

    /**
     * Add device
     */
    @Transactional
    public void addDevice(Device device) {
        // Check if device already exists
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", device.getDeviceId());
        Device existing = deviceMapper.selectOne(queryWrapper);

        if (existing != null) {
            throw new BusinessException("Device already exists");
        }

        device.setStatus("OFFLINE");
        deviceMapper.insert(device);
        log.info("Device added: {}", device.getDeviceId());
    }

    /**
     * Update device
     */
    @Transactional
    public void updateDevice(Device device) {
        if (device.getId() == null) {
            throw new BusinessException("Device ID is required");
        }

        Device existing = deviceMapper.selectById(device.getId());
        if (existing == null) {
            throw new BusinessException("Device not found");
        }

        deviceMapper.updateById(device);
        log.info("Device updated: {}", device.getDeviceId());
    }

    /**
     * Delete device
     */
    @Transactional
    public void deleteDevice(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("Device not found");
        }

        // Delete device channels
        QueryWrapper<DeviceChannel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", device.getDeviceId());
        deviceChannelMapper.delete(queryWrapper);

        // Delete device
        deviceMapper.deleteById(id);

        // Remove session
        sessionManager.removeSession(device.getDeviceId());

        log.info("Device deleted: {}", device.getDeviceId());
    }

    /**
     * Get device channels
     */
    public List<DeviceChannel> getDeviceChannels(String deviceId) {
        QueryWrapper<DeviceChannel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId);
        return deviceChannelMapper.selectList(queryWrapper);
    }

    /**
     * Sync device channels (send catalog query)
     */
    public void syncDeviceChannels(String deviceId) {
        Device device = getDeviceByDeviceId(deviceId);
        if (device == null) {
            throw new BusinessException("Device not found");
        }

        if (!"GB28181".equals(device.getDeviceType())) {
            throw new BusinessException("Only GB28181 devices support channel sync");
        }

        if (!sessionManager.isOnline(deviceId)) {
            throw new BusinessException("Device is offline");
        }

        boolean success = sipCommandSender.sendCatalogQuery(deviceId);
        if (!success) {
            throw new BusinessException("Failed to send catalog query");
        }

        log.info("Catalog query sent to device: {}", deviceId);
    }

    /**
     * Get device status statistics
     */
    public Map<String, Object> getDeviceStatus() {
        long total = deviceMapper.selectCount(null);
        long online = sessionManager.getOnlineCount();
        long offline = total - online;

        return Map.of(
                "total", total,
                "online", online,
                "offline", offline
        );
    }
}

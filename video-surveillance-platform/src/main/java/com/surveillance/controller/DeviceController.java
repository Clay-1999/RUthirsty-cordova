package com.surveillance.controller;

import com.surveillance.dao.entity.Device;
import com.surveillance.dao.entity.DeviceChannel;
import com.surveillance.dto.Result;
import com.surveillance.service.device.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Device Management Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * Get device list
     */
    @GetMapping("/list")
    public Result<List<Device>> getDeviceList() {
        List<Device> devices = deviceService.getDeviceList();
        return Result.success(devices);
    }

    /**
     * Get device detail
     */
    @GetMapping("/detail/{id}")
    public Result<Device> getDeviceDetail(@PathVariable Long id) {
        Device device = deviceService.getDeviceById(id);
        return Result.success(device);
    }

    /**
     * Add device
     */
    @PostMapping("/add")
    public Result<Void> addDevice(@RequestBody Device device) {
        deviceService.addDevice(device);
        return Result.success();
    }

    /**
     * Update device
     */
    @PutMapping("/update")
    public Result<Void> updateDevice(@RequestBody Device device) {
        deviceService.updateDevice(device);
        return Result.success();
    }

    /**
     * Delete device
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return Result.success();
    }

    /**
     * Get device channels
     */
    @GetMapping("/channels/{deviceId}")
    public Result<List<DeviceChannel>> getDeviceChannels(@PathVariable String deviceId) {
        List<DeviceChannel> channels = deviceService.getDeviceChannels(deviceId);
        return Result.success(channels);
    }

    /**
     * Sync device channels
     */
    @PostMapping("/sync/{deviceId}")
    public Result<Void> syncDeviceChannels(@PathVariable String deviceId) {
        deviceService.syncDeviceChannels(deviceId);
        return Result.success();
    }

    /**
     * Get device status statistics
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getDeviceStatus() {
        Map<String, Object> status = deviceService.getDeviceStatus();
        return Result.success(status);
    }
}

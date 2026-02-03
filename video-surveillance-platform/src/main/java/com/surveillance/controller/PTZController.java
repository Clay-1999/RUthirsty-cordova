package com.surveillance.controller;

import com.surveillance.dto.Result;
import com.surveillance.service.ptz.PTZService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * PTZ Control Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ptz")
public class PTZController {

    private final PTZService ptzService;

    public PTZController(PTZService ptzService) {
        this.ptzService = ptzService;
    }

    /**
     * PTZ control
     */
    @PostMapping("/control")
    public Result<Void> control(@RequestBody PTZControlRequest request) {
        ptzService.control(
                request.getDeviceId(),
                request.getChannelId(),
                request.getCommand(),
                request.getSpeed()
        );
        return Result.success();
    }

    /**
     * Set preset
     */
    @PostMapping("/preset/set")
    public Result<Void> setPreset(@RequestBody PresetRequest request) {
        ptzService.setPreset(
                request.getDeviceId(),
                request.getChannelId(),
                request.getPresetId(),
                request.getPresetName()
        );
        return Result.success();
    }

    /**
     * Call preset
     */
    @PostMapping("/preset/call")
    public Result<Void> callPreset(@RequestBody PresetRequest request) {
        ptzService.callPreset(
                request.getDeviceId(),
                request.getChannelId(),
                request.getPresetId()
        );
        return Result.success();
    }

    /**
     * Delete preset
     */
    @PostMapping("/preset/delete")
    public Result<Void> deletePreset(@RequestBody PresetRequest request) {
        ptzService.deletePreset(
                request.getDeviceId(),
                request.getChannelId(),
                request.getPresetId()
        );
        return Result.success();
    }

    @Data
    public static class PTZControlRequest {
        private String deviceId;
        private String channelId;
        private String command;
        private Integer speed;
    }

    @Data
    public static class PresetRequest {
        private String deviceId;
        private String channelId;
        private Integer presetId;
        private String presetName;
    }
}

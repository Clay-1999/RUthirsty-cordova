package com.surveillance.controller;

import com.surveillance.dao.entity.StreamSession;
import com.surveillance.dto.Result;
import com.surveillance.service.stream.StreamService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Stream Management Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/stream")
public class StreamController {

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    /**
     * Start live stream
     */
    @PostMapping("/play")
    public Result<Map<String, String>> play(@RequestBody PlayRequest request) {
        Map<String, String> result = streamService.startLiveStream(
                request.getDeviceId(),
                request.getChannelId()
        );
        return Result.success(result);
    }

    /**
     * Stop stream
     */
    @PostMapping("/stop")
    public Result<Void> stop(@RequestBody StopRequest request) {
        streamService.stopStream(request.getSessionId());
        return Result.success();
    }

    /**
     * Get stream info
     */
    @GetMapping("/info/{sessionId}")
    public Result<StreamSession> getInfo(@PathVariable String sessionId) {
        StreamSession session = streamService.getStreamInfo(sessionId);
        return Result.success(session);
    }

    /**
     * Get stream list
     */
    @GetMapping("/list")
    public Result<List<StreamSession>> getList() {
        List<StreamSession> sessions = streamService.getStreamList();
        return Result.success(sessions);
    }

    @Data
    public static class PlayRequest {
        private String deviceId;
        private String channelId;
        private String streamType;
    }

    @Data
    public static class StopRequest {
        private String sessionId;
    }
}

package com.surveillance.service.stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.surveillance.dao.entity.Device;
import com.surveillance.dao.entity.StreamSession;
import com.surveillance.dao.mapper.StreamSessionMapper;
import com.surveillance.exception.BusinessException;
import com.surveillance.media.zlmediakit.StreamManager;
import com.surveillance.protocol.gb28181.command.SipCommandSender;
import com.surveillance.protocol.gb28181.session.SessionManager;
import com.surveillance.service.device.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stream Service
 */
@Slf4j
@Service
public class StreamService {

    private final DeviceService deviceService;
    private final StreamManager streamManager;
    private final StreamSessionMapper streamSessionMapper;
    private final SessionManager sessionManager;
    private final SipCommandSender sipCommandSender;

    public StreamService(DeviceService deviceService,
                         StreamManager streamManager,
                         StreamSessionMapper streamSessionMapper,
                         SessionManager sessionManager,
                         SipCommandSender sipCommandSender) {
        this.deviceService = deviceService;
        this.streamManager = streamManager;
        this.streamSessionMapper = streamSessionMapper;
        this.sessionManager = sessionManager;
        this.sipCommandSender = sipCommandSender;
    }

    /**
     * Start live stream
     */
    public Map<String, String> startLiveStream(String deviceId, String channelId) {
        // Check device
        Device device = deviceService.getDeviceByDeviceId(deviceId);
        if (device == null) {
            throw new BusinessException("Device not found");
        }

        if (!sessionManager.isOnline(deviceId)) {
            throw new BusinessException("Device is offline");
        }

        // Generate SSRC
        String ssrc = generateSSRC();

        // Create RTP session
        StreamSession streamSession = streamManager.createRtpSession(deviceId, channelId, ssrc, 0);
        if (streamSession == null) {
            throw new BusinessException("Failed to create stream session");
        }

        // Send INVITE to device
        boolean success = sipCommandSender.sendInvite(deviceId, channelId, ssrc, streamSession.getRtpPort());
        if (!success) {
            streamManager.closeSession(streamSession.getSessionId());
            throw new BusinessException("Failed to send INVITE to device");
        }

        log.info("Live stream started: device={}, channel={}, session={}",
                deviceId, channelId, streamSession.getSessionId());

        // Return playback URLs
        Map<String, String> result = new HashMap<>();
        result.put("sessionId", streamSession.getSessionId());
        result.put("flvUrl", streamSession.getFlvUrl());
        result.put("hlsUrl", streamSession.getHlsUrl());
        result.put("rtmpUrl", streamSession.getRtmpUrl());
        result.put("rtspUrl", streamSession.getRtspUrl());
        result.put("webrtcUrl", streamSession.getWebrtcUrl());

        return result;
    }

    /**
     * Stop stream
     */
    public void stopStream(String sessionId) {
        StreamSession session = streamSessionMapper.selectOne(
                new QueryWrapper<StreamSession>().eq("session_id", sessionId)
        );

        if (session == null) {
            throw new BusinessException("Stream session not found");
        }

        boolean success = streamManager.closeSession(sessionId);
        if (!success) {
            log.warn("Failed to close stream session: {}", sessionId);
        }

        log.info("Stream stopped: session={}", sessionId);
    }

    /**
     * Get stream info
     */
    public StreamSession getStreamInfo(String sessionId) {
        StreamSession session = streamSessionMapper.selectOne(
                new QueryWrapper<StreamSession>().eq("session_id", sessionId)
        );

        if (session == null) {
            throw new BusinessException("Stream session not found");
        }

        return session;
    }

    /**
     * Get stream session list
     */
    public List<StreamSession> getStreamList() {
        QueryWrapper<StreamSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE");
        return streamSessionMapper.selectList(queryWrapper);
    }

    private String generateSSRC() {
        return String.format("%010d", (int) (Math.random() * 10000000000L));
    }
}

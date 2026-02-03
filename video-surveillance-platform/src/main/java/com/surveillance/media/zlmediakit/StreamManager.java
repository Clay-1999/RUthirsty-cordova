package com.surveillance.media.zlmediakit;

import cn.hutool.core.util.IdUtil;
import com.surveillance.dao.entity.StreamSession;
import com.surveillance.dao.mapper.StreamSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Stream Manager Service
 */
@Slf4j
@Service
public class StreamManager {

    private final ZLMediaKitClient zlmClient;
    private final ZLMediaKitConfig zlmConfig;
    private final StreamSessionMapper streamSessionMapper;

    public StreamManager(ZLMediaKitClient zlmClient,
                         ZLMediaKitConfig zlmConfig,
                         StreamSessionMapper streamSessionMapper) {
        this.zlmClient = zlmClient;
        this.zlmConfig = zlmConfig;
        this.streamSessionMapper = streamSessionMapper;
    }

    /**
     * Create RTP receive session for GB28181 device
     *
     * @param deviceId  Device ID
     * @param channelId Channel ID
     * @param ssrc      SSRC
     * @param tcpMode   TCP mode: 0-UDP, 1-TCP passive, 2-TCP active
     * @return Stream session
     */
    public StreamSession createRtpSession(String deviceId, String channelId, String ssrc, Integer tcpMode) {
        String sessionId = IdUtil.simpleUUID();
        String streamId = generateStreamId(deviceId, channelId);

        // Open RTP server port
        Integer rtpPort = zlmClient.openRtpServer(streamId, 0, tcpMode, ssrc);
        if (rtpPort == null) {
            log.error("Failed to open RTP server for device: {}, channel: {}", deviceId, channelId);
            return null;
        }

        log.info("Opened RTP server on port {} for stream: {}", rtpPort, streamId);

        // Create stream session
        StreamSession session = new StreamSession();
        session.setSessionId(sessionId);
        session.setDeviceId(deviceId);
        session.setChannelId(channelId);
        session.setStreamType("LIVE");
        session.setApp("rtp");
        session.setStream(streamId);
        session.setSsrc(ssrc);
        session.setMediaServerId("default");
        session.setMediaServerIp(zlmConfig.getIp());
        session.setRtpPort(rtpPort);
        session.setStatus("ACTIVE");
        session.setStartTime(LocalDateTime.now());

        // Build playback URLs (will be available after device starts pushing)
        Map<String, String> urls = zlmClient.buildPlayUrls("rtp", streamId);
        session.setFlvUrl(urls.get("flv"));
        session.setHlsUrl(urls.get("hls"));
        session.setRtmpUrl(urls.get("rtmp"));
        session.setRtspUrl(urls.get("rtsp"));
        session.setWebrtcUrl(urls.get("webrtc"));

        streamSessionMapper.insert(session);
        log.info("Created stream session: {}", sessionId);

        return session;
    }

    /**
     * Create RTSP proxy session for ONVIF device
     *
     * @param deviceId  Device ID
     * @param channelId Channel ID
     * @param rtspUrl   RTSP URL
     * @return Stream session
     */
    public StreamSession createRtspProxySession(String deviceId, String channelId, String rtspUrl) {
        String sessionId = IdUtil.simpleUUID();
        String streamId = generateStreamId(deviceId, channelId);

        // Add stream proxy
        boolean success = zlmClient.addStreamProxy("live", streamId, rtspUrl, true, true, true, false);
        if (!success) {
            log.error("Failed to add stream proxy for RTSP URL: {}", rtspUrl);
            return null;
        }

        log.info("Added stream proxy for stream: {}", streamId);

        // Create stream session
        StreamSession session = new StreamSession();
        session.setSessionId(sessionId);
        session.setDeviceId(deviceId);
        session.setChannelId(channelId);
        session.setStreamType("LIVE");
        session.setApp("live");
        session.setStream(streamId);
        session.setMediaServerId("default");
        session.setMediaServerIp(zlmConfig.getIp());
        session.setStatus("ACTIVE");
        session.setStartTime(LocalDateTime.now());

        // Build playback URLs
        Map<String, String> urls = zlmClient.buildPlayUrls("live", streamId);
        session.setFlvUrl(urls.get("flv"));
        session.setHlsUrl(urls.get("hls"));
        session.setRtmpUrl(urls.get("rtmp"));
        session.setRtspUrl(urls.get("rtsp"));
        session.setWebrtcUrl(urls.get("webrtc"));

        streamSessionMapper.insert(session);
        log.info("Created RTSP proxy session: {}", sessionId);

        return session;
    }

    /**
     * Close stream session
     *
     * @param sessionId Session ID
     * @return Success
     */
    public boolean closeSession(String sessionId) {
        StreamSession session = streamSessionMapper.selectById(sessionId);
        if (session == null) {
            log.warn("Stream session not found: {}", sessionId);
            return false;
        }

        // Close stream
        boolean success = zlmClient.closeStream(session.getApp(), session.getStream());
        if (!success) {
            log.warn("Failed to close stream: {}/{}", session.getApp(), session.getStream());
        }

        // Close RTP server if exists
        if ("rtp".equals(session.getApp())) {
            zlmClient.closeRtpServer(session.getStream());
        }

        // Update session status
        session.setStatus("CLOSED");
        session.setEndTime(LocalDateTime.now());
        streamSessionMapper.updateById(session);

        log.info("Closed stream session: {}", sessionId);
        return true;
    }

    /**
     * Generate stream ID from device and channel
     */
    private String generateStreamId(String deviceId, String channelId) {
        return deviceId + "_" + channelId;
    }
}

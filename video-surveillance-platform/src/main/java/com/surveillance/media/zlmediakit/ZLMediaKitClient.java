package com.surveillance.media.zlmediakit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ZLMediaKit HTTP API Client
 */
@Slf4j
@Component
public class ZLMediaKitClient {

    private final ZLMediaKitConfig config;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public ZLMediaKitClient(ZLMediaKitConfig config) {
        this.config = config;
        this.gson = new Gson();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Open RTP server port for receiving RTP stream
     *
     * @param streamId Stream ID
     * @param port     Port (0 for auto allocation)
     * @param tcpMode  TCP mode: 0-UDP, 1-TCP passive, 2-TCP active
     * @param ssrc     SSRC
     * @return RTP port
     */
    public Integer openRtpServer(String streamId, Integer port, Integer tcpMode, String ssrc) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("secret", config.getSecret());
            params.addProperty("port", port);
            params.addProperty("enable_tcp", tcpMode > 0 ? 1 : 0);
            params.addProperty("stream_id", streamId);
            if (ssrc != null) {
                params.addProperty("ssrc", ssrc);
            }

            JsonObject response = post("/openRtpServer", params);
            if (response.get("code").getAsInt() == 0) {
                return response.get("port").getAsInt();
            } else {
                log.error("Failed to open RTP server: {}", response.get("msg").getAsString());
                return null;
            }
        } catch (Exception e) {
            log.error("Error opening RTP server", e);
            return null;
        }
    }

    /**
     * Close RTP server
     *
     * @param streamId Stream ID
     * @return Success
     */
    public boolean closeRtpServer(String streamId) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("secret", config.getSecret());
            params.addProperty("stream_id", streamId);

            JsonObject response = post("/closeRtpServer", params);
            return response.get("code").getAsInt() == 0;
        } catch (Exception e) {
            log.error("Error closing RTP server", e);
            return false;
        }
    }

    /**
     * Start sending RTP stream (for cascade)
     *
     * @param app      Application name
     * @param stream   Stream name
     * @param ssrc     SSRC
     * @param dstUrl   Destination URL (IP:port)
     * @param dstPort  Destination port
     * @param isUdp    Use UDP
     * @return Success
     */
    public boolean startSendRtp(String app, String stream, String ssrc, String dstUrl, Integer dstPort, Boolean isUdp) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("secret", config.getSecret());
            params.addProperty("vhost", "__defaultVhost__");
            params.addProperty("app", app);
            params.addProperty("stream", stream);
            params.addProperty("ssrc", ssrc);
            params.addProperty("dst_url", dstUrl);
            params.addProperty("dst_port", dstPort);
            params.addProperty("is_udp", isUdp ? 1 : 0);

            JsonObject response = post("/startSendRtp", params);
            return response.get("code").getAsInt() == 0;
        } catch (Exception e) {
            log.error("Error starting send RTP", e);
            return false;
        }
    }

    /**
     * Stop sending RTP stream
     *
     * @param app    Application name
     * @param stream Stream name
     * @return Success
     */
    public boolean stopSendRtp(String app, String stream) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("secret", config.getSecret());
            params.addProperty("vhost", "__defaultVhost__");
            params.addProperty("app", app);
            params.addProperty("stream", stream);

            JsonObject response = post("/stopSendRtp", params);
            return response.get("code").getAsInt() == 0;
        } catch (Exception e) {
            log.error("Error stopping send RTP", e);
            return false;
        }
    }

    /**
     * Add stream proxy (pull RTSP/RTMP stream)
     *
     * @param app       Application name
     * @param stream    Stream name
     * @param url       Source URL
     * @param enableRtsp Enable RTSP
     * @param enableRtmp Enable RTMP
     * @param enableHls  Enable HLS
     * @param enableMp4  Enable MP4
     * @return Success
     */
    public boolean addStreamProxy(String app, String stream, String url,
                                   Boolean enableRtsp, Boolean enableRtmp,
                                   Boolean enableHls, Boolean enableMp4) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("secret", config.getSecret());
            params.addProperty("vhost", "__defaultVhost__");
            params.addProperty("app", app);
            params.addProperty("stream", stream);
            params.addProperty("url", url);
            params.addProperty("enable_rtsp", enableRtsp ? 1 : 0);
            params.addProperty("enable_rtmp", enableRtmp ? 1 : 0);
            params.addProperty("enable_hls", enableHls ? 1 : 0);
            params.addProperty("enable_mp4", enableMp4 ? 0 : 0);
            params.addProperty("rtp_type", 0);

            JsonObject response = post("/addStreamProxy", params);
            return response.get("code").getAsInt() == 0;
        } catch (Exception e) {
            log.error("Error adding stream proxy", e);
            return false;
        }
    }

    /**
     * Delete stream proxy
     *
     * @param key Proxy key
     * @return Success
     */
    public boolean delStreamProxy(String key) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("secret", config.getSecret());
            params.addProperty("key", key);

            JsonObject response = post("/delStreamProxy", params);
            return response.get("code").getAsInt() == 0;
        } catch (Exception e) {
            log.error("Error deleting stream proxy", e);
            return false;
        }
    }

    /**
     * Close stream
     *
     * @param app    Application name
     * @param stream Stream name
     * @return Success
     */
    public boolean closeStream(String app, String stream) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("secret", config.getSecret());
            params.addProperty("vhost", "__defaultVhost__");
            params.addProperty("app", app);
            params.addProperty("stream", stream);
            params.addProperty("force", 1);

            JsonObject response = post("/close_stream", params);
            return response.get("code").getAsInt() == 0;
        } catch (Exception e) {
            log.error("Error closing stream", e);
            return false;
        }
    }

    /**
     * Get media list
     *
     * @return Media list
     */
    public JsonObject getMediaList() {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("secret", config.getSecret());

            return post("/getMediaList", params);
        } catch (Exception e) {
            log.error("Error getting media list", e);
            return null;
        }
    }

    /**
     * Get server configuration
     *
     * @return Server config
     */
    public JsonObject getServerConfig() {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("secret", config.getSecret());

            return post("/getServerConfig", params);
        } catch (Exception e) {
            log.error("Error getting server config", e);
            return null;
        }
    }

    /**
     * Build playback URLs for a stream
     *
     * @param app    Application name
     * @param stream Stream name
     * @return Map of playback URLs
     */
    public Map<String, String> buildPlayUrls(String app, String stream) {
        String baseUrl = config.getIp();
        return Map.of(
                "flv", String.format("http://%s:%d/%s/%s.live.flv", baseUrl, config.getHttpPort(), app, stream),
                "hls", String.format("http://%s:%d/%s/%s/hls.m3u8", baseUrl, config.getHttpPort(), app, stream),
                "rtmp", String.format("rtmp://%s:%d/%s/%s", baseUrl, config.getRtmpPort(), app, stream),
                "rtsp", String.format("rtsp://%s:%d/%s/%s", baseUrl, config.getRtspPort(), app, stream),
                "webrtc", String.format("http://%s:%d/%s/%s.live.flv", baseUrl, config.getHttpPort(), app, stream)
        );
    }

    /**
     * POST request to ZLMediaKit API
     */
    private JsonObject post(String path, JsonObject params) throws IOException {
        String url = config.getApiUrl() + path;
        String json = gson.toJson(params);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                return gson.fromJson(responseBody, JsonObject.class);
            } else {
                throw new IOException("HTTP request failed: " + response.code());
            }
        }
    }
}

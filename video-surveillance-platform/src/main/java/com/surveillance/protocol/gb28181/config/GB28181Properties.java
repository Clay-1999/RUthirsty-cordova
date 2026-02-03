package com.surveillance.protocol.gb28181.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GB28181 Configuration Properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "gb28181")
public class GB28181Properties {

    private Boolean enabled = true;

    private SipConfig sip = new SipConfig();

    private MediaConfig media = new MediaConfig();

    private KeepaliveConfig keepalive = new KeepaliveConfig();

    @Data
    public static class SipConfig {
        private String ip = "127.0.0.1";
        private Integer port = 5060;
        private String domain = "3402000000";
        private String id = "34020000002000000001";
        private String password = "12345678";
        private String transport = "UDP";
        private String charset = "GB2312";
    }

    @Data
    public static class MediaConfig {
        private String ip = "127.0.0.1";
        private String rtpPortRange = "30000-30500";
        private Integer rtpReceiveTimeout = 15000;
    }

    @Data
    public static class KeepaliveConfig {
        private Integer timeout = 180;
        private Integer interval = 60;
    }

    public Integer getRtpPortStart() {
        String[] parts = media.getRtpPortRange().split("-");
        return Integer.parseInt(parts[0]);
    }

    public Integer getRtpPortEnd() {
        String[] parts = media.getRtpPortRange().split("-");
        return Integer.parseInt(parts[1]);
    }
}

package com.surveillance.media.zlmediakit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ZLMediaKit Configuration Properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "zlmediakit")
public class ZLMediaKitConfig {
    private Boolean enabled = true;
    private String ip = "127.0.0.1";
    private Integer httpPort = 80;
    private Integer httpSslPort = 443;
    private Integer rtmpPort = 1935;
    private Integer rtspPort = 554;
    private Integer rtpProxyPort = 10000;
    private String secret = "035c73f7-bb6b-4889-a715-d9eb2d1925cc";
    private Boolean hookEnable = true;
    private String hookAdminParams;

    public String getHttpUrl() {
        return "http://" + ip + ":" + httpPort;
    }

    public String getApiUrl() {
        return getHttpUrl() + "/index/api";
    }
}

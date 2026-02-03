package com.surveillance.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Stream Session Entity
 */
@Data
@TableName("t_stream_session")
public class StreamSession implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String sessionId;
    private String deviceId;
    private String channelId;
    private String streamType;
    private String app;
    private String stream;
    private String ssrc;
    private String mediaServerId;
    private String mediaServerIp;
    private Integer rtpPort;
    private String flvUrl;
    private String hlsUrl;
    private String rtmpUrl;
    private String rtspUrl;
    private String webrtcUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String callId;
    private String fromTag;
    private String toTag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

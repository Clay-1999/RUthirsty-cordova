package com.surveillance.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Device Entity
 */
@Data
@TableName("t_device")
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String manufacturer;
    private String model;
    private String firmware;
    private String ipAddress;
    private Integer port;
    private String username;
    private String password;
    private String status;
    private LocalDateTime registerTime;
    private LocalDateTime lastKeepaliveTime;
    private String transport;
    private String streamMode;
    private String charset;
    private Integer expires;
    private Integer keepaliveInterval;
    private String hostAddress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

package com.surveillance.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Device Channel Entity
 */
@Data
@TableName("t_device_channel")
public class DeviceChannel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String deviceId;
    private String channelId;
    private String channelName;
    private String manufacturer;
    private String model;
    private String owner;
    private String civilCode;
    private String block;
    private String address;
    private Integer parental;
    private String parentId;
    private Integer safetyWay;
    private Integer registerWay;
    private String certNum;
    private Integer certifiable;
    private Integer errCode;
    private LocalDateTime endTime;
    private Integer secrecy;
    private String ipAddress;
    private Integer port;
    private String status;
    private Double longitude;
    private Double latitude;
    private Integer ptzType;
    private Integer positionType;
    private Integer roomType;
    private Integer useType;
    private Integer supplyLightType;
    private Integer directionType;
    private String resolution;
    private String businessGroupId;
    private String downloadSpeed;
    private Integer svcSpaceSupportMode;
    private Integer svcTimeSupportMode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

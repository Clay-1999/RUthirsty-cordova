package com.surveillance.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Cascade Configuration Entity
 */
@Data
@TableName("t_cascade_config")
public class CascadeConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String platformId;
    private String platformName;
    private String serverIp;
    private Integer serverPort;
    private String serverDomain;
    private String localId;
    private String localIp;
    private Integer localPort;
    private String username;
    private String password;
    private Integer expires;
    private Integer keepaliveInterval;
    private String transport;
    private String charset;
    private String catalogGroup;
    private String catalogId;
    private String status;
    private LocalDateTime registerTime;
    private LocalDateTime lastKeepaliveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

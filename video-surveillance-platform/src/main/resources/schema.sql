-- Video Surveillance Platform Database Schema
-- Version: 1.0.0
-- Date: 2026-02-03

-- Create database
CREATE DATABASE IF NOT EXISTS video_surveillance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE video_surveillance;

-- ========================================
-- 1. Device Table
-- ========================================
CREATE TABLE IF NOT EXISTS t_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    device_id VARCHAR(50) UNIQUE NOT NULL COMMENT 'Device ID (GB28181 or custom)',
    device_name VARCHAR(100) NOT NULL COMMENT 'Device name',
    device_type VARCHAR(20) NOT NULL COMMENT 'Device type: ONVIF, GB28181',
    manufacturer VARCHAR(50) COMMENT 'Manufacturer',
    model VARCHAR(50) COMMENT 'Model',
    firmware VARCHAR(50) COMMENT 'Firmware version',
    ip_address VARCHAR(50) COMMENT 'IP address',
    port INT COMMENT 'Port',
    username VARCHAR(50) COMMENT 'Username',
    password VARCHAR(100) COMMENT 'Password (encrypted)',
    status VARCHAR(20) DEFAULT 'OFFLINE' COMMENT 'Status: ONLINE, OFFLINE',
    register_time DATETIME COMMENT 'Registration time',
    last_keepalive_time DATETIME COMMENT 'Last keepalive time',
    transport VARCHAR(10) COMMENT 'Transport protocol: UDP, TCP',
    stream_mode VARCHAR(20) COMMENT 'Stream mode: UDP, TCP_PASSIVE, TCP_ACTIVE',
    charset VARCHAR(10) DEFAULT 'GB2312' COMMENT 'Character encoding',
    expires INT DEFAULT 3600 COMMENT 'Registration expiration (seconds)',
    keepalive_interval INT DEFAULT 60 COMMENT 'Keepalive interval (seconds)',
    host_address VARCHAR(50) COMMENT 'Host address from Via header',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete: 0-not deleted, 1-deleted',
    INDEX idx_device_id (device_id),
    INDEX idx_status (status),
    INDEX idx_device_type (device_type),
    INDEX idx_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Device table';

-- ========================================
-- 2. Device Channel Table
-- ========================================
CREATE TABLE IF NOT EXISTS t_device_channel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    device_id VARCHAR(50) NOT NULL COMMENT 'Device ID',
    channel_id VARCHAR(50) NOT NULL COMMENT 'Channel ID',
    channel_name VARCHAR(100) COMMENT 'Channel name',
    manufacturer VARCHAR(50) COMMENT 'Manufacturer',
    model VARCHAR(50) COMMENT 'Model',
    owner VARCHAR(50) COMMENT 'Owner',
    civil_code VARCHAR(20) COMMENT 'Civil code (administrative division)',
    block VARCHAR(50) COMMENT 'Block',
    address VARCHAR(200) COMMENT 'Installation address',
    parental INT DEFAULT 0 COMMENT 'Has sub-devices: 0-no, 1-yes',
    parent_id VARCHAR(50) COMMENT 'Parent device/area ID',
    safety_way INT DEFAULT 0 COMMENT 'Security mode',
    register_way INT DEFAULT 1 COMMENT 'Registration method',
    cert_num VARCHAR(50) COMMENT 'Certificate number',
    certifiable INT DEFAULT 0 COMMENT 'Certifiable',
    err_code INT DEFAULT 0 COMMENT 'Error code',
    end_time DATETIME COMMENT 'End time',
    secrecy INT DEFAULT 0 COMMENT 'Secrecy level',
    ip_address VARCHAR(50) COMMENT 'IP address',
    port INT COMMENT 'Port',
    status VARCHAR(20) DEFAULT 'OFF' COMMENT 'Status: ON, OFF',
    longitude DOUBLE COMMENT 'Longitude',
    latitude DOUBLE COMMENT 'Latitude',
    ptz_type INT DEFAULT 0 COMMENT 'PTZ type: 0-unknown, 1-dome, 2-hemisphere, 3-fixed, 4-remote',
    position_type INT DEFAULT 0 COMMENT 'Position type',
    room_type INT DEFAULT 0 COMMENT 'Room type',
    use_type INT DEFAULT 0 COMMENT 'Use type',
    supply_light_type INT DEFAULT 0 COMMENT 'Supply light type',
    direction_type INT DEFAULT 0 COMMENT 'Direction type',
    resolution VARCHAR(20) COMMENT 'Resolution',
    business_group_id VARCHAR(50) COMMENT 'Business group ID',
    download_speed VARCHAR(20) COMMENT 'Download speed',
    svc_space_support_mode INT DEFAULT 0 COMMENT 'SVC space support mode',
    svc_time_support_mode INT DEFAULT 0 COMMENT 'SVC time support mode',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete: 0-not deleted, 1-deleted',
    UNIQUE KEY uk_device_channel (device_id, channel_id),
    INDEX idx_device_id (device_id),
    INDEX idx_channel_id (channel_id),
    INDEX idx_status (status),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Device channel table';

-- ========================================
-- 3. Cascade Configuration Table
-- ========================================
CREATE TABLE IF NOT EXISTS t_cascade_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    platform_id VARCHAR(50) UNIQUE NOT NULL COMMENT 'Upstream platform ID',
    platform_name VARCHAR(100) NOT NULL COMMENT 'Upstream platform name',
    server_ip VARCHAR(50) NOT NULL COMMENT 'Upstream platform IP',
    server_port INT NOT NULL COMMENT 'Upstream platform port',
    server_domain VARCHAR(100) COMMENT 'Upstream platform domain',
    local_id VARCHAR(50) NOT NULL COMMENT 'Local platform ID',
    local_ip VARCHAR(50) NOT NULL COMMENT 'Local platform IP',
    local_port INT NOT NULL COMMENT 'Local platform port',
    username VARCHAR(50) COMMENT 'Authentication username',
    password VARCHAR(100) COMMENT 'Authentication password',
    expires INT DEFAULT 3600 COMMENT 'Registration expiration (seconds)',
    keepalive_interval INT DEFAULT 60 COMMENT 'Keepalive interval (seconds)',
    transport VARCHAR(10) DEFAULT 'UDP' COMMENT 'Transport protocol: UDP, TCP',
    charset VARCHAR(10) DEFAULT 'GB2312' COMMENT 'Character encoding',
    catalog_group VARCHAR(50) DEFAULT 'default' COMMENT 'Catalog group',
    catalog_id VARCHAR(50) COMMENT 'Catalog ID',
    status VARCHAR(20) DEFAULT 'DISABLED' COMMENT 'Status: ENABLED, DISABLED, REGISTERED, UNREGISTERED',
    register_time DATETIME COMMENT 'Registration time',
    last_keepalive_time DATETIME COMMENT 'Last keepalive time',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete: 0-not deleted, 1-deleted',
    INDEX idx_platform_id (platform_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cascade configuration table';

-- ========================================
-- 4. Stream Session Table
-- ========================================
CREATE TABLE IF NOT EXISTS t_stream_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    session_id VARCHAR(50) UNIQUE NOT NULL COMMENT 'Session ID',
    device_id VARCHAR(50) NOT NULL COMMENT 'Device ID',
    channel_id VARCHAR(50) NOT NULL COMMENT 'Channel ID',
    stream_type VARCHAR(20) NOT NULL COMMENT 'Stream type: LIVE, PLAYBACK, DOWNLOAD',
    app VARCHAR(50) COMMENT 'Application name',
    stream VARCHAR(100) COMMENT 'Stream ID',
    ssrc VARCHAR(20) COMMENT 'SSRC',
    media_server_id VARCHAR(50) COMMENT 'Media server ID',
    media_server_ip VARCHAR(50) COMMENT 'Media server IP',
    rtp_port INT COMMENT 'RTP port',
    flv_url VARCHAR(500) COMMENT 'FLV playback URL',
    hls_url VARCHAR(500) COMMENT 'HLS playback URL',
    rtmp_url VARCHAR(500) COMMENT 'RTMP playback URL',
    rtsp_url VARCHAR(500) COMMENT 'RTSP playback URL',
    webrtc_url VARCHAR(500) COMMENT 'WebRTC playback URL',
    start_time DATETIME COMMENT 'Start time',
    end_time DATETIME COMMENT 'End time',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'Status: ACTIVE, CLOSED, ERROR',
    call_id VARCHAR(100) COMMENT 'SIP Call-ID',
    from_tag VARCHAR(50) COMMENT 'SIP From tag',
    to_tag VARCHAR(50) COMMENT 'SIP To tag',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete: 0-not deleted, 1-deleted',
    INDEX idx_device_channel (device_id, channel_id),
    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    INDEX idx_ssrc (ssrc),
    INDEX idx_call_id (call_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Stream session table';

-- ========================================
-- 5. PTZ Preset Table
-- ========================================
CREATE TABLE IF NOT EXISTS t_ptz_preset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    device_id VARCHAR(50) NOT NULL COMMENT 'Device ID',
    channel_id VARCHAR(50) NOT NULL COMMENT 'Channel ID',
    preset_id INT NOT NULL COMMENT 'Preset ID',
    preset_name VARCHAR(100) COMMENT 'Preset name',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete: 0-not deleted, 1-deleted',
    UNIQUE KEY uk_device_channel_preset (device_id, channel_id, preset_id),
    INDEX idx_device_channel (device_id, channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PTZ preset table';

-- ========================================
-- 6. Media Server Table
-- ========================================
CREATE TABLE IF NOT EXISTS t_media_server (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    server_id VARCHAR(50) UNIQUE NOT NULL COMMENT 'Server ID',
    server_name VARCHAR(100) NOT NULL COMMENT 'Server name',
    ip VARCHAR(50) NOT NULL COMMENT 'IP address',
    http_port INT DEFAULT 80 COMMENT 'HTTP port',
    http_ssl_port INT DEFAULT 443 COMMENT 'HTTPS port',
    rtmp_port INT DEFAULT 1935 COMMENT 'RTMP port',
    rtsp_port INT DEFAULT 554 COMMENT 'RTSP port',
    rtp_proxy_port INT DEFAULT 10000 COMMENT 'RTP proxy port',
    secret VARCHAR(100) COMMENT 'API secret',
    status VARCHAR(20) DEFAULT 'OFFLINE' COMMENT 'Status: ONLINE, OFFLINE',
    last_heartbeat_time DATETIME COMMENT 'Last heartbeat time',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete: 0-not deleted, 1-deleted',
    INDEX idx_server_id (server_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Media server table';

-- ========================================
-- Insert default media server
-- ========================================
INSERT INTO t_media_server (server_id, server_name, ip, http_port, rtmp_port, rtsp_port, rtp_proxy_port, secret, status)
VALUES ('default', 'Default ZLMediaKit Server', '127.0.0.1', 80, 1935, 554, 10000, '035c73f7-bb6b-4889-a715-d9eb2d1925cc', 'ONLINE')
ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;

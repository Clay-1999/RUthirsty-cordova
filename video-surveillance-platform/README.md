# Video Surveillance Platform - Backend

基于Spring Boot的视频监控平台后端服务，支持GB/T 28181和ONVIF协议。

## 技术栈

- Spring Boot 3.2.x
- MySQL 8.0
- MyBatis Plus
- Redis
- GB/T 28181 (JAIN-SIP)
- ZLMediaKit流媒体服务器

## 快速开始

### 1. 启动依赖服务

使用Docker Compose启动MySQL、Redis和ZLMediaKit:

```bash
cd docker
docker-compose up -d
```

### 2. 配置应用

编辑 `src/main/resources/application.yml`，修改以下配置:

- 数据库连接信息
- Redis连接信息
- GB28181平台编码和IP地址
- ZLMediaKit服务器地址

### 3. 构建项目

```bash
mvn clean package
```

### 4. 运行应用

```bash
java -jar target/video-surveillance-platform-1.0.0.jar
```

或使用Maven:

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## 主要功能

### 设备管理
- GB28181设备注册和心跳管理
- ONVIF设备接入
- 设备通道管理
- 设备状态监控

### 流媒体服务
- 实时视频点播
- 历史视频回放
- 多种播放协议支持 (FLV/HLS/RTMP/RTSP/WebRTC)

### PTZ控制
- 云台方向控制
- 变倍控制
- 预置位管理

### 级联功能
- 向上级平台注册
- 目录同步
- 流转发

## API文档

启动应用后访问: `http://localhost:8080/doc.html`

## 项目结构

```
src/main/java/com/surveillance/
├── config/                 # 配置类
├── controller/             # REST API控制器
├── service/                # 业务服务层
│   ├── device/            # 设备管理
│   ├── stream/            # 流媒体服务
│   ├── cascade/           # 级联服务
│   └── ptz/               # PTZ控制
├── protocol/               # 协议实现
│   ├── gb28181/           # GB28181协议
│   └── onvif/             # ONVIF协议
├── media/                  # 流媒体管理
│   └── zlmediakit/        # ZLMediaKit集成
├── dao/                    # 数据访问层
├── dto/                    # 数据传输对象
├── websocket/              # WebSocket
└── exception/              # 异常处理
```

## 配置说明

### GB28181配置

```yaml
gb28181:
  sip:
    ip: 192.168.1.100        # 本地SIP服务器IP
    port: 5060                # SIP端口
    domain: 3402000000        # SIP域
    id: 34020000002000000001  # 平台编码(20位)
    password: 12345678        # 认证密码
```

### ZLMediaKit配置

```yaml
zlmediakit:
  ip: 192.168.1.100          # ZLMediaKit服务器IP
  http-port: 80              # HTTP API端口
  secret: your-secret-key    # API密钥
```

## 开发指南

### 添加新的设备协议

1. 在 `protocol/` 目录下创建新的协议包
2. 实现设备发现、连接、控制接口
3. 在 `service/device/` 中添加对应的服务类
4. 注册到设备管理服务

### 扩展流媒体功能

1. 在 `media/` 目录下实现新的流媒体服务器客户端
2. 在 `StreamManager` 中添加对应的会话管理方法
3. 更新流媒体API控制器

## 常见问题

### 1. GB28181设备无法注册

- 检查SIP端口(5060)是否开放
- 确认平台编码配置正确
- 查看设备和平台的网络连通性

### 2. 视频无法播放

- 确认ZLMediaKit服务正常运行
- 检查RTP端口范围(30000-30500)是否开放
- 查看流媒体服务器日志

### 3. 级联注册失败

- 确认上级平台配置正确
- 检查网络连通性
- 查看SIP消息日志

## 许可证

MIT License

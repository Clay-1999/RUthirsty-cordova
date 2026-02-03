# 视频监控平台 (Video Surveillance Platform)

基于Spring Boot和React的视频监控平台，支持GB/T 28181和ONVIF协议的设备接入，支持北向GB/T 28181级联。

## 项目概述

本项目是一个完整的视频监控解决方案，包括：

- **后端服务**: 基于Spring Boot 3.2.x，实现设备管理、流媒体控制、PTZ控制、级联管理等功能
- **前端应用**: 基于React 18 + TypeScript，提供设备管理、实况监控、级联管理等界面
- **流媒体服务**: 集成ZLMediaKit，支持多种流媒体协议转换和分发

## 技术架构

### 后端技术栈
- Spring Boot 3.2.x
- MySQL 8.0
- MyBatis Plus
- Redis
- GB/T 28181 (JAIN-SIP)
- ONVIF协议
- ZLMediaKit流媒体服务器

### 前端技术栈
- React 18
- TypeScript 5
- Ant Design 5
- Vite 5
- Axios
- flv.js / hls.js

## 核心功能

### 1. 设备管理
- ✅ GB28181设备注册和心跳管理
- ✅ ONVIF设备接入
- ✅ 设备通道管理
- ✅ 设备状态监控
- ✅ 设备信息查询

### 2. 流媒体服务
- ✅ 实时视频点播
- ✅ 多种播放协议支持 (FLV/HLS/RTMP/RTSP/WebRTC)
- ✅ 流会话管理
- ⏳ 历史视频回放
- ⏳ 录像查询和下载

### 3. PTZ控制
- ✅ 云台方向控制
- ✅ 变倍控制
- ⏳ 预置位管理
- ⏳ 巡航控制

### 4. 级联功能
- ✅ 向上级平台注册
- ⏳ 目录同步
- ⏳ 流转发
- ⏳ 级联状态监控

### 5. 前端界面
- ✅ 设备管理页面
- ✅ 实况监控页面 (多画面)
- ✅ 级联管理页面
- ✅ PTZ控制面板
- ✅ 视频播放器组件

## 项目结构

```
.
├── video-surveillance-platform/    # 后端项目
│   ├── src/main/
│   │   ├── java/com/surveillance/
│   │   │   ├── config/            # 配置类
│   │   │   ├── controller/        # REST API
│   │   │   ├── service/           # 业务服务
│   │   │   ├── protocol/          # 协议实现
│   │   │   │   ├── gb28181/      # GB28181协议
│   │   │   │   └── onvif/        # ONVIF协议
│   │   │   ├── media/             # 流媒体管理
│   │   │   ├── dao/               # 数据访问
│   │   │   └── dto/               # 数据传输对象
│   │   └── resources/
│   │       ├── application.yml    # 配置文件
│   │       └── schema.sql         # 数据库脚本
│   ├── docker/                    # Docker配置
│   │   └── docker-compose.yml
│   └── pom.xml
│
└── video-surveillance-web/         # 前端项目
    ├── src/
    │   ├── api/                   # API接口
    │   ├── components/            # 组件
    │   ├── pages/                 # 页面
    │   ├── types/                 # 类型定义
    │   └── App.tsx
    ├── package.json
    └── vite.config.ts
```

## 快速开始

### 前置要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Docker (可选)

### 1. 启动依赖服务

使用Docker Compose启动MySQL、Redis和ZLMediaKit:

```bash
cd video-surveillance-platform/docker
docker-compose up -d
```

### 2. 启动后端服务

```bash
cd video-surveillance-platform
mvn clean package
java -jar target/video-surveillance-platform-1.0.0.jar
```

后端服务将在 `http://localhost:8080` 启动。

### 3. 启动前端应用

```bash
cd video-surveillance-web
npm install
npm run dev
```

前端应用将在 `http://localhost:3000` 启动。

### 4. 访问应用

打开浏览器访问: `http://localhost:3000`

## 配置说明

### 后端配置

编辑 `video-surveillance-platform/src/main/resources/application.yml`:

```yaml
# GB28181配置
gb28181:
  sip:
    ip: 192.168.1.100              # 修改为本机IP
    port: 5060
    id: 34020000002000000001        # 平台编码(20位)
    password: 12345678

# ZLMediaKit配置
zlmediakit:
  ip: 192.168.1.100                # 修改为ZLMediaKit服务器IP
  http-port: 80
  secret: 035c73f7-bb6b-4889-a715-d9eb2d1925cc
```

### 前端配置

编辑 `video-surveillance-web/vite.config.ts`:

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  # 后端服务地址
      changeOrigin: true,
    },
  },
}
```

## API文档

### 设备管理API

```
POST   /api/device/add              # 添加设备
PUT    /api/device/update           # 更新设备
DELETE /api/device/delete/{id}      # 删除设备
GET    /api/device/list             # 设备列表
GET    /api/device/channels/{id}    # 获取设备通道
POST   /api/device/sync/{id}        # 同步设备通道
```

### 流媒体API

```
POST   /api/stream/play             # 开始播放
POST   /api/stream/stop             # 停止播放
GET    /api/stream/info/{sessionId} # 流信息
GET    /api/stream/list             # 流会话列表
```

### PTZ控制API

```
POST   /api/ptz/control             # PTZ控制
POST   /api/ptz/preset/set          # 设置预置位
POST   /api/ptz/preset/call         # 调用预置位
```

### 级联管理API

```
POST   /api/cascade/add             # 添加级联配置
POST   /api/cascade/register/{id}   # 向上级注册
POST   /api/cascade/unregister/{id} # 注销注册
```

## 开发进度

### 已完成 ✅
- [x] 项目基础架构搭建
- [x] 数据库设计和实体类
- [x] ZLMediaKit集成
- [x] 前端项目结构和组件
- [x] 设备管理界面
- [x] 实况监控界面
- [x] 级联管理界面
- [x] 视频播放器组件
- [x] PTZ控制面板

### 进行中 ⏳
- [ ] GB28181 SIP协议栈实现
- [ ] 设备管理服务和API
- [ ] PTZ控制功能实现
- [ ] ONVIF协议支持
- [ ] GB28181级联功能

### 待开发 📋
- [ ] 历史视频回放
- [ ] 录像查询和下载
- [ ] 预置位管理
- [ ] 用户权限管理
- [ ] 系统日志和监控
- [ ] 性能优化

## 部署指南

### Docker部署

1. 构建后端镜像:
```bash
cd video-surveillance-platform
docker build -t video-surveillance-backend .
```

2. 构建前端镜像:
```bash
cd video-surveillance-web
npm run build
docker build -t video-surveillance-frontend .
```

3. 使用docker-compose启动所有服务:
```bash
docker-compose up -d
```

### 生产环境部署

参考各子项目的README文档:
- [后端部署文档](video-surveillance-platform/README.md)
- [前端部署文档](video-surveillance-web/README.md)

## 常见问题

### 1. GB28181设备无法注册
- 检查SIP端口(5060)是否开放
- 确认平台编码配置正确
- 查看设备和平台的网络连通性

### 2. 视频无法播放
- 确认ZLMediaKit服务正常运行
- 检查RTP端口范围(30000-30500)是否开放
- 查看流媒体服务器日志

### 3. 前端无法连接后端
- 检查后端服务是否启动
- 确认API代理配置正确
- 查看浏览器控制台错误信息

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

MIT License

## 联系方式

如有问题，请提交Issue或联系开发团队。

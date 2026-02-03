# Video Surveillance Platform - Frontend

基于React + TypeScript + Vite的视频监控平台前端应用。

## 技术栈

- React 18
- TypeScript 5
- Vite 5
- Ant Design 5
- React Router 6
- Axios
- flv.js / hls.js (视频播放)
- Zustand (状态管理)

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

应用将在 `http://localhost:3000` 启动。

### 3. 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist` 目录。

### 4. 预览生产构建

```bash
npm run preview
```

## 项目结构

```
src/
├── api/                    # API接口层
│   ├── request.ts         # Axios封装
│   ├── device.ts          # 设备API
│   ├── stream.ts          # 流媒体API
│   ├── ptz.ts             # PTZ控制API
│   └── cascade.ts         # 级联API
├── types/                  # TypeScript类型定义
│   └── index.ts
├── components/             # 通用组件
│   ├── Layout/            # 布局组件
│   ├── VideoPlayer/       # 视频播放器
│   │   └── FlvPlayer.tsx  # FLV播放器
│   ├── PTZControl/        # PTZ控制面板
│   └── DeviceTree/        # 设备树
├── pages/                  # 页面组件
│   ├── DeviceManage/      # 设备管理
│   ├── LiveStream/        # 实况监控
│   └── CascadeManage/     # 级联管理
├── hooks/                  # 自定义Hooks
├── utils/                  # 工具函数
├── store/                  # 状态管理
├── App.tsx                 # 根组件
├── main.tsx                # 入口文件
└── index.css               # 全局样式
```

## 主要功能

### 设备管理
- 设备列表展示
- 添加/编辑/删除设备
- 设备状态监控
- GB28181设备通道同步
- 设备统计信息

### 实况监控
- 多画面视频播放(1/4/9/16画面)
- 设备通道树形展示
- FLV/HLS视频播放
- PTZ云台控制
- 视频窗口切换

### 级联管理
- 级联配置管理
- 向上级平台注册/注销
- 目录同步
- 级联状态监控

## 配置说明

### API代理配置

在 `vite.config.ts` 中配置后端API代理:

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

### 视频播放器

支持两种视频播放方式:

1. **FLV播放** (推荐)
   - 低延迟
   - 适合实时监控
   - 使用flv.js

2. **HLS播放**
   - 兼容性好
   - 延迟较高
   - 使用hls.js

## 开发指南

### 添加新页面

1. 在 `src/pages/` 下创建页面组件
2. 在 `src/App.tsx` 中添加路由
3. 在 `src/components/Layout/index.tsx` 中添加菜单项

### 添加新API

1. 在 `src/types/index.ts` 中定义类型
2. 在 `src/api/` 下创建API文件
3. 使用 `request` 函数发起请求

### 自定义主题

在 `src/main.tsx` 中配置Ant Design主题:

```typescript
<ConfigProvider
  theme={{
    token: {
      colorPrimary: '#1890ff',
    },
  }}
>
  <App />
</ConfigProvider>
```

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## 常见问题

### 1. 视频无法播放

- 检查后端服务是否正常运行
- 确认ZLMediaKit服务正常
- 检查浏览器控制台错误信息
- 确认视频流URL正确

### 2. API请求失败

- 检查后端服务地址配置
- 确认网络连接正常
- 查看浏览器Network面板

### 3. 跨域问题

- 开发环境使用Vite代理
- 生产环境配置Nginx反向代理

## 部署

### Nginx配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/video-surveillance-web/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 许可证

MIT License

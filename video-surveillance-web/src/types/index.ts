/**
 * Common API Response Type
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

/**
 * Device Type
 */
export interface Device {
  id?: number;
  deviceId: string;
  deviceName: string;
  deviceType: 'GB28181' | 'ONVIF';
  manufacturer?: string;
  model?: string;
  firmware?: string;
  ipAddress?: string;
  port?: number;
  username?: string;
  password?: string;
  status: 'ONLINE' | 'OFFLINE';
  registerTime?: string;
  lastKeepaliveTime?: string;
  transport?: 'UDP' | 'TCP';
  streamMode?: 'UDP' | 'TCP_PASSIVE' | 'TCP_ACTIVE';
  charset?: string;
  expires?: number;
  keepaliveInterval?: number;
  createTime?: string;
  updateTime?: string;
}

/**
 * Device Channel Type
 */
export interface DeviceChannel {
  id?: number;
  deviceId: string;
  channelId: string;
  channelName?: string;
  manufacturer?: string;
  model?: string;
  owner?: string;
  civilCode?: string;
  address?: string;
  parental?: number;
  parentId?: string;
  status: 'ON' | 'OFF';
  longitude?: number;
  latitude?: number;
  ptzType?: number;
  createTime?: string;
  updateTime?: string;
}

/**
 * Stream Session Type
 */
export interface StreamSession {
  id?: number;
  sessionId: string;
  deviceId: string;
  channelId: string;
  streamType: 'LIVE' | 'PLAYBACK';
  app?: string;
  stream?: string;
  ssrc?: string;
  mediaServerId?: string;
  flvUrl?: string;
  hlsUrl?: string;
  rtmpUrl?: string;
  rtspUrl?: string;
  webrtcUrl?: string;
  startTime?: string;
  endTime?: string;
  status: 'ACTIVE' | 'CLOSED';
  createTime?: string;
}

/**
 * Cascade Config Type
 */
export interface CascadeConfig {
  id?: number;
  platformId: string;
  platformName: string;
  serverIp: string;
  serverPort: number;
  serverDomain?: string;
  localId: string;
  localIp: string;
  localPort: number;
  username?: string;
  password?: string;
  expires?: number;
  keepaliveInterval?: number;
  transport?: 'UDP' | 'TCP';
  charset?: string;
  status: 'ENABLED' | 'DISABLED' | 'REGISTERED' | 'UNREGISTERED';
  registerTime?: string;
  lastKeepaliveTime?: string;
  createTime?: string;
  updateTime?: string;
}

/**
 * PTZ Command Type
 */
export interface PTZCommand {
  deviceId: string;
  channelId: string;
  command: 'LEFT' | 'RIGHT' | 'UP' | 'DOWN' | 'ZOOM_IN' | 'ZOOM_OUT' | 'STOP';
  speed?: number;
}

/**
 * PTZ Preset Type
 */
export interface PTZPreset {
  id?: number;
  deviceId: string;
  channelId: string;
  presetId: number;
  presetName?: string;
}

/**
 * Play Request Type
 */
export interface PlayRequest {
  deviceId: string;
  channelId: string;
  streamType?: 'LIVE' | 'PLAYBACK';
  startTime?: string;
  endTime?: string;
}

/**
 * Play Response Type
 */
export interface PlayResponse {
  sessionId: string;
  flvUrl: string;
  hlsUrl: string;
  rtmpUrl: string;
  rtspUrl: string;
  webrtcUrl: string;
}

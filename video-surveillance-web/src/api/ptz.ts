import { request } from './request';
import type { PTZCommand, PTZPreset } from '@/types';

/**
 * PTZ API
 */
export const ptzApi = {
  /**
   * PTZ control
   */
  control: (data: PTZCommand) => {
    return request<void>({
      url: '/ptz/control',
      method: 'POST',
      data,
    });
  },

  /**
   * Set preset
   */
  setPreset: (data: { deviceId: string; channelId: string; presetId: number; presetName?: string }) => {
    return request<void>({
      url: '/ptz/preset/set',
      method: 'POST',
      data,
    });
  },

  /**
   * Call preset
   */
  callPreset: (data: { deviceId: string; channelId: string; presetId: number }) => {
    return request<void>({
      url: '/ptz/preset/call',
      method: 'POST',
      data,
    });
  },

  /**
   * Delete preset
   */
  deletePreset: (data: { deviceId: string; channelId: string; presetId: number }) => {
    return request<void>({
      url: '/ptz/preset/delete',
      method: 'POST',
      data,
    });
  },

  /**
   * Get preset list
   */
  getPresetList: (deviceId: string, channelId: string) => {
    return request<PTZPreset[]>({
      url: '/ptz/preset/list',
      method: 'GET',
      params: { deviceId, channelId },
    });
  },
};

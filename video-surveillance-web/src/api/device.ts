import { request } from './request';
import type { Device, DeviceChannel } from '@/types';

/**
 * Device API
 */
export const deviceApi = {
  /**
   * Get device list
   */
  getList: (params?: any) => {
    return request<Device[]>({
      url: '/device/list',
      method: 'GET',
      params,
    });
  },

  /**
   * Get device detail
   */
  getDetail: (id: number) => {
    return request<Device>({
      url: `/device/detail/${id}`,
      method: 'GET',
    });
  },

  /**
   * Add device
   */
  add: (data: Device) => {
    return request<void>({
      url: '/device/add',
      method: 'POST',
      data,
    });
  },

  /**
   * Update device
   */
  update: (data: Device) => {
    return request<void>({
      url: '/device/update',
      method: 'PUT',
      data,
    });
  },

  /**
   * Delete device
   */
  delete: (id: number) => {
    return request<void>({
      url: `/device/delete/${id}`,
      method: 'DELETE',
    });
  },

  /**
   * Get device channels
   */
  getChannels: (deviceId: string) => {
    return request<DeviceChannel[]>({
      url: `/device/channels/${deviceId}`,
      method: 'GET',
    });
  },

  /**
   * Sync device channels (GB28181 catalog query)
   */
  syncChannels: (deviceId: string) => {
    return request<void>({
      url: `/device/sync/${deviceId}`,
      method: 'POST',
    });
  },

  /**
   * Get device status statistics
   */
  getStatus: () => {
    return request<{
      total: number;
      online: number;
      offline: number;
    }>({
      url: '/device/status',
      method: 'GET',
    });
  },
};

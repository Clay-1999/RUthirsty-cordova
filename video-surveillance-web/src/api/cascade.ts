import { request } from './request';
import type { CascadeConfig } from '@/types';

/**
 * Cascade API
 */
export const cascadeApi = {
  /**
   * Get cascade config list
   */
  getList: () => {
    return request<CascadeConfig[]>({
      url: '/cascade/list',
      method: 'GET',
    });
  },

  /**
   * Add cascade config
   */
  add: (data: CascadeConfig) => {
    return request<void>({
      url: '/cascade/add',
      method: 'POST',
      data,
    });
  },

  /**
   * Update cascade config
   */
  update: (data: CascadeConfig) => {
    return request<void>({
      url: '/cascade/update',
      method: 'PUT',
      data,
    });
  },

  /**
   * Delete cascade config
   */
  delete: (id: number) => {
    return request<void>({
      url: `/cascade/delete/${id}`,
      method: 'DELETE',
    });
  },

  /**
   * Register to upstream platform
   */
  register: (id: number) => {
    return request<void>({
      url: `/cascade/register/${id}`,
      method: 'POST',
    });
  },

  /**
   * Unregister from upstream platform
   */
  unregister: (id: number) => {
    return request<void>({
      url: `/cascade/unregister/${id}`,
      method: 'POST',
    });
  },

  /**
   * Get cascade status
   */
  getStatus: (id: number) => {
    return request<{
      status: string;
      registerTime?: string;
      lastKeepaliveTime?: string;
    }>({
      url: `/cascade/status/${id}`,
      method: 'GET',
    });
  },

  /**
   * Sync catalog to upstream
   */
  syncCatalog: (id: number) => {
    return request<void>({
      url: `/cascade/catalog/sync/${id}`,
      method: 'POST',
    });
  },
};

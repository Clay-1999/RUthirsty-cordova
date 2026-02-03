import { request } from './request';
import type { PlayRequest, PlayResponse, StreamSession } from '@/types';

/**
 * Stream API
 */
export const streamApi = {
  /**
   * Start live stream
   */
  play: (data: PlayRequest) => {
    return request<PlayResponse>({
      url: '/stream/play',
      method: 'POST',
      data,
    });
  },

  /**
   * Start playback
   */
  playback: (data: PlayRequest) => {
    return request<PlayResponse>({
      url: '/stream/playback',
      method: 'POST',
      data,
    });
  },

  /**
   * Stop stream
   */
  stop: (sessionId: string) => {
    return request<void>({
      url: '/stream/stop',
      method: 'POST',
      data: { sessionId },
    });
  },

  /**
   * Get stream info
   */
  getInfo: (sessionId: string) => {
    return request<StreamSession>({
      url: `/stream/info/${sessionId}`,
      method: 'GET',
    });
  },

  /**
   * Get stream session list
   */
  getList: () => {
    return request<StreamSession[]>({
      url: '/stream/list',
      method: 'GET',
    });
  },
};

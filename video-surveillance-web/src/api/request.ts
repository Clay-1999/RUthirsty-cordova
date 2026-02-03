import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { message } from 'antd';
import type { ApiResponse } from '@/types';

/**
 * Create Axios instance
 */
const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor
 */
instance.interceptors.request.use(
  (config) => {
    // Add authentication token if needed
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor
 */
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message: msg, data } = response.data;

    if (code === 200) {
      return data;
    } else {
      message.error(msg || 'Request failed');
      return Promise.reject(new Error(msg || 'Request failed'));
    }
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          message.error('Unauthorized, please login');
          // Redirect to login page
          break;
        case 403:
          message.error('Access denied');
          break;
        case 404:
          message.error('Resource not found');
          break;
        case 500:
          message.error(data?.message || 'Server error');
          break;
        default:
          message.error(data?.message || 'Request failed');
      }
    } else if (error.request) {
      message.error('Network error, please check your connection');
    } else {
      message.error('Request error: ' + error.message);
    }

    return Promise.reject(error);
  }
);

/**
 * Generic request function
 */
export const request = <T = any>(config: AxiosRequestConfig): Promise<T> => {
  return instance.request<any, T>(config);
};

export default instance;

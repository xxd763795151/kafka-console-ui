import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios';
import { notification } from 'ant-design-vue';
import { VueAxios } from './axios';
import { getClusterInfo } from '@/utils/local-cache';
import router from '@/router';

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/',
  timeout: 120000,
});

const errorHandler = (error: any): Promise<never> => {
  if (error.response) {
    if (error.response.status === 401) {
      notification.error({
        message: String(error.response.status),
        description: '请登录',
      });
      router.push({ path: '/login-page' });
    } else if (error.response.status === 403) {
      notification.error({
        message: '请求失败',
        description: '无权限执行此操作',
      });
    } else {
      notification.error({
        message: '请求失败',
        description: '操作失败，请稍后重试或联系管理员',
      });
    }
  } else {
    notification.error({
      message: '请求失败',
      description: '网络连接失败，请检查网络后重试',
    });
  }
  return Promise.reject(error);
};

request.interceptors.request.use((config: any): any => {
  const clusterInfo = getClusterInfo();
  if (clusterInfo && config.headers) {
    (config.headers as Record<string, string>)['X-Cluster-Info-Id'] = String(clusterInfo.id ?? '');
  }
  const token = localStorage.getItem('access_token');
  if (token && config.headers) {
    (config.headers as Record<string, string>)['X-Auth-Token'] = token;
  }
  // eslint-disable-next-line no-console
  console.debug('[request] baseURL=', request.defaults.baseURL, ' env.VITE_API_BASE_URL=', import.meta.env.VITE_API_BASE_URL, ' request.url=', config.url, ' method=', config.method);
  return config;
}, errorHandler);

request.interceptors.response.use((response: AxiosResponse): any => {
  if (response.config.responseType === 'blob') {
    return response;
  }
  return response.data;
}, errorHandler);

const installer = {
  vm: {} as Record<string, unknown>,
  install(app: any, instance?: AxiosInstance): void {
    VueAxios.install(app, instance || request);
  },
};

export default request;

export { installer as VueAxios, request as axios };

import axios from "axios";
import router from "../router";
import notification from "ant-design-vue/es/notification";
import { VueAxios } from "./axios";
import { getClusterInfo } from "@/utils/local-cache";

// 创建 axios 实例
const request = axios.create({
  // API 请求的默认前缀
  baseURL: process.env.VUE_APP_API_BASE_URL,
  timeout: 120000, // 请求超时时间
});

// 异常拦截处理器
const errorHandler = (error) => {
  if (error.response) {
    const data = error.response.data;
    notification.error({
      message: error.response.status,
      description: JSON.stringify(data),
    });
  }
  return Promise.reject(error);
};

// request interceptor
request.interceptors.request.use((config) => {
  const clusterInfo = getClusterInfo();
  config.headers["token"] = localStorage.getItem('token');
  if (clusterInfo) {
    config.headers["X-Cluster-Info-Id"] = clusterInfo.id;
    // config.headers["X-Cluster-Info-Name"] = encodeURIComponent(clusterInfo.clusterName);
  }
  return config;
}, errorHandler);

// response interceptor
request.interceptors.response.use((response) => {
  if (response.data.code === -5000){
    router.push({ path:'/'})
    return
  }
  return response.data;
}, errorHandler);

const installer = {
  vm: {},
  install(Vue) {
    Vue.use(VueAxios, request);
  },
};

export default request;

export { installer as VueAxios, request as axios };

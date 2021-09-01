import axios from "axios";
import notification from "ant-design-vue/es/notification";
import { VueAxios } from "./axios";

// 创建 axios 实例
const request = axios.create({
  // API 请求的默认前缀
  baseURL: "/kafka-console",
  timeout: 10000, // 请求超时时间
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
// request.interceptors.request.use(config => {
//
//   return config
// }, errorHandler)

// response interceptor
request.interceptors.response.use((response) => {
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

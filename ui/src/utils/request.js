import axios from "axios";
import notification from "ant-design-vue/es/notification";
import { VueAxios } from "./axios";
import { getClusterInfo } from "@/utils/local-cache";
import Router from "@/router";
// 创建 axios 实例
const request = axios.create({
  // API 请求的默认前缀
  baseURL: process.env.VUE_APP_API_BASE_URL,
  timeout: 120000, // 请求超时时间
});

// axios.defaults.headers.common['X-Auth-Token'] = localStorage.getItem('access_token');

// 异常拦截处理器
const errorHandler = (error) => {
  if (error.response) {
    if (error.response.status == 401) {
      notification.error({
        message: error.response.status,
        description: "请登录",
      });
      Router.push({ path: "/login-page" });
    } else if (error.response.status == 403) {
      // const data = error.response.data;
      // notification.error({
      //   message: error.response.status,
      //   description: data.msg,
      // });
    } else {
      const data = error.response.data;
      notification.error({
        message: error.response.status,
        description: JSON.stringify(data),
      });
    }
  }
  return Promise.reject(error);
};

// request interceptor
request.interceptors.request.use((config) => {
  const clusterInfo = getClusterInfo();
  if (clusterInfo) {
    config.headers["X-Cluster-Info-Id"] = clusterInfo.id;
    // config.headers["X-Cluster-Info-Name"] = encodeURIComponent(clusterInfo.clusterName);
  }
  const token = localStorage.getItem("access_token");
  if (token) {
    config.headers["X-Auth-Token"] = token;
  }
  return config;
}, errorHandler);

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

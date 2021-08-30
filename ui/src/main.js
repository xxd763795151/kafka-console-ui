import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
// eslint-disable-next-line no-unused-vars
import {Button} from "ant-design-vue";
import 'ant-design-vue/dist/antd.css';

Vue.config.productionTip = false;
Vue.use(Button)

new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");

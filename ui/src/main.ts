import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import { VueAxios } from './utils/request';
import { setupActionDirective } from '@/directives/action';
import { message } from 'ant-design-vue';

const app = createApp(App);

app.use(Antd);
app.use(store);
app.use(router);
app.use(VueAxios);

setupActionDirective(app);

message.config({
  duration: 1,
  maxCount: 1,
});

app.mount('#app');

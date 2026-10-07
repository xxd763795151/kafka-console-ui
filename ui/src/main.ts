import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import request, { VueAxios } from './utils/request';
import { setupActionDirective } from '@/directives/action';
import { message } from 'ant-design-vue';
import { AuthApi } from '@/utils/api';
import { AUTH } from '@/store/mutation-types';

const app = createApp(App);

app.use(Antd);
app.use(store);
app.use(VueAxios);

setupActionDirective(app);

message.config({
  duration: 1,
  maxCount: 1,
});

async function bootstrap(): Promise<void> {
  try {
    const enableAuth = (await request({
      url: AuthApi.enable.url,
      method: AuthApi.enable.method,
    })) as unknown as boolean;
    store.commit(AUTH.ENABLE, enableAuth);
  } catch {
    // The request interceptor reports the error. Keep the previous fail-open behavior.
  }

  app.use(router);
  app.mount('#app');
}

void bootstrap();

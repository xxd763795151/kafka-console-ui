import type { App } from 'vue';
import type { AxiosInstance } from 'axios';

const VueAxios = {
  vm: {} as Record<string, unknown>,
  installed: false,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  install(app: App, instance?: AxiosInstance): void {
    if (this.installed) {
      return;
    }
    this.installed = true;

    if (!instance) {
      // eslint-disable-next-line no-console
      console.error('You have to install axios');
      return;
    }

    app.config.globalProperties.axios = instance;
    app.config.globalProperties.$http = instance;
    (app as any).axios = instance;
  },
};

export { VueAxios };

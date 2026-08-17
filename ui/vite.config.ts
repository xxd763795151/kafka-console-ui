import { defineConfig, loadEnv, type PluginOption } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'node:path';

const BACKEND_API_PREFIXES = [
  '/user',
  '/acl',
  '/config',
  '/topic',
  '/consumer',
  '/cluster',
  '/op',
  '/message',
  '/client',
  '/sys',
  '/auth',
  '/cluster-role',
];

const createProxyFor = (target: string, ctx: string) => ({
  target,
  changeOrigin: true,
  configure(proxy: any) {
    proxy.on('error', (err: any, req: any) => {
      // eslint-disable-next-line no-console
      console.error('[proxy:error]', ctx, req.method, req.url, err.message);
    });
    proxy.on('proxyReq', (_proxyReq: any, req: any) => {
      // eslint-disable-next-line no-console
      console.info('[proxy:hit]', ctx, '→', target, req.method, req.url);
    });
  },
});

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const target = env.SW_PROXY_TARGET || 'http://127.0.0.1:7766';

  const proxy: Record<string, any> = {};
  for (const prefix of BACKEND_API_PREFIXES) {
    proxy[prefix] = createProxyFor(target, prefix);
  }
  proxy['/kafka-console'] = {
    target,
    changeOrigin: true,
    rewrite: (p: string) => p.replace(/^\/kafka-console/, ''),
    configure(proxy: any) {
      proxy.on('error', (err: any, req: any) => {
        // eslint-disable-next-line no-console
        console.error('[proxy:error] /kafka-console', req.method, req.url, err.message);
      });
      proxy.on('proxyReq', (_proxyReq: any, req: any) => {
        // eslint-disable-next-line no-console
        console.info('[proxy:hit] /kafka-console →', target, req.method, req.url);
      });
    },
  };

  return {
    plugins: [
      vue() as PluginOption,
      {
        name: 'log-env-start',
        configureServer() {
          // eslint-disable-next-line no-console
          console.info('[vite] mode=', mode, ' loaded VITE_API_BASE_URL=', env.VITE_API_BASE_URL, ' proxy target=', target);
        },
      },
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    define: {
      'process.env': {},
    },
    server: {
      proxy,
    },
    build: {
      sourcemap: mode !== 'production',
      outDir: 'dist',
      chunkSizeWarningLimit: 1500,
    },
  };
});

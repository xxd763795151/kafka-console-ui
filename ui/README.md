# kafka-console-ui

## 技术栈
- Vue 3.x
- TypeScript 5.x
- Vite 5.x
- Ant Design Vue 4.x
- Vue Router 4.x
- Vuex 4.x
- Axios 1.x
- dayjs

## Node 版本要求
Node.js >= v24.13.0

## Project setup
```bash
npm install
```

### Compiles and hot-reloads for development
```bash
npm run dev
# 或
npm run serve
```

### Compiles and minifies for production
```bash
npm run build
```

### Type Check
```bash
npx vue-tsc --noEmit
```

### Lints and fixes files
```bash
npm run lint
```

### Customize configuration
See [Vite Configuration Reference](https://vitejs.dev/config/).

### 本地开发代理配置
开发环境下通过 Vite dev server 代理 `/kafka-console` 路径到后端服务：
- 默认为 `http://127.0.0.1:7766`
- 可通过环境变量 `SW_PROXY_TARGET` 自定义后端地址

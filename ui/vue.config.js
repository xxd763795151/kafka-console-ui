module.exports = {
  productionSourceMap: process.env.NODE_ENV !== "production",
  devServer: {
    proxy: {
      "/kafka-console": {
        target: `${process.env.SW_PROXY_TARGET || "http://127.0.0.1:7766"}`,
        changeOrigin: true,
        // pathRewrite: {
        //   '^/kafka-console': '/'
        // }
      },
    },
  },
};

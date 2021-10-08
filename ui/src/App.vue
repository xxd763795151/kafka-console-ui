<template>
  <div id="app">
    <div id="nav">
      <router-link to="/" class="pad-l-r">主页</router-link>
      <span>|</span
      ><router-link to="/cluster-page" class="pad-l-r">集群</router-link>
      <span>|</span
      ><router-link to="/topic-page" class="pad-l-r">Topic</router-link>
      <span>|</span
      ><router-link to="/group-page" class="pad-l-r">消费组</router-link>
      <span v-show="config.enableAcl">|</span
      ><router-link to="/acl-page" class="pad-l-r" v-show="config.enableAcl"
        >Acl</router-link
      >
      <span>|</span
      ><router-link to="/op-page" class="pad-l-r">运维</router-link>
    </div>
    <router-view class="content" />
  </div>
</template>
<script>
import { KafkaConfigApi } from "@/utils/api";
import request from "@/utils/request";

export default {
  data() {
    return {
      config: {},
    };
  },
  created() {
    request({
      url: KafkaConfigApi.getConfig.url,
      method: KafkaConfigApi.getConfig.method,
    }).then((res) => {
      this.config = res.data;
    });
  },
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#app {
  width: 100%;
  height: 100%;
}

#nav {
  background-color: #9fe0e0;
  font-size: large;
  padding-top: 1%;
  padding-bottom: 1%;
  margin-bottom: 1%;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #61c126;
}

.pad-l-r {
  padding-left: 10px;
  padding-right: 10px;
}

.content {
  padding-left: 2%;
  padding-right: 2%;
  height: 90%;
  width: 100%;
}
</style>

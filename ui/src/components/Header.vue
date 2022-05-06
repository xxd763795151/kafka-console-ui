<template>
  <div id="main">
    <div id="nav">
      <h2 class="logo">Kafka 控制台</h2>
      <span v-show="manager">
        <router-link to="/home" class="pad-l-r">主页</router-link>
        <span>|</span>
      </span>
      <span>
        <router-link to="/cluster-page" class="pad-l-r">集群</router-link>
      </span>
      <span>
        <span>|</span>
        <router-link to="/topic-page" class="pad-l-r">Topic</router-link>
      </span>
      <span>
        <span>|</span>
        <router-link to="/group-page" class="pad-l-r">消费组</router-link>
      </span>
      <span>
        <span>|</span>
        <router-link to="/message-page" class="pad-l-r">消息</router-link>
      </span>
      <span v-show="manager && enableSasl">
        <span>|</span>
        <router-link to="/acl-page" class="pad-l-r">Acl</router-link>
      </span>
      <span>
        <span>|</span>
        <router-link to="/op-page" class="pad-l-r">运维</router-link>
      </span>
      <span v-show="manager">
        <span>|</span>
        <router-link to="/devops/user" class="pad-l-r">用户</router-link>
      </span>

      <span class="right">
        <span>集群：{{ clusterName }}</span>
        <span> | </span>
        <span @click="logout" style="cursor: pointer">登出</span>
      </span>
    </div>
    <router-view class="content" />
  </div>
</template>
<script>
import { KafkaClusterApi } from "@/utils/api";
import request from "@/utils/request";
import { mapMutations, mapState } from "vuex";
import { getClusterInfo } from "@/utils/local-cache";
import notification from "ant-design-vue/lib/notification";
import { CLUSTER } from "@/store/mutation-types";
import {isManager} from "../utils/role";
import router from "../router";

export default {
  name: "Header",
  data() {
    return {
      manager: isManager(),
      config: {},
    };
  },
  created() {
    const clusterInfo = getClusterInfo();
    if (!clusterInfo) {
      request({
        url: KafkaClusterApi.peekClusterInfo.url,
        method: KafkaClusterApi.peekClusterInfo.method,
      }).then((res) => {
        if (res.code == 0) {
          this.switchCluster(res.data);
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    } else {
      this.switchCluster(clusterInfo);
    }
  },
  computed: {
    ...mapState({
      clusterName: (state) => state.clusterInfo.clusterName,
      enableSasl: (state) => state.clusterInfo.enableSasl,
    }),
  },
  methods: {
    ...mapMutations({
      switchCluster: CLUSTER.SWITCH,
    }),
    logout: function (){
      localStorage.clear();
      router.push("/")
    }
  },
};
</script>

<style>
#main {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

#main {
  width: 100%;
  height: 100%;
}

#nav {
  background-color: #9fe0e0;
  font-size: large;
  padding-top: 1%;
  padding-bottom: 1%;
  margin-bottom: 1%;
  text-align: center;
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
.logo {
  float: left;
  left: 1%;
  top: 1%;
  position: absolute;
}
.cluster {
  float: right;
  right: 8%;
  top: 2%;
  position: absolute;
}
.right {
  float: right;
  right: 2%;
  top: 2%;
  position: absolute;
}
</style>
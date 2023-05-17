<template>
  <div id="app">
    <div id="nav">
      <h2 class="logo">Kafka 控制台</h2>
      <router-link to="/" class="pad-l-r">主页</router-link>
      <span>|</span
      ><router-link to="/cluster-page" class="pad-l-r">集群</router-link>
      <span>|</span
      ><router-link to="/topic-page" class="pad-l-r">Topic</router-link>
      <span>|</span
      ><router-link to="/group-page" class="pad-l-r">消费组</router-link>
      <span>|</span
      ><router-link to="/message-page" class="pad-l-r">消息</router-link>
      <span>|</span
      ><router-link to="/client-quota-page" class="pad-l-r">限流</router-link>
      <span>|</span
      ><router-link to="/acl-page" class="pad-l-r">Acl</router-link>
      <span v-show="showUserMenu">|</span
      ><router-link to="/user-page" class="pad-l-r" v-show="showUserMenu"
        >用户</router-link
      >
      <span>|</span
      ><router-link to="/op-page" class="pad-l-r">运维</router-link>
      <div class="right">
        <span>集群：{{ clusterName }} </span>
        <a-dropdown v-show="showUsername">
          <span>
            <span> | </span><a-icon type="smile"></a-icon>
            <span>{{ username }}</span>
          </span>
          <a-menu slot="overlay">
            <a-menu-item key="1">
              <a href="javascript:;" @click="logout">
                <!--              <a-icon type="logout"/>-->
                <span>退出</span>
              </a>
            </a-menu-item>
          </a-menu>
        </a-dropdown>
      </div>
    </div>
    <router-view class="content" />
  </div>
</template>
<script>
import { KafkaClusterApi, AuthApi } from "@/utils/api";
import request from "@/utils/request";
import { mapMutations, mapState } from "vuex";
import {
  deleteToken,
  deleteUsername,
  getClusterInfo,
  getPermissions,
  getUsername,
} from "@/utils/local-cache";
import notification from "ant-design-vue/lib/notification";
import { AUTH, CLUSTER } from "@/store/mutation-types";

export default {
  data() {
    return {
      config: {},
    };
  },
  created() {
    this.intAuthState();
    this.initClusterInfo();
  },
  computed: {
    ...mapState({
      clusterName: (state) => state.clusterInfo.clusterName,
      enableSasl: (state) => state.clusterInfo.enableSasl,
      showUsername: (state) => state.auth.enable && state.auth.username,
      username: (state) => state.auth.username,
      showUserMenu: (state) => state.auth.enable,
    }),
  },
  methods: {
    ...mapMutations({
      switchCluster: CLUSTER.SWITCH,
      enableAuth: AUTH.ENABLE,
      setUsername: AUTH.SET_USERNAME,
      setPermissions: AUTH.SET_PERMISSIONS,
    }),
    beforeLoadFn() {
      this.setUsername(getUsername());
      this.setPermissions(getPermissions());
    },
    intAuthState() {
      request({
        url: AuthApi.enable.url,
        method: AuthApi.enable.method,
      }).then((res) => {
        const enable = res;
        this.enableAuth(enable);
        // if (!enable){
        //   this.initClusterInfo();
        // }
      });
    },
    initClusterInfo() {
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
    logout() {
      deleteToken();
      deleteUsername();
      this.$router.push("/login-page");
    },
  },
  mounted() {
    this.beforeLoadFn();
  },
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
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
.right {
  float: right;
  right: 1%;
  top: 2%;
  position: absolute;
}
</style>

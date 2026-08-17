<template>
  <div id="app">
    <div id="nav">
      <h2 class="logo">Kafka 控制台</h2>
      <router-link to="/" class="pad-l-r">主页</router-link>
      <span>|</span>
      <router-link to="/cluster-page" class="pad-l-r">集群</router-link>
      <span>|</span>
      <router-link to="/topic-page" class="pad-l-r">Topic</router-link>
      <span>|</span>
      <router-link to="/group-page" class="pad-l-r">消费组</router-link>
      <span>|</span>
      <router-link to="/message-page" class="pad-l-r">消息</router-link>
      <span>|</span>
      <router-link to="/client-quota-page" class="pad-l-r">限流</router-link>
      <span>|</span>
      <router-link to="/acl-page" class="pad-l-r">Acl</router-link>
      <span v-show="showUserMenu">|</span>
      <router-link to="/user-page" class="pad-l-r" v-show="showUserMenu">用户</router-link>
      <span>|</span>
      <router-link to="/op-page" class="pad-l-r">运维</router-link>
      <div class="right">
        <span>集群：{{ clusterName }} </span>
        <span v-show="showUsername">
          <a-dropdown>
            <span class="user-dropdown-trigger">
              <span> | </span><SmileOutlined />
              <span>{{ username }}</span>
            </span>
            <template #overlay>
              <a-menu>
                <a-menu-item key="1">
                  <a href="javascript:;" @click="logout">
                    <span>退出</span>
                  </a>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </span>
      </div>
    </div>
    <router-view class="content" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { SmileOutlined } from '@ant-design/icons-vue';
import { KafkaClusterApi, AuthApi } from '@/utils/api';
import request from '@/utils/request';
import { mapMutations, mapState } from 'vuex';
import {
  deleteToken,
  deleteUsername,
  getClusterInfo,
  getPermissions,
  getUsername,
} from '@/utils/local-cache';
import { notification } from 'ant-design-vue';
import { AUTH, CLUSTER } from '@/store/mutation-types';

export default defineComponent({
  name: 'App',
  components: { SmileOutlined },
  data() {
    return {
      config: {} as Record<string, any>,
    };
  },
  created() {
    this.intAuthState();
    this.initClusterInfo();
  },
  computed: {
    ...mapState({
      clusterName: (state: any) => state.clusterInfo.clusterName,
      enableSasl: (state: any) => state.clusterInfo.enableSasl,
      showUsername: (state: any) => state.auth.enable && state.auth.username,
      username: (state: any) => state.auth.username,
      showUserMenu: (state: any) => state.auth.enable,
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
      const uname = getUsername();
      if (uname) {
        this.setUsername(uname);
      }
      const perms = getPermissions();
      if (perms) {
        this.setPermissions(perms);
      }
    },
    intAuthState() {
      request({
        url: AuthApi.enable.url,
        method: AuthApi.enable.method,
      }).then((res: any) => {
        const enable = res as boolean;
        this.enableAuth(enable);
      });
    },
    initClusterInfo() {
      const clusterInfo = getClusterInfo();
      if (!clusterInfo) {
        request({
          url: KafkaClusterApi.peekClusterInfo.url,
          method: KafkaClusterApi.peekClusterInfo.method,
        }).then((res: any) => {
          if (res.code == 0) {
            this.switchCluster(res.data);
          } else {
            notification.error({
              message: 'error',
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
      this.$router.push('/login-page');
    },
  },
  mounted() {
    this.beforeLoadFn();
  },
});
</script>

<style>
html,
body,
#app {
  background-color: #fff;
}

.operation-btn.ant-btn-dangerous,
.operation-btn[danger] {
  background-color: #ff4d4f;
  border-color: #ff4d4f;
  color: #fff;
  text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.12);
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.05);
}
.operation-btn.ant-btn-dangerous:hover,
.operation-btn[danger]:hover {
  background-color: #ff7875;
  border-color: #ff7875;
  color: #fff;
}
.operation-btn.ant-btn-dangerous:active,
.operation-btn[danger]:active {
  background-color: #d9363e;
  border-color: #d9363e;
  color: #fff;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
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
  text-decoration: none;
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

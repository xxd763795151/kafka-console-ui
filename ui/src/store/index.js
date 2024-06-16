import Vue from "vue";
import Vuex from "vuex";
import { CLUSTER, AUTH } from "@/store/mutation-types";
import {
  setClusterInfo,
  setPermissions,
  setToken,
  setUsername,
  deleteClusterInfo,
} from "@/utils/local-cache";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    clusterInfo: {
      id: undefined,
      clusterName: undefined,
      enableSasl: false,
    },
    auth: {
      enable: false,
      username: "",
      permissions: [],
    },
  },
  mutations: {
    [CLUSTER.SWITCH](state, clusterInfo) {
      state.clusterInfo.id = clusterInfo.id;
      state.clusterInfo.clusterName = clusterInfo.clusterName;
      let enableSasl = false;
      for (let p in clusterInfo.properties) {
        if (enableSasl) {
          break;
        }
        enableSasl =
          clusterInfo.properties[p].indexOf("security.protocol=SASL") != -1;
      }
      state.clusterInfo.enableSasl = enableSasl;
      setClusterInfo(clusterInfo);
    },
    [CLUSTER.DELETE]() {
      deleteClusterInfo();
    },
    [AUTH.ENABLE](state, enable) {
      state.auth.enable = enable;
    },
    [AUTH.SET_TOKEN](state, info) {
      setToken(info);
    },
    [AUTH.SET_USERNAME](state, username) {
      setUsername(username);
      state.auth.username = username;
    },
    [AUTH.SET_PERMISSIONS](state, permissions) {
      setPermissions(permissions);
      state.auth.permissions = permissions;
    },
  },
  actions: {},
  modules: {},
});

import Vue from "vue";
import Vuex from "vuex";
import { CLUSTER } from "@/store/mutation-types";
import { setClusterInfo } from "@/utils/local-cache";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    clusterInfo: {
      id: undefined,
      clusterName: undefined,
      enableSasl: false,
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
  },
  actions: {},
  modules: {},
});

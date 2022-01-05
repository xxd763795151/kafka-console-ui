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
    },
  },
  mutations: {
    [CLUSTER.SWITCH](state, clusterInfo) {
      state.clusterInfo.id = clusterInfo.id;
      state.clusterInfo.clusterName = clusterInfo.clusterName;
      setClusterInfo(clusterInfo);
    },
  },
  actions: {},
  modules: {},
});

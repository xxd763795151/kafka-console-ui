import { createStore } from 'vuex';
import { CLUSTER, AUTH } from '@/store/mutation-types';
import {
  setClusterInfo,
  setPermissions,
  setToken,
  setUsername,
  deleteClusterInfo,
} from '@/utils/local-cache';
import type { ClusterInfo } from '@/utils/local-cache';
import { setStore } from '@/utils/auth';

export interface AuthState {
  enable: boolean;
  username: string;
  permissions: string | string[];
}

export interface ClusterState {
  id: number | string | undefined;
  clusterName: string | undefined;
  enableSasl: boolean;
}

export interface RootState {
  clusterInfo: ClusterState;
  auth: AuthState;
}

const store = createStore<RootState>({
  state: {
    clusterInfo: {
      id: undefined,
      clusterName: undefined,
      enableSasl: false,
    },
    auth: {
      enable: false,
      username: '',
      permissions: [],
    },
  },
  mutations: {
    [CLUSTER.SWITCH](state, clusterInfo: ClusterInfo) {
      state.clusterInfo.id = clusterInfo.id;
      state.clusterInfo.clusterName = clusterInfo.clusterName;
      let enableSasl = false;
      if (clusterInfo.properties) {
        for (const p in clusterInfo.properties) {
          if (enableSasl) {
            break;
          }
          enableSasl = clusterInfo.properties[p].indexOf('security.protocol=SASL') !== -1;
        }
      }
      state.clusterInfo.enableSasl = enableSasl;
      setClusterInfo(clusterInfo);
    },
    [CLUSTER.DELETE]() {
      deleteClusterInfo();
    },
    [AUTH.ENABLE](state, enable: boolean) {
      state.auth.enable = enable;
    },
    [AUTH.SET_TOKEN](_state, info: string) {
      setToken(info);
    },
    [AUTH.SET_USERNAME](state, username: string) {
      setUsername(username);
      state.auth.username = username;
    },
    [AUTH.SET_PERMISSIONS](state, permissions: string | string[]) {
      setPermissions(permissions);
      state.auth.permissions = permissions;
    },
  },
  actions: {},
  modules: {},
});

setStore(store as any);

export default store;

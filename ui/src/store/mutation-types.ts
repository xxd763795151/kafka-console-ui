export const CLUSTER = {
  SWITCH: 'switchCluster',
  DELETE: 'deleteClusterInfo',
} as const;

export const AUTH = {
  ENABLE: 'enable',
  SET_TOKEN: 'setToken',
  SET_USERNAME: 'setUsername',
  SET_PERMISSIONS: 'setPermissions',
} as const;

export type ClusterMutationTypes = (typeof CLUSTER)[keyof typeof CLUSTER];
export type AuthMutationTypes = (typeof AUTH)[keyof typeof AUTH];

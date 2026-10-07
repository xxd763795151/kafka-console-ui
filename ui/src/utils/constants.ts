export interface ConstantEventType {
  updateUserDialogData: string;
}

export interface CacheType {
  clusterInfo: string;
  auth: string;
  token: string;
  username: string;
  enableAuth: string;
  permissions: string;
}

export const ConstantEvent: ConstantEventType = {
  updateUserDialogData: 'updateUserDialogData',
};

export const Cache: CacheType = {
  clusterInfo: 'clusterInfo',
  auth: 'auth',
  token: 'access_token',
  username: 'login_user',
  enableAuth: 'enable_auth',
  permissions: 'permissions',
};

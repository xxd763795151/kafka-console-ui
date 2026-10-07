import type { Store } from 'vuex';

interface AuthState {
  enable: boolean;
  username: string;
  permissions: string | string[];
}

interface RootState {
  auth: AuthState;
}

let storeInstance: Store<RootState> | null = null;

export function setStore(store: Store<RootState>): void {
  storeInstance = store;
}

export function isUnauthorized(permission: string): boolean {
  if (!storeInstance) return false;
  const enableAuth = storeInstance.state.auth.enable;
  const permissions = storeInstance.state.auth.permissions;
  const permsArr = Array.isArray(permissions) ? permissions : (permissions as string);
  return enableAuth && (!permissions || (permsArr as string[]).indexOf(permission) < 0);
}

export function isAuthorized(permission: string): boolean {
  if (!storeInstance) return true;
  const enableAuth = storeInstance.state.auth.enable;
  if (!enableAuth) {
    return true;
  }
  const permissions = storeInstance.state.auth.permissions;
  const permsArr = Array.isArray(permissions) ? permissions : (permissions as string);
  return !!permissions && (permsArr as string[]).indexOf(permission) >= 0;
}

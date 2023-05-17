import Store from "@/store";

export function isUnauthorized(permission) {
  const enableAuth = Store.state.auth.enable;
  const permissions = Store.state.auth.permissions;
  return enableAuth && (!permissions || permissions.indexOf(permission) < 0);
}

export function isAuthorized(permission) {
  const enableAuth = Store.state.auth.enable;
  if (!enableAuth) {
    return true;
  }
  const permissions = Store.state.auth.permissions;
  return permissions && permissions.indexOf(permission) >= 0;
}

import { Cache } from "@/utils/constants";

export function setClusterInfo(clusterInfo) {
  localStorage.setItem(Cache.clusterInfo, JSON.stringify(clusterInfo));
}

export function deleteClusterInfo() {
  localStorage.removeItem(Cache.clusterInfo);
}

export function getClusterInfo() {
  const str = localStorage.getItem(Cache.clusterInfo);
  return str ? JSON.parse(str) : undefined;
}

// export function setAuth(auth) {
//   localStorage.setItem(Cache.auth, JSON.stringify(auth));
// }

export function setToken(token) {
  localStorage.setItem(Cache.token, token);
}

export function getToken() {
  return localStorage.getItem(Cache.token);
}

export function deleteToken() {
  localStorage.removeItem(Cache.token);
}

export function deleteUsername() {
  localStorage.removeItem(Cache.username);
}

export function setUsername(username) {
  localStorage.setItem(Cache.username, username);
}

export function getUsername() {
  return localStorage.getItem(Cache.username);
}

export function setPermissions(permissions) {
  localStorage.setItem(Cache.permissions, permissions);
}

export function getPermissions() {
  return localStorage.getItem(Cache.permissions);
}

// export function setEnableAuth(enable) {
//   localStorage.setItem()
// }

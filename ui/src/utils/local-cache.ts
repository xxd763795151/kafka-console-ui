import { Cache } from '@/utils/constants';

export interface ClusterInfo {
  id: number | string | undefined;
  clusterName: string | undefined;
  enableSasl?: boolean;
  properties?: Record<string, string>;
}

export function setClusterInfo(clusterInfo: ClusterInfo): void {
  localStorage.setItem(Cache.clusterInfo, JSON.stringify(clusterInfo));
}

export function deleteClusterInfo(): void {
  localStorage.removeItem(Cache.clusterInfo);
}

export function getClusterInfo(): ClusterInfo | undefined {
  const str = localStorage.getItem(Cache.clusterInfo);
  return str ? (JSON.parse(str) as ClusterInfo) : undefined;
}

export function setToken(token: string): void {
  localStorage.setItem(Cache.token, token);
}

export function getToken(): string | null {
  return localStorage.getItem(Cache.token);
}

export function deleteToken(): void {
  localStorage.removeItem(Cache.token);
}

export function deleteUsername(): void {
  localStorage.removeItem(Cache.username);
}

export function setUsername(username: string): void {
  localStorage.setItem(Cache.username, username);
}

export function getUsername(): string | null {
  return localStorage.getItem(Cache.username);
}

export function setPermissions(permissions: string | string[]): void {
  localStorage.setItem(Cache.permissions, permissions as string);
}

export function getPermissions(): string | null {
  return localStorage.getItem(Cache.permissions);
}

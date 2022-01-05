import { Cache } from "@/utils/constants";

export function setClusterInfo(clusterInfo) {
  localStorage.setItem(Cache.clusterInfo, JSON.stringify(clusterInfo));
}

export function getClusterInfo() {
  const str = localStorage.getItem(Cache.clusterInfo);
  return str ? JSON.parse(str) : undefined;
}

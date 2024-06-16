export const KafkaAclApi = {
  addKafkaUser: {
    url: "/user",
    method: "post",
  },
  getKafkaUserDetail: {
    url: "/user/detail",
    method: "get",
  },
  deleteKafkaUser: {
    url: "/user/auth",
    method: "delete",
  },
  getAclList: {
    url: "/acl/list",
    method: "post",
  },
  addProducerAuth: {
    url: "/acl/producer",
    method: "post",
  },
  deleteProducerAuth: {
    url: "/acl/producer",
    method: "delete",
  },
  addConsumerAuth: {
    url: "/acl/consumer",
    method: "post",
  },
  deleteConsumerAuth: {
    url: "/acl/consumer",
    method: "delete",
  },
  getOperationList: {
    url: "/acl/operation/list",
    method: "get",
  },
  addAclAuth: {
    url: "/acl",
    method: "post",
  },
  getAclDetailList: {
    url: "/acl/detail",
    method: "post",
  },
  deleteAcl: {
    url: "/acl",
    method: "delete",
  },
  clearAcl: {
    url: "/acl/clear",
    method: "delete",
  },
  getSaslScramUserList: {
    url: "/user/scram",
    method: "get",
  },
  deleteSaslScramUser: {
    url: "/user",
    method: "delete",
  },
};

export const KafkaConfigApi = {
  getConfig: {
    url: "/config/console",
    method: "get",
  },
  getTopicConfig: {
    url: "/config/topic",
    method: "get",
  },
  getBrokerConfig: {
    url: "/config/broker",
    method: "get",
  },
  getBrokerLoggerConfig: {
    url: "/config/broker/logger",
    method: "get",
  },
  setBrokerConfig: {
    url: "/config/broker",
    method: "post",
  },
  deleteBrokerConfig: {
    url: "/config/broker",
    method: "delete",
  },
  setBrokerLoggerConfig: {
    url: "/config/broker/logger",
    method: "post",
  },
  deleteBrokerLoggerConfig: {
    url: "/config/broker/logger",
    method: "delete",
  },
  setTopicConfig: {
    url: "/config/topic",
    method: "post",
  },
  deleteTopicConfig: {
    url: "/config/topic",
    method: "delete",
  },
};

export const KafkaTopicApi = {
  getTopicNameList: {
    url: "/topic",
    method: "get",
  },
  getTopicList: {
    url: "/topic/list",
    method: "get",
  },
  deleteTopic: {
    url: "/topic",
    method: "delete",
  },
  getPartitionInfo: {
    url: "/topic/partition",
    method: "get",
  },
  creatTopic: {
    url: "/topic/new",
    method: "post",
  },
  addPartition: {
    url: "/topic/partition/new",
    method: "post",
  },
  getCurrentReplicaAssignment: {
    url: "/topic/replica/assignment",
    method: "get",
  },
  updateReplicaAssignment: {
    url: "/topic/replica/assignment",
    method: "post",
  },
  configThrottle: {
    url: "/topic/replica/throttle",
    method: "post",
  },
  sendStats: {
    url: "/topic/send/stats",
    method: "get",
  },
};

export const KafkaConsumerApi = {
  getConsumerGroupList: {
    url: "/consumer/group/list",
    method: "post",
  },
  deleteConsumerGroup: {
    url: "/consumer/group",
    method: "delete",
  },
  getConsumerMembers: {
    url: "/consumer/member",
    method: "get",
  },
  getConsumerDetail: {
    url: "/consumer/detail",
    method: "get",
  },
  addSubscription: {
    url: "/consumer/subscription",
    method: "post",
  },
  resetOffset: {
    url: "/consumer/reset/offset",
    method: "post",
  },
  getGroupIdList: {
    url: "/consumer/group/id/list",
    method: "get",
  },
  getSubscribeTopicList: {
    url: "/consumer/topic/list",
    method: "get",
  },
  getTopicSubscribedByGroups: {
    url: "/consumer/topic/subscribed",
    method: "get",
  },
  getOffsetPartition: {
    url: "/consumer/offset/partition",
    method: "get",
  },
};

export const KafkaClusterApi = {
  getClusterInfo: {
    url: "/cluster",
    method: "get",
  },
  getClusterInfoList: {
    url: "/cluster/info",
    method: "get",
  },
  getClusterInfoListForSelect: {
    url: "/cluster/info/select",
    method: "get",
  },
  addClusterInfo: {
    url: "/cluster/info",
    method: "post",
  },
  deleteClusterInfo: {
    url: "/cluster/info",
    method: "delete",
  },
  updateClusterInfo: {
    url: "/cluster/info",
    method: "put",
  },
  peekClusterInfo: {
    url: "/cluster/info/peek",
    method: "get",
  },
  getBrokerApiVersionInfo: {
    url: "/cluster/info/api/version",
    method: "get",
  },
};

export const KafkaOpApi = {
  syncConsumerOffset: {
    url: "/op/sync/consumer/offset",
    method: "post",
  },
  minOffsetAlignment: {
    url: "/op/sync/min/offset/alignment",
    method: "post",
  },
  getOffsetAlignmentList: {
    url: "/op/sync/alignment/list",
    method: "get",
  },
  deleteAlignment: {
    url: "/op/sync/alignment",
    method: "delete",
  },
  electPreferredLeader: {
    url: "/op/replication/preferred",
    method: "post",
  },
  configThrottle: {
    url: "/op/broker/throttle",
    method: "post",
  },
  removeThrottle: {
    url: "/op/broker/throttle",
    method: "delete",
  },
  currentReassignments: {
    url: "/op/replication/reassignments",
    method: "get",
  },
  cancelReassignment: {
    url: "/op/replication/reassignments",
    method: "delete",
  },
  proposedAssignment: {
    url: "/op/replication/reassignments/proposed",
    method: "post",
  },
};
export const KafkaMessageApi = {
  searchByTime: {
    url: "/message/search/time",
    method: "post",
  },
  searchByOffset: {
    url: "/message/search/offset",
    method: "post",
  },
  searchDetail: {
    url: "/message/search/detail",
    method: "post",
  },
  deserializerList: {
    url: "/message/deserializer/list",
    method: "get",
  },
  send: {
    url: "/message/send",
    method: "post",
  },
  resend: {
    url: "/message/resend",
    method: "post",
  },
  delete: {
    url: "/message",
    method: "delete",
  },
  sendStatistics: {
    url: "/message/send/statistics",
    method: "post",
  },
};

export const KafkaClientQuotaApi = {
  getClientQuotaConfigs: {
    url: "/client/quota/list",
    method: "post",
  },
  alterClientQuotaConfigs: {
    url: "/client/quota",
    method: "post",
  },
  deleteClientQuotaConfigs: {
    url: "/client/quota",
    method: "delete",
  },
};

export const UserManageApi = {
  getPermissions: {
    url: "/sys/user/manage/permission",
    method: "get",
  },
  addPermission: {
    url: "/sys/user/manage/permission",
    method: "post",
  },
  getRole: {
    url: "/sys/user/manage/role",
    method: "get",
  },
  addOrUpdateRole: {
    url: "/sys/user/manage/role",
    method: "post",
  },
  deleteRole: {
    url: "/sys/user/manage/role",
    method: "delete",
  },
  getUsers: {
    url: "/sys/user/manage/user",
    method: "get",
  },
  addOrUpdateUser: {
    url: "/sys/user/manage/user",
    method: "post",
  },
  deleteUser: {
    url: "/sys/user/manage/user",
    method: "delete",
  },
  updatePassword: {
    url: "/sys/user/manage/user/password",
    method: "post",
  },
};

export const AuthApi = {
  enable: {
    url: "/auth/enable",
    method: "get",
  },
  login: {
    url: "/auth/login",
    method: "post",
  },
  ownDataAuthority: {
    url: "/auth/own/data/auth",
    method: "get",
  },
};

export const ClusterRoleRelationApi = {
  select: {
    url: "/cluster-role/relation",
    method: "get",
  },
  add: {
    url: "/cluster-role/relation",
    method: "post",
  },
  delete: {
    url: "/cluster-role/relation",
    method: "delete",
  },
};

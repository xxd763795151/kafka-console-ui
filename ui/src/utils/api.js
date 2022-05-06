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

export const DevOpsUserAPi = {
  createUser: {
    url: "/devops/user/add",
    method: "post",
  },
  userList: {
    url: "/devops/user/list",
    method: "get",
  },
  deleteUser: {
    url: "/devops/user/",
    method: "delete",
  },
  updateUser: {
    url: "/devops/user/update",
    method: "post",
  },
  login: {
    url: "/devops/user/login",
    method: "post",
  },
}

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
};

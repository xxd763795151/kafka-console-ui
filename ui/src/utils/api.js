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
    url: "/config",
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
};

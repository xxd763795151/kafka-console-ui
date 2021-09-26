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
};

export const KafkaTopicApi = {
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
};

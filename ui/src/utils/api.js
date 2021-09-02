export const KafkaAclApi = {
  addKafkaUser: {
    url: "/user",
    method: "post",
  },
  deleteKafkaUser: {
    url: "/user/auth",
    method: "delete",
  },
  getAclList: {
    url: "/acl/list",
    method: "post",
  },
};

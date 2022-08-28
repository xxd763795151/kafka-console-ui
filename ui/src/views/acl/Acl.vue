<template>
  <div class="content">
    <a-tabs default-active-key="1" size="large" tabPosition="top">
      <a-tab-pane key="1" tab="资源授权">
        <acl-list></acl-list>
      </a-tab-pane>
      <a-tab-pane key="2" tab="SaslScram用户管理">
        <div v-show="enableSasl">
          <sasl-scram></sasl-scram>
        </div>
        <div v-show="!enableSasl">
          <h2>未启用SASL SCRAM认证，不支持该认证的用户管理操作</h2>
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
import AclList from "@/views/acl/AclList";
import SaslScram from "@/views/acl/SaslScram";
import { mapState } from "vuex";

export default {
  name: "Acl",
  components: {
    AclList,
    SaslScram,
  },
  computed: {
    ...mapState({
      enableSasl: (state) => state.clusterInfo.enableSasl,
    }),
  },
};
</script>

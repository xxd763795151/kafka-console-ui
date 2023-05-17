<template>
  <div class="content">
    <a-tabs default-active-key="1" size="large" tabPosition="top">
      <a-tab-pane key="1" tab="资源授权" v-if="isAuthorized('acl:authority')">
        <acl-list></acl-list>
      </a-tab-pane>
      <a-tab-pane
        key="2"
        tab="SaslScram用户管理"
        v-if="isAuthorized('acl:sasl-scram')"
      >
        <sasl-scram></sasl-scram>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
import AclList from "@/views/acl/AclList";
import SaslScram from "@/views/acl/SaslScram";
import { isAuthorized } from "@/utils/auth";

export default {
  name: "Acl",
  methods: { isAuthorized },
  components: {
    AclList,
    SaslScram,
  },
};
</script>

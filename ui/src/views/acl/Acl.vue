<template>
  <div class="content">
    <a-tabs default-active-key="1" size="large" tabPosition="top">
      <a-tab-pane key="1" tab="资源授权">
        <acl-list></acl-list>
      </a-tab-pane>
      <a-tab-pane key="2" tab="SaslScram用户管理">
        <sasl-scram></sasl-scram>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
import AclList from "@/views/acl/AclList";
import SaslScram from "@/views/acl/SaslScram";

export default {
  name: "Acl",
  components: {
    AclList,
    SaslScram,
  },
};
</script>

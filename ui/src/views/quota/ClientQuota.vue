<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-tabs default-active-key="1" size="large" tabPosition="top">
        <a-tab-pane key="1" tab="使用说明">
        </a-tab-pane>
        <a-tab-pane key="2" tab="用户">
          <UserQuota></UserQuota>
        </a-tab-pane>
        <a-tab-pane key="3" tab="客户端ID">
          <ClientIDQuota></ClientIDQuota>
        </a-tab-pane>
        <a-tab-pane key="4" tab="用户_客户端ID">
          <UserAndClientIDQuota></UserAndClientIDQuota>
        </a-tab-pane>
        <!--        <a-tab-pane key="5" tab="IP">-->
        <!--          <IpQuota></IpQuota>-->
        <!--        </a-tab-pane>-->
      </a-tabs>
    </a-spin>
  </div>
</template>

<script>
import ClientIDQuota from "@/views/quota/ClientIDQuota.vue";
import UserQuota from "@/views/quota/UserQuota.vue";
import UserAndClientIDQuota from "@/views/quota/UserAndClientIDQuota.vue";

export default {
  name: "ClientQuota",
  components: {ClientIDQuota, UserQuota, UserAndClientIDQuota},
  data() {
    return {
      loading: false,
    };
  },
};
</script>

<style scoped></style>

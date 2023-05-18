<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-tabs default-active-key="1" size="large" tabPosition="top">
        <a-tab-pane key="1" tab="使用说明">
          <ClientQuotaIntroduce></ClientQuotaIntroduce>
        </a-tab-pane>
        <a-tab-pane v-if="isAuthorized('quota:user')" key="2" tab="用户">
          <UserQuota></UserQuota>
        </a-tab-pane>
        <a-tab-pane key="3" tab="客户端ID" v-if="isAuthorized('quota:client')">
          <ClientIDQuota></ClientIDQuota>
        </a-tab-pane>
        <a-tab-pane
          key="4"
          tab="用户_客户端ID"
          v-if="isAuthorized('quota:user-client')"
        >
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
import ClientQuotaIntroduce from "@/views/quota/ClientQuotaIntroduce.vue";
import { isAuthorized } from "@/utils/auth";

export default {
  name: "ClientQuota",
  methods: { isAuthorized },
  components: {
    ClientIDQuota,
    UserQuota,
    UserAndClientIDQuota,
    ClientQuotaIntroduce,
  },
  data() {
    return {
      loading: false,
    };
  },
};
</script>

<style scoped></style>

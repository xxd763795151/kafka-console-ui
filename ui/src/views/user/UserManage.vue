<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-tabs default-active-key="1" size="large" tabPosition="top">
        <a-tab-pane
          key="1"
          tab="用户列表"
          v-if="isAuthorized('user-manage:user')"
        >
          <User></User>
        </a-tab-pane>
        <a-tab-pane
          key="2"
          tab="角色列表"
          v-if="isAuthorized('user-manage:role')"
        >
          <Role></Role>
        </a-tab-pane>
        <a-tab-pane
          key="3"
          tab="权限列表"
          v-if="isAuthorized('user-manage:permission')"
        >
          <Permission></Permission>
        </a-tab-pane>
        <a-tab-pane
          key="4"
          tab="个人设置"
          v-if="isAuthorized('user-manage:setting')"
        >
          <UserSetting></UserSetting>
        </a-tab-pane>
      </a-tabs>
    </a-spin>
  </div>
</template>

<script>
import Permission from "@/views/user/Permission.vue";
import Role from "@/views/user/Role.vue";
import User from "@/views/user/User.vue";
import UserSetting from "@/views/user/UserSetting.vue";
import { isAuthorized } from "@/utils/auth";
export default {
  name: "UserManage",
  components: { Permission, Role, User, UserSetting },
  data() {
    return {
      loading: false,
      topicList: [],
    };
  },
  methods: { isAuthorized },
  created() {},
};
</script>

<style scoped></style>

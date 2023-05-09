<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-tabs default-active-key="1" size="large" tabPosition="top">
        <a-tab-pane key="1" tab="用户列表">
          <User></User>
        </a-tab-pane>
        <a-tab-pane key="2" tab="角色列表">
          <Role></Role>
        </a-tab-pane>
        <a-tab-pane key="3" tab="权限列表">
          <Permission></Permission>
        </a-tab-pane>
        <a-tab-pane key="4" tab="个人设置">
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
export default {
  name: "UserManage",
  components: { Permission, Role, User, UserSetting },
  data() {
    return {
      loading: false,
      topicList: [],
    };
  },
  methods: {},
  created() {},
};
</script>

<style scoped></style>

<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-tabs default-active-key="1" size="large" tabPosition="top">
        <a-tab-pane key="1" tab="用户列表"> </a-tab-pane>
        <a-tab-pane key="2" tab="角色列表"> </a-tab-pane>
        <a-tab-pane key="3" tab="权限列表">
          <Permission></Permission>
        </a-tab-pane>
        <a-tab-pane key="4" tab="个人设置"> </a-tab-pane>
      </a-tabs>
    </a-spin>
  </div>
</template>

<script>
import Permission from "@/views/user/Permission.vue";
export default {
  name: "UserManage",
  components: { Permission },
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

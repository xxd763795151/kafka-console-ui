<template>
  <a-popconfirm
    :title="title"
    ok-text="确认"
    cancel-text="取消"
    @confirm="confirm"
    @cancel="cancel"
  >
  </a-popconfirm>
</template>
<script>
export default {
  name: "ConfirmDialog",
  props: {
    title: {
      type: String,
      default: "",
    },
    confirmEvent: {
      default: "",
    },
  },
  methods: {
    confirm(e) {
      console.log(e);
      this.$message.success("Click on Yes");
    },
    cancel(e) {
      console.log(e);
      this.$message.error("Click on No");
    },
  },
};
</script>

<style scoped></style>

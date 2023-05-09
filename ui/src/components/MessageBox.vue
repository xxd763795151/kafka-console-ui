<template>
  <a-modal
    title="消息提示"
    :visible="visible"
    :width="400"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      {{ message }}
    </div>
  </a-modal>
</template>

<script>
export default {
  name: "MessageBox",
  props: {
    message: {
      type: String,
      default: "",
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    handleCancel() {
      this.visible = false;
      this.$emit("closeMessageBox", {});
    },
  },
};
</script>

<style scoped></style>

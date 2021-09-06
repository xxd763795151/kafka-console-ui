<template>
  <a-modal
    title="用户详情"
    :visible="show"
    :width="800"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    @cancel="handleCancel"
  >
  </a-modal>
</template>

<script>
// import { KafkaAclApi } from "@/utils/api";
// import request from "@/utils/request";

export default {
  name: "UserDetail",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    // KafkaAclApi, request,
  },
  data() {
    return {
      formLayout: "horizontal",
      show: this.visible,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getAclDetail();
      }
    },
  },
  methods: {
    handleCancel() {
      this.$emit("userDetailDialog", {});
    },
  },
};
</script>

<style scoped></style>

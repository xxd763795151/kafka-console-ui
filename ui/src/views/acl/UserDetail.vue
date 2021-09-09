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
    <a-form :form="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
      <a-form-item label="用户名">
        <span>{{ user.username }}</span>
      </a-form-item>
      <a-form-item label="密码">
        <span>{{ user.password }}</span>
      </a-form-item>
      <a-form-item label="凭证信息">
        <span>{{ user.credentialInfos }}</span>
      </a-form-item>
      <a-form-item label="数据一致性说明">
        <strong>{{ user.consistencyDescription }}</strong>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
import { KafkaAclApi } from "@/utils/api";
import request from "@/utils/request";

export default {
  name: "UserDetail",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    username: {
      type: String,
    },
  },
  components: {},
  data() {
    return {
      formLayout: "horizontal",
      show: this.visible,
      form: this.$form.createForm(this, { name: "UserDetailForm" }),
      user: {},
    };
  },
  watch: {
    visible(n, o) {
      this.show = n;
      if (n != o && this.show) {
        this.getUserDetail();
      }
    },
  },
  methods: {
    handleCancel() {
      this.$emit("userDetailDialog", {});
    },
    getUserDetail() {
      const api = KafkaAclApi.getKafkaUserDetail;
      request({
        url: api.url,
        method: api.method,
        params: { username: this.username },
      }).then((res) => {
        if (res.code != 0) {
          this.$message.error(res.msg);
        } else {
          this.user = res.data;
        }
      });
    },
  },
};
</script>

<style scoped></style>

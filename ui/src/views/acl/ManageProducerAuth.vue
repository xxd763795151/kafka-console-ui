<template>
  <a-modal
    title="管理生产权限"
    :visible="show"
    :confirm-loading="confirmLoading"
    :width="800"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="提交"
    cancelText="取消"
    :mask="false"
    :destroyOnClose="true"
  >
    <a-form :form="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
      <a-form-item label="用户名">
        <a-input
          v-decorator="['username', { initialValue: record.username }]"
          disabled="disabled"
        />
      </a-form-item>
      <a-form-item label="topic">
        <a-input
          v-decorator="[
            'topic',
            { rules: [{ required: true, message: '请输入topic!' }] },
          ]"
        />
      </a-form-item>
      <a-form-item label="类型">
        <a-radio-group v-decorator="['type', { initialValue: 'grant' }]">
          <a-radio value="grant"> 授予 </a-radio>
          <a-radio value="revoke"> 收回 </a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
import { KafkaAclApi } from "@/utils/api";
import request from "@/utils/request";
export default {
  name: "ManageProducerAuth",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    record: {
      default: {},
    },
  },
  data() {
    return {
      formLayout: "horizontal",
      form: this.$form.createForm(this, { name: "AddProducerAuthForm" }),
      confirmLoading: false,
      show: this.visible,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
    },
  },
  methods: {
    handleOk() {
      const form = this.form;
      form.validateFields((e, v) => {
        if (e) {
          return;
        }
        const param = { username: v.username, topic: v.topic };
        const api = {};
        switch (v.type) {
          case "grant":
            Object.assign(api, KafkaAclApi.addProducerAuth);
            break;
          case "revoke":
            Object.assign(api, KafkaAclApi.deleteProducerAuth);
            break;
          default:
            this.$message.error("unknown error");
            return;
        }

        this.confirmLoading = true;
        request({
          url: api.url,
          method: api.method,
          data: param,
        }).then((res) => {
          this.confirmLoading = false;
          if (res.code == 0) {
            this.$message.success(res.msg);
            this.$emit("manageProducerAuthDialog", v);
          } else {
            this.$message.error(res.msg);
          }
        });
      });
    },
    handleCancel() {
      this.$emit("manageProducerAuthDialog", {});
    },
  },
};
</script>

<style scoped></style>

<template>
  <a-modal
    title="重置密码"
    :visible="show"
    :width="800"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :form="form"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @submit="handleSubmit"
        >
          <a-form-item label="账号">
            <a-input
              :disabled="true"
              v-decorator="['username', { initialValue: username }]"
              placeholder="username"
            />
          </a-form-item>
          <a-form-item label="密码">
            <a-input
                v-decorator="[
                'password',
                { rules: [{ required: true, message: '请输入密码!' }] },
              ]"
                placeholder="请输入密码"
            />
          </a-form-item>

          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 提交 </a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { DevOpsUserAPi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
export default {
  name: "UpdatePassword",
  props: {
    username: {
      type: String,
      default: "",
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: this.visible,
      data: [],
      loading: false,
      form: this.$form.createForm(this, { name: "coordinated" }),
    };
  },
  watch: {
    visible(v) {
      this.show = v;
    },
  },
  methods: {
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          if (values.assignment) {
            const assignment = {};
            values.assignment.split("\n").forEach((e) => {
              const c = e.split("=");
              if (c.length > 1) {
                let k = c[0];
                let v = c[1];
                let arr = v.split(",");
                if (arr.length > 0) {
                  assignment[k] = arr;
                }
              }
            });
            values.assignment = assignment;
          }
          this.loading = true;
          request({
            url: DevOpsUserAPi.updateUser.url,
            method: DevOpsUserAPi.updateUser.method,
            data: values,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeResetPasswordDialog", { refresh: true });
            } else {
              notification.error({
                message: "error",
                description: res.msg,
              });
            }
          });
        }
      });
    },
    handleCancel() {
      this.data = [];
      this.$emit("closeResetPasswordDialog", { refresh: false });
    },
  },
};
</script>

<style scoped></style>

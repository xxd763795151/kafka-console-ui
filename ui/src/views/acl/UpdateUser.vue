<template>
  <a-modal
    title="新增/更新用户"
    :visible.sync="show"
    :confirm-loading="confirmLoading"
    :width="800"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="提交"
    cancelText="取消"
    :mask="true"
  >
    <div>
      <a-form layout="vertical" :form="this.form">
        <!--每一项元素-->
        <a-form-item label="用户名">
          <a-input
            placeholder="username"
            :allowClear="true"
            :maxLength="30"
            v-decorator="[
              'username',
              {
                rules: [{ required: true, message: '请填写用户名!' }],
              },
            ]"
          />
        </a-form-item>
        <a-form-item label="密码">
          <a-input
            placeholder="password"
            :allowClear="true"
            :maxLength="30"
            v-decorator="[
              'password',
              {
                rules: [{ required: true, message: '请填写密码!' }],
              },
            ]"
          />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import notification from "ant-design-vue/es/notification";
import { KafkaAclApi } from "@/utils/api";

export default {
  name: "UpdateUser",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  beforeCreate() {
    //创建表单
    this.form = this.$form.createForm(this, { name: "addOrUpdateUserModal" });
  },
  data() {
    return {
      ModalText: "Content of the modal",
      confirmLoading: false,
      show: this.visible,
    };
  },
  watch: {
    visible(val) {
      this.show = val;
    },
  },
  methods: {
    handleOk() {
      const form = this.form;
      form.validateFields((err, values) => {
        if (err) {
          return;
        }
        this.confirmLoading = true;
        request({
          url: KafkaAclApi.addKafkaUser.url,
          method: KafkaAclApi.addKafkaUser.method,
          data: { username: values.username, password: values.password },
        }).then((res) => {
          this.confirmLoading = false;
          if (res.code == 0) {
            notification.success({
              message: res.msg,
            });
            form.resetFields();
            this.$emit("updateUserDialogData", { ok: true, show: false });
          } else {
            notification.error({
              message: res.msg,
            });
          }
        });
      });
    },
    handleCancel() {
      this.$emit("updateUserDialogData", { ok: false, show: false });
    },
  },
};
</script>

<style scoped>
.input-c {
  margin-bottom: 1%;
}
</style>

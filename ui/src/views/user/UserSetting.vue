<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-form :form="form" @submit="handleSubmit">
        <a-form-item label="新密码">
          <a-input-password
            v-decorator="[
              'password',
              { rules: [{ required: true, message: '请输入密码' }] },
            ]"
          />
        </a-form-item>
        <a-form-item label="确认密码">
          <a-input-password
            v-decorator="[
              'confirmPassword',
              {
                rules: [
                  {
                    required: true,
                    message: '两次密码不一致',
                    pattern: new RegExp(
                      '^' + form.getFieldValue('password') + '$'
                    ),
                  },
                ],
              },
            ]"
          />
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
          <a-button type="primary" html-type="submit"> 提交 </a-button>
        </a-form-item>
      </a-form>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { UserManageApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

export default {
  name: "UserSetting",
  components: {},
  props: {
    topicList: {
      type: Array,
    },
  },
  data() {
    return {
      form: this.$form.createForm(this, { name: "user_setting" }),
      loading: false,
    };
  },
  methods: {
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          const param = Object.assign({}, values);
          this.loading = true;
          request({
            url: UserManageApi.updatePassword.url,
            method: UserManageApi.updatePassword.method,
            data: param,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
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
  },
};
</script>

<style scoped>
.content {
  padding-left: 30%;
  padding-right: 30%;
}
</style>

<template>
  <a-form
    :form="form"
    :label-col="{ span: 5 }"
    :wrapper-col="{ span: 12 }"
    @submit="handleSubmit"
    class="login-box"
  >
    <h3 class="login-title">登录kafka-console-ui</h3>
    <a-form-item label="账号">
      <a-input
        style="width: 200px"
        allowClear
        v-decorator="[
          'username',
          { rules: [{ required: true, message: '请输入账号' }] },
        ]"
      >
      </a-input>
    </a-form-item>
    <a-form-item label="密码">
      <a-input-password
        style="width: 200px"
        v-decorator="[
          'password',
          { rules: [{ required: true, message: '请输入密码' }] },
        ]"
      />
    </a-form-item>
    <a-form-item :wrapper-col="{ span: 16, offset: 5 }">
      <a-button type="primary" @click="handleSubmit" :loading="loading"
        >登录</a-button
      >
    </a-form-item>
  </a-form>
</template>

<script>
import request from "@/utils/request";
import {AuthApi} from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import {mapMutations} from "vuex";
import {AUTH} from "@/store/mutation-types";

export default {
  name: "Login",
  data() {
    return {
      form: this.$form.createForm(this, { name: "login-form" }),
      loading: false,
    };
  },
  methods: {
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          this.loading = true;
          const params = Object.assign({}, values);
          request({
            url: AuthApi.login.url,
            method: AuthApi.login.method,
            data: params,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.setToken(res.data.token);
              this.setUsername(params.username);
              this.$router.push("/");
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
    ...mapMutations({
      setToken: AUTH.SET_TOKEN,
      setUsername: AUTH.SET_USERNAME,
    }),
  },
};
</script>

<style scoped>
.login-box {
  border: 1px solid #dcdfe6;
  width: 350px;
  height: 300px;
  margin: 120px auto;
  padding: 35px 35px 15px 35px;
  -webkit-border-radius: 5px;
  -moz-border-radius: 5px;
  box-shadow: 0 0 25px #909399;
}

.login-title {
  text-align: center;
  margin: 0 auto 40px auto;
  color: #303133;
}
</style>

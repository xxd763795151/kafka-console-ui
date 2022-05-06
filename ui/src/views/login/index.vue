<template>
  <div id="login">
    <div class="kafka-console-ui">
      <span style="font-size: xxx-large; font-weight: bold">kafka-console-ui</span>
    </div>
    <div>
        <a-form
            :form="form"
            :label-col="{ span: 10 }"
            :wrapper-col="{ span: 4 }"
            @submit="handleSubmit"
        >
          <a-form-item label="账号">
            <a-input
                v-decorator="[
                'username',
                { rules: [{ required: true, message: '请输入账号' }] },
              ]"
                placeholder="请输入账号"
            />
          </a-form-item>
          <a-form-item label="密码">
            <a-input
                v-decorator="[
                'password',
                { rules: [{ required: true, message: '请输入密码' }] },
              ]"
                placeholder="请输入密码"
            />
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 10, offset: 10 }">
            <a-button type="primary" html-type="submit"> 提交 </a-button>
          </a-form-item>
        </a-form>
    </div>
    <router-view></router-view>
  </div>
</template>

<script>
import notification from "ant-design-vue/lib/notification";
import request from "@/utils/request";
import { DevOpsUserAPi } from "@/utils/api";

export default {
  name: 'login',
  data(){
    return{
      form: this.$form.createForm(this, { name: "coordinated" }),
    }
  },methods:{
    handleSubmit(e){
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          if (values.configs) {
            const config = {};
            values.configs.split("\n").forEach((e) => {
              const c = e.split("=");
              if (c.length > 1) {
                let k = c[0].trim(),
                    v = c[1].trim();
                if (k && v) {
                  config[k] = v;
                }
              }
            });
            values.configs = config;
          }
          request({
            url: DevOpsUserAPi.login.url,
            method: DevOpsUserAPi.login.method,
            data: values,
          }).then((res) => {
            if (res.code == 0) {
              localStorage.setItem('token', res.data.token)
              localStorage.setItem('role', res.data.role)
              this.$router.push({ path:'/main'})
            } else {
              notification.error({
                message: "error",
                description: res.msg,
              });
            }
          });
        }
      });
    }
  }
}
</script>
<style>
.kafka-console-ui{
  text-align: center;
  height: 100px;
  margin-top: 50px;
  margin-bottom: 20px;
}
</style>
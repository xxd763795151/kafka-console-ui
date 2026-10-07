<template>
  <a-form
    ref="formRef"
    :model="formState"
    :label-col="{ span: 5 }"
    :wrapper-col="{ span: 12 }"
    @finish="handleSubmit"
    class="login-box"
  >
    <h3 class="login-title">登录kafka-console-ui</h3>
    <a-form-item
      label="账号"
      name="username"
      :rules="[{ required: true, message: '请输入账号' }]"
    >
      <a-input
        style="width: 200px"
        allowClear
        v-model:value="formState.username"
      >
      </a-input>
    </a-form-item>
    <a-form-item
      label="密码"
      name="password"
      :rules="[{ required: true, message: '请输入密码' }]"
    >
      <a-input-password
        style="width: 200px"
        v-model:value="formState.password"
      />
    </a-form-item>
    <a-form-item :wrapper-col="{ span: 16, offset: 5 }">
      <a-button
        type="primary"
        html-type="submit"
        :loading="loading"
        >登录</a-button
      >
    </a-form-item>
  </a-form>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import request from "@/utils/request";
import { AuthApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import { mapMutations } from "vuex";
import { AUTH, CLUSTER } from "@/store/mutation-types";

export default defineComponent({
  name: "Login",
  setup() {
    const formRef = ref();
    const formState = reactive({
      username: '',
      password: '',
    });
    return {
      formRef,
      formState,
    };
  },
  data() {
    return {
      loading: false,
    };
  },
  methods: {
    async handleSubmit() {
      this.loading = true;
      const params = Object.assign({}, (this as any).formState);
      request({
        url: AuthApi.login.url,
        method: AuthApi.login.method,
        data: params,
      }).then((res: any) => {
        this.loading = false;
        if (res.code == 0) {
          this.setToken(res.data.token);
          this.setUsername(params.username);
          this.setPermissions(res.data.permissions);
          this.$router.push("/");
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    ...mapMutations({
      setToken: AUTH.SET_TOKEN,
      setUsername: AUTH.SET_USERNAME,
      setPermissions: AUTH.SET_PERMISSIONS,
      deleteClusterInfo: CLUSTER.DELETE,
    }),
  },
  created() {
    request({
      url: AuthApi.ownDataAuthority.url,
      method: AuthApi.ownDataAuthority.method,
    }).then((res: any) => {
      if (!res) {
        this.deleteClusterInfo();
      }
    });
  },
});
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

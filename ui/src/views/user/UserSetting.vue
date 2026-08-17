<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-form :model="formState" :rules="rules" ref="formRef" @submit="handleSubmit">
        <a-form-item label="新密码" name="password">
          <a-input-password
            v-model:value="formState.password"
          />
        </a-form-item>
        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password
            v-model:value="formState.confirmPassword"
          />
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
          <a-button type="primary" html-type="submit"> 提交 </a-button>
        </a-form-item>
      </a-form>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, ref, computed } from 'vue';
import { message } from 'ant-design-vue';
import request from '@/utils/request';
import { UserManageApi } from '@/utils/api';
import notification from 'ant-design-vue/lib/notification';
import type { Rule } from 'ant-design-vue/es/form';

export default defineComponent({
  name: 'UserSetting',
  props: {
    topicList: {
      type: Array,
    },
  },
  setup() {
    const formRef = ref();

    const state = reactive({
      loading: false,
      formState: {
        password: '',
        confirmPassword: '',
      },
    });

    const validateConfirmPassword = async (_rule: Rule, value: string) => {
      if (value !== state.formState.password) {
        return Promise.reject('两次密码不一致');
      }
      return Promise.resolve();
    };

    const rules = {
      password: [{ required: true, message: '请输入密码' }],
      confirmPassword: [
        { required: true, message: '两次密码不一致' },
        { validator: validateConfirmPassword },
      ],
    };

    const handleSubmit = async (e: any) => {
      e.preventDefault();
      const param = Object.assign({}, state.formState);
      state.loading = true;
      request({
        url: UserManageApi.updatePassword.url,
        method: UserManageApi.updatePassword.method,
        data: param,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          message.success(res.msg);
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    return {
      ...toRefs(state),
      formRef,
      rules,
      handleSubmit,
    };
  },
});
</script>

<style scoped>
.content {
  padding-left: 30%;
  padding-right: 30%;
}
</style>

<template>
  <a-modal
    title="新增/更新用户"
    v-model:open="show"
    :confirm-loading="confirmLoading"
    :width="800"
    @ok="handleOk"
    @cancel="handleCancel"
    ok-text="提交"
    cancel-text="取消"
    :mask="true"
    :destroy-on-close="true"
  >
    <div>
      <a-form
        ref="formRef"
        layout="vertical"
        :model="formState"
      >
        <a-form-item
          label="用户名"
          name="username"
          :rules="[{ required: true, message: '请填写用户名!' }]"
        >
          <a-input
            v-model:value="formState.username"
            placeholder="username"
            :allow-clear="true"
            :max-length="100"
          />
        </a-form-item>
        <a-form-item
          label="密码"
          name="password"
          :rules="[{ required: true, message: '请填写密码!' }]"
        >
          <a-input
            v-model:value="formState.password"
            placeholder="password"
            :allow-clear="true"
            :max-length="100"
          />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from 'vue'
import { notification } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import request from "@/utils/request";
import { KafkaAclApi } from "@/utils/api";

export default defineComponent({
  name: "UpdateUser",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const formRef = ref<FormInstance>()
    const confirmLoading = ref(false)
    const show = ref(props.visible)

    const formState = reactive({
      username: '',
      password: '',
    })

    watch(() => props.visible, (val) => {
      show.value = val;
    })

    watch(show, (val) => {
      emit('update:visible', val)
    })

    function handleOk() {
      formRef.value?.validate().then((values: any) => {
        confirmLoading.value = true;
        request({
          url: KafkaAclApi.addKafkaUser.url,
          method: KafkaAclApi.addKafkaUser.method,
          data: { username: values.username, password: values.password },
        }).then((res: any) => {
          confirmLoading.value = false;
          if (res.code == 0) {
            notification.success({
              message: res.msg,
            });
            formRef.value?.resetFields();
            emit("updateUserDialogData", { ok: true, show: false });
          } else {
            notification.error({
              message: res.msg,
            });
          }
        });
      }).catch(() => {
      });
    }

    function handleCancel() {
      emit("updateUserDialogData", { ok: false, show: false });
    }

    return {
      formRef,
      confirmLoading,
      show,
      formState,
      handleOk,
      handleCancel,
    }
  }
});
</script>

<style scoped>
.input-c {
  margin-bottom: 1%;
}
</style>

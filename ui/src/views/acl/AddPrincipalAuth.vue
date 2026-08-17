<template>
  <a-modal
    title="增加权限"
    :open="show"
    :confirm-loading="confirmLoading"
    :width="800"
    @ok="handleOk"
    @cancel="handleCancel"
    ok-text="提交"
    cancel-text="取消"
    :mask="false"
    :destroy-on-close="true"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 12 }"
    >
      <a-form-item
        label="主体标识"
        name="username"
        :rules="[{ required: true, message: '请输入!' }]"
      >
        <a-input v-model:value="formState.username" />
      </a-form-item>
      <a-form-item label="资源类型" name="resourceType">
        <a-radio-group v-model:value="formState.resourceType">
          <a-radio value="TOPIC"> topic</a-radio>
          <a-radio value="GROUP"> 消费组</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        label="资源名称"
        name="resourceName"
        :rules="[{ required: true, message: '请输入!' }]"
      >
        <a-input
          v-model:value="formState.resourceName"
          placeholder="请输入topic或消费组名称"
        />
      </a-form-item>
      <a-form-item
        label="主机"
        name="host"
        :rules="[{ required: true, message: '请输入!' }]"
      >
        <a-input
          v-model:value="formState.host"
          placeholder="请输入主机地址，比如：*，全部匹配"
        />
      </a-form-item>
      <a-form-item
        label="操作类型"
        name="operation"
        has-feedback
        :rules="[{ required: true, message: '请选择!' }]"
      >
        <a-select
          v-model:value="formState.operation"
          placeholder="请选择!"
        >
          <a-select-option v-for="i in operations" :key="i">
            {{ i }}</a-select-option
          >
        </a-select>
      </a-form-item>
      <a-form-item label="权限类型" name="permissionType">
        <a-radio-group v-model:value="formState.permissionType">
          <a-radio value="ALLOW"> 允许</a-radio>
          <a-radio value="DENY"> 拒绝</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { KafkaAclApi } from "@/utils/api";
import request from "@/utils/request";

export default defineComponent({
  name: "AddPrincipalAuth",
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
    const operations = ref<any[]>([])

    const formState = reactive({
      username: '',
      resourceType: 'TOPIC',
      resourceName: '',
      host: '*',
      operation: undefined as any,
      permissionType: 'ALLOW',
    })

    watch(() => props.visible, (v) => {
      if (show.value != v) {
        show.value = v;
        if (show.value) {
          getOperationList();
        }
      }
    })

    function handleOk() {
      formRef.value?.validate().then((values: any) => {
        const param = Object.assign({}, values);
        const api = KafkaAclApi.addAclAuth;
        confirmLoading.value = true;
        request({
          url: api.url,
          method: api.method,
          data: param,
        }).then((res: any) => {
          confirmLoading.value = false;
          if (res.code == 0) {
            message.success(res.msg);
            emit("closeAddPrincipalAuthDialog", { refresh: true });
          } else {
            message.error(res.msg);
          }
        });
      }).catch(() => {
      });
    }

    function handleCancel() {
      emit("closeAddPrincipalAuthDialog", { refresh: false });
    }

    function getOperationList() {
      request({
        url: KafkaAclApi.getOperationList.url,
        method: KafkaAclApi.getOperationList.method,
      }).then((res: any) => {
        if (res.code != 0) {
          message.error(res.msg);
        } else {
          operations.value.splice(0, operations.value.length);
          operations.value.push(...res.data);
        }
      });
    }

    return {
      formRef,
      confirmLoading,
      show,
      operations,
      formState,
      handleOk,
      handleCancel,
    }
  }
});
</script>

<style scoped></style>

<template>
  <a-modal
    title="管理消费权限"
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
      <a-form-item label="用户名" name="username">
        <a-input
          v-model:value="formState.username"
          disabled
        />
      </a-form-item>
      <a-form-item
        label="topic"
        name="topic"
        :rules="[{ required: true, message: '请输入topic!' }]"
      >
        <a-input v-model:value="formState.topic" />
      </a-form-item>
      <a-form-item
        label="消费组"
        name="groupId"
        :rules="[{ required: true, message: '请输入消费组!' }]"
      >
        <a-input v-model:value="formState.groupId" />
      </a-form-item>
      <a-form-item label="类型" name="type">
        <a-radio-group v-model:value="formState.type">
          <a-radio value="grant"> 授予 </a-radio>
          <a-radio value="revoke"> 收回 </a-radio>
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
  name: "AddProducerAuth",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    record: {
      default: () => ({}),
    },
  },
  setup(props, { emit }) {
    const formRef = ref<FormInstance>()
    const confirmLoading = ref(false)
    const show = ref(props.visible)

    const formState = reactive({
      username: '',
      topic: '',
      groupId: '',
      type: 'grant',
    })

    watch(() => props.visible, (v) => {
      show.value = v;
      if (v) {
        formState.username = (props.record as any).username || ''
      }
    })

    function handleOk() {
      formRef.value?.validate().then((values: any) => {
        const param = {
          username: values.username,
          topic: values.topic,
          groupId: values.groupId,
        };
        const api: any = {};
        switch (values.type) {
          case "grant":
            Object.assign(api, KafkaAclApi.addConsumerAuth);
            break;
          case "revoke":
            Object.assign(api, KafkaAclApi.deleteConsumerAuth);
            break;
          default:
            message.error("unknown error");
            return;
        }

        confirmLoading.value = true;
        request({
          url: api.url,
          method: api.method,
          data: param,
        }).then((res: any) => {
          confirmLoading.value = false;
          if (res.code == 0) {
            message.success(res.msg);
            emit("manageConsumerAuthDialog", values);
          } else {
            message.error(res.msg);
          }
        });
      }).catch(() => {
      });
    }

    function handleCancel() {
      emit("manageConsumerAuthDialog", {});
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

<style scoped></style>

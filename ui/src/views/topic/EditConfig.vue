<template>
  <a-modal
    title="编辑配置"
    :open="show"
    :width="1000"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :model="formState"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @submit="handleSubmit"
        >
          <a-form-item label="属性" name="name">
            <a-input
              :disabled="true"
              v-model:value="formState.name"
              placeholder="name"
            />
          </a-form-item>
          <a-form-item
            label="值"
            name="value"
            :rules="[{ required: true, message: '输入属性值!' }]"
          >
            <a-input
              v-model:value="formState.value"
              placeholder="value"
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

<script lang="ts">
import { defineComponent, reactive, ref, watch } from "vue";
import { message, notification } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaConfigApi } from "@/utils/api";

export default defineComponent({
  name: "EditTopicConfig",
  props: {
    topic: {
      type: String,
      default: "",
    },
    open: {
      type: Boolean,
      default: false,
    },
    record: {
      type: Object,
      default: () => ({}),
    },
  },
  emits: ["closeEditConfigDialog"],
  setup(props, { emit }) {
    const show = ref(props.open);
    const data = ref<any[]>([]);
    const loading = ref(false);

    const formState = reactive({
      name: (props.record as any).name || "",
      value: (props.record as any).value || "",
    });

    watch(
      () => props.open,
      (v) => {
        show.value = v;
      }
    );

    watch(
      () => props.record,
      (v) => {
        formState.name = (v as any).name || "";
        formState.value = (v as any).value || "";
      },
      { deep: true }
    );

    function handleSubmit(e: Event) {
      e.preventDefault();
      loading.value = true;
      const api = KafkaConfigApi.setTopicConfig;
      request({
        url: api.url,
        method: api.method,
        data: Object.assign({ entity: props.topic }, formState),
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("closeEditConfigDialog", { refresh: true });
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    }

    function handleCancel() {
      data.value = [];
      emit("closeEditConfigDialog", { refresh: false });
    }

    return {
      show,
      data,
      loading,
      formState,
      handleSubmit,
      handleCancel,
    };
  },
});
</script>

<style scoped></style>

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
          ref="formRef"
          :model="formState"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @finish="handleSubmit"
        >
          <a-form-item label="属性">
            <a-input
              disabled
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
import request from "@/utils/request";
import { KafkaConfigApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import { message } from "ant-design-vue";

interface ConfigRecord {
  name?: string;
  value?: string;
  source?: string;
  readOnly?: boolean;
}

export default defineComponent({
  name: "EditConfig",
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
    brokerId: {
      type: String,
      default: "",
    },
    isLoggerConfig: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const formRef = ref();
    const show = ref(props.open);
    const loading = ref(false);
    const formState = reactive<ConfigRecord>({
      name: props.record?.name || "",
      value: props.record?.value || "",
    });

    watch(
      () => props.open,
      (v: boolean) => {
        show.value = v;
        if (v) {
          formState.name = props.record?.name || "";
          formState.value = props.record?.value || "";
        }
      }
    );

    watch(
      () => props.record,
      (newRecord: ConfigRecord) => {
        formState.name = newRecord?.name || "";
        formState.value = newRecord?.value || "";
      }
    );

    const handleSubmit = async () => {
      loading.value = true;
      const api = props.isLoggerConfig
        ? KafkaConfigApi.setBrokerLoggerConfig
        : KafkaConfigApi.setBrokerConfig;
      request({
        url: api.url,
        method: api.method,
        data: Object.assign({ entity: props.brokerId }, { name: formState.name, value: formState.value }),
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
    };

    const handleCancel = () => {
      emit("closeEditConfigDialog", { refresh: false });
    };

    return {
      formRef,
      show,
      loading,
      formState,
      handleSubmit,
      handleCancel,
    };
  },
});
</script>

<style scoped></style>

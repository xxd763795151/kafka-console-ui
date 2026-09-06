<template>
  <a-modal
    title="新增Topic"
    :open="show"
    :width="800"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :model="formState"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @finish="handleSubmit"
        >
          <a-form-item
            label="Topic名称"
            name="name"
            :rules="[{ required: true, message: '输入topic名称!' }]"
          >
            <a-input
              v-model:value="formState.name"
              placeholder="topic"
            />
          </a-form-item>
          <a-form-item
            label="分区"
            name="numPartitions"
            :rules="[{ required: true, message: '输入分区数!' }]"
          >
            <a-input-number
              :min="1"
              :max="128"
              v-model:value="formState.numPartitions"
            />
            <span class="ant-form-text"> 个分区 </span>
          </a-form-item>
          <a-form-item
            label="副本"
            name="replicationFactor"
            :rules="[{ required: true, message: '输入副本数!' }]"
          >
            <a-input-number
              :min="1"
              :max="32"
              v-model:value="formState.replicationFactor"
            />
            <span class="ant-form-text"> 个副本 </span>
          </a-form-item>
          <a-form-item label="属性" name="configs">
            <a-textarea
              rows="5"
              placeholder="格式示例如下：
max.message.bytes=1024
retention.bytes=1024
retention.ms=3600000"
              v-model:value="formState.configs"
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
import { KafkaTopicApi } from "@/utils/api";

export default defineComponent({
  name: "CreateTopic",
  props: {
    topic: {
      type: String,
      default: "",
    },
    open: {
      type: Boolean,
      default: false,
    },
  },
  emits: ["closeCreateTopicDialog"],
  setup(props, { emit }) {
    const show = ref(props.open);
    const data = ref<any[]>([]);
    const loading = ref(false);

    const formState = reactive({
      name: "",
      numPartitions: 1,
      replicationFactor: 1,
      configs: "",
    });

    watch(
      () => props.open,
      (v) => {
        show.value = v;
        if (v) {
          formState.name = "";
          formState.numPartitions = 1;
          formState.replicationFactor = 1;
          formState.configs = "";
        }
      }
    );

    function handleSubmit(formValues: Record<string, any>) {
      const values = { ...formValues };
      if (values.configs) {
        const config: Record<string, string> = {};
        values.configs.split("\n").forEach((e) => {
          const c = e.split("=");
          if (c.length > 1) {
            const k = c[0].trim();
            const v = c[1].trim();
            if (k && v) {
              config[k] = v;
            }
          }
        });
        (values as any).configs = config;
      }
      loading.value = true;
      request({
        url: KafkaTopicApi.creatTopic.url,
        method: KafkaTopicApi.creatTopic.method,
        data: values,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("closeCreateTopicDialog", { refresh: true });
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
      emit("closeCreateTopicDialog", { refresh: false });
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

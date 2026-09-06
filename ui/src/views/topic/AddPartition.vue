<template>
  <a-modal
    title="增加分区"
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
          <a-form-item label="Topic名称" name="topic">
            <a-input
              :disabled="true"
              v-model:value="formState.topic"
              placeholder="topic"
            />
          </a-form-item>
          <a-form-item
            label="增加分区数"
            name="addNum"
            :rules="[{ required: true, message: '输入分区数!' }]"
          >
            <a-input-number
              :min="1"
              :max="32"
              v-model:value="formState.addNum"
            />
            <span class="ant-form-text"> 个分区 </span>
          </a-form-item>
          <a-form-item label="副本" name="assignment">
            <a-textarea
              rows="5"
              placeholder="可选参数，指定新增分区的副本，格式示例如下：
1=1,2
2=2,3"
              v-model:value="formState.assignment"
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
  name: "AddPartition",
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
  emits: ["closeAddPartitionDialog"],
  setup(props, { emit }) {
    const show = ref(props.open);
    const data = ref<any[]>([]);
    const loading = ref(false);

    const formState = reactive({
      topic: props.topic,
      addNum: 1,
      assignment: "",
    });

    watch(
      () => props.open,
      (v) => {
        show.value = v;
        if (show.value) {
          formState.topic = props.topic;
          formState.addNum = 1;
          formState.assignment = "";
          getPartitionInfo();
        }
      }
    );

    watch(
      () => props.topic,
      (v) => {
        formState.topic = v;
      }
    );

    function getPartitionInfo() {
      loading.value = false;
    }

    function handleSubmit(formValues: Record<string, any>) {
      const values = { ...formValues };
      if (values.assignment) {
        const assignment: Record<string, string[]> = {};
        values.assignment.split("\n").forEach((e) => {
          const c = e.split("=");
          if (c.length > 1) {
            const k = c[0];
            const v = c[1];
            const arr = v.split(",");
            if (arr.length > 0) {
              assignment[k] = arr;
            }
          }
        });
        (values as any).assignment = assignment;
      }
      loading.value = true;
      request({
        url: KafkaTopicApi.addPartition.url,
        method: KafkaTopicApi.addPartition.method,
        data: values,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("closeAddPartitionDialog", { refresh: true });
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
      emit("closeAddPartitionDialog", { refresh: false });
    }

    return {
      show,
      data,
      loading,
      formState,
      getPartitionInfo,
      handleSubmit,
      handleCancel,
    };
  },
});
</script>

<style scoped></style>

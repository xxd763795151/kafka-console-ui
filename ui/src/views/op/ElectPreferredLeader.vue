<template>
  <a-modal
    title="选择Preferred副本作为Leader"
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
            label="Topic"
            name="topic"
            :rules="[{ required: true, message: '请选择一个topic!' }]"
          >
            <a-select
              v-model:value="formState.topic"
              @change="handleTopicChange"
              show-search
              :filter-option="true"
              option-filter-prop="label"
              placeholder="请选择一个topic"
            >
              <a-select-option v-for="v in topicList" :key="v" :value="v" :label="String(v)">
                {{ v }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item
            label="分区"
            name="partition"
            :rules="[{ required: true, message: '请选择一个分区!' }]"
          >
            <a-select
              v-model:value="formState.partition"
              show-search
              :filter-option="true"
              option-filter-prop="label"
              placeholder="请选择一个分区"
            >
              <a-select-option
                v-for="v in partitions"
                :key="v"
                :value="v"
                :label="v == -1 ? '全部' : String(v)"
              >
                <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 确认 </a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import notification from "ant-design-vue/es/notification";
import request from "@/utils/request";
import { KafkaTopicApi, KafkaOpApi } from "@/utils/api";

export default defineComponent({
  name: "ElectPreferredLeader",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const show = ref(props.visible);
    const data = ref<any[]>([]);
    const loading = ref(false);
    const topicList = ref<any[]>([]);
    const partitions = ref<any[]>([]);

    const formState = reactive({
      topic: undefined as any,
      partition: undefined as any,
    });

    watch(
      () => props.visible,
      (v) => {
        show.value = v;
        if (show.value) {
          getTopicNameList();
        }
      }
    );

    const handleSubmit = (values: any) => {
      loading.value = true;
      request({
        url: KafkaOpApi.electPreferredLeader.url,
        method: KafkaOpApi.electPreferredLeader.method,
        data: values,
      }).then((res: any) => {
          loading.value = false;
          if (res.code != 0) {
            notification.error({
              message: "error",
              description: res.msg,
            });
          } else {
            message.success(res.msg);
            emit("closeElectPreferredLeaderDialog", { refresh: false });
          }
        });
    };

    const getTopicNameList = () => {
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
      }).then((res: any) => {
        if (res.code == 0) {
          topicList.value = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    const getPartitionInfo = (topic: string) => {
      loading.value = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + topic,
        method: KafkaTopicApi.getPartitionInfo.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          partitions.value = res.data.map((v: any) => v.partition);
          partitions.value.splice(0, 0, -1);
        }
      });
    };

    const handleTopicChange = (topic: string) => {
      getPartitionInfo(topic);
    };

    const handleCancel = () => {
      data.value = [];
      emit("closeElectPreferredLeaderDialog", { refresh: false });
    };

    return {
      show,
      data,
      loading,
      topicList,
      partitions,
      formState,
      handleSubmit,
      getTopicNameList,
      getPartitionInfo,
      handleTopicChange,
      handleCancel,
    };
  },
});
</script>

<style scoped></style>

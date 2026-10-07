<template>
  <a-modal
    :title="'Topic: ' + topic"
    :open="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div v-for="(v, k) in data" :key="k">
          <strong>消费组: </strong><span class="color-font">{{ k }}</span
          ><strong> | 积压: </strong><span class="color-font">{{ v.lag }}</span>
          <a-button
            type="primary"
            size="small"
            style="float: right"
            @click="getConsumerDetail"
          >
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
          <hr />
          <a-table
            :columns="columns"
            :data-source="v.data"
            bordered
            :rowKey="(record) => record.topic + record.partition"
          >
            <template #bodyCell="{ column, text, record }">
              <template v-if="column.key === 'clientId'">
                <span v-if="text"> {{ text }}@{{ record.host }} </span>
              </template>
            </template>
          </a-table>
        </div>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from "vue";
import { message, notification } from "ant-design-vue";
import { ReloadOutlined } from "@ant-design/icons-vue";
import request from "@/utils/request";
import { KafkaConsumerApi } from "@/utils/api";

const columns = [
  {
    title: "分区",
    dataIndex: "partition",
    key: "partition",
  },
  {
    title: "客户端",
    dataIndex: "clientId",
    key: "clientId",
    width: 400,
  },
  {
    title: "日志位点",
    dataIndex: "logEndOffset",
    key: "logEndOffset",
  },
  {
    title: "消费位点",
    dataIndex: "consumerOffset",
    key: "consumerOffset",
  },
  {
    title: "积压",
    dataIndex: "lag",
    key: "lag",
  },
];

export default defineComponent({
  name: "ConsumedDetail",
  components: {
    ReloadOutlined,
  },
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
  emits: ["closeConsumedDetailDialog"],
  setup(props, { emit }) {
    const show = ref(props.open);
    const data = ref<any>([]);
    const loading = ref(false);
    const showResetPartitionOffsetDialog = ref(false);
    const select = reactive({
      topic: "",
      partition: 0,
    });
    const group = ref("");

    watch(
      () => props.open,
      (v) => {
        show.value = v;
        if (show.value) {
          getConsumerDetail();
        }
      }
    );

    function getConsumerDetail() {
      loading.value = true;
      request({
        url:
          KafkaConsumerApi.getTopicSubscribedByGroups.url +
          "?topic=" +
          props.topic,
        method: KafkaConsumerApi.getTopicSubscribedByGroups.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          data.value = res.data;
        }
      });
    }

    function handleCancel() {
      data.value = [];
      emit("closeConsumedDetailDialog", {});
    }

    function resetTopicOffsetToEndpoint(groupId: string, topic: string, type: string) {
      requestResetOffset({
        groupId: groupId,
        topic: topic,
        level: 1,
        type: type,
      });
    }

    function requestResetOffset(data: any, callbackOnSuccess?: Function) {
      loading.value = true;
      request({
        url: KafkaConsumerApi.resetOffset.url,
        method: KafkaConsumerApi.resetOffset.method,
        data: data,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          message.success(res.msg);
          getConsumerDetail();
          if (callbackOnSuccess) {
            callbackOnSuccess();
          }
        }
      });
    }

    function openResetPartitionOffsetDialog(topic: string, partition: number) {
      showResetPartitionOffsetDialog.value = true;
      select.topic = topic;
      select.partition = partition;
    }

    function closeResetPartitionOffsetDialog() {
      showResetPartitionOffsetDialog.value = false;
    }

    return {
      columns,
      show,
      data,
      loading,
      showResetPartitionOffsetDialog,
      select,
      group,
      getConsumerDetail,
      handleCancel,
      resetTopicOffsetToEndpoint,
      requestResetOffset,
      openResetPartitionOffsetDialog,
      closeResetPartitionOffsetDialog,
    };
  },
});
</script>

<style scoped>
.color-font {
  color: dodgerblue;
}
#resetPartitionOffsetModal .ant-input-number {
  width: 100% !important;
}
</style>

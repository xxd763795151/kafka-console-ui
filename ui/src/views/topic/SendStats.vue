<template>
  <a-modal
    :title="topic + '发送统计'"
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
        <h4>
          今天发送消息数：{{ today.total
          }}<a-button
            type="primary"
            size="small"
            style="float: right"
            @click="sendStatus"
          >
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </h4>
        <a-table
          :columns="columns"
          :data-source="today.detail"
          bordered
          :rowKey="(record) => record.partition"
        >
        </a-table>
        <hr />
        <h4>昨天发送消息数：{{ yesterday.total }}</h4>
        <a-table
          :columns="columns"
          :data-source="yesterday.detail"
          bordered
          :rowKey="(record) => record.partition"
        >
        </a-table>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from "vue";
import { notification } from "ant-design-vue";
import { ReloadOutlined } from "@ant-design/icons-vue";
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";

const columns = [
  {
    title: "分区",
    dataIndex: "partition",
    key: "partition",
  },
  {
    title: "数量",
    dataIndex: "num",
    key: "num",
  },
];

export default defineComponent({
  name: "SendStats",
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
  emits: ["closeMessageStatsDialog"],
  setup(props, { emit }) {
    const show = ref(props.open);
    const data = ref<any>([]);
    const loading = ref(false);
    const yesterday = reactive<any>({});
    const today = reactive<any>({});

    watch(
      () => props.open,
      (v) => {
        show.value = v;
        if (show.value) {
          sendStatus();
        }
      }
    );

    function sendStatus() {
      loading.value = true;
      const api = KafkaTopicApi.sendStats;
      request({
        url: api.url + "?topic=" + props.topic,
        method: api.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          data.value = res.data;
          Object.assign(yesterday, data.value.yesterday || {});
          Object.assign(today, data.value.today || {});
        }
      });
    }

    function handleCancel() {
      data.value = [];
      Object.keys(yesterday).forEach((key) => delete (yesterday as any)[key]);
      Object.keys(today).forEach((key) => delete (today as any)[key]);
      emit("closeMessageStatsDialog", {});
    }

    return {
      columns,
      show,
      data,
      loading,
      yesterday,
      today,
      sendStatus,
      handleCancel,
    };
  },
});
</script>

<style scoped></style>

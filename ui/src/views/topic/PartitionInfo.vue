<template>
  <a-modal
    title="分区详情"
    :open="show"
    :width="1500"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-table
          bordered
          :columns="columns"
          :data-source="data"
          :rowKey="
            (record, index) => {
              return index;
            }
          "
        >
          <template #bodyCell="{ column, text, record }">
            <template v-if="column.key === 'replicas'">
              <ul>
                <li v-for="i in text" :key="i">
                  {{ i }}
                </li>
              </ul>
            </template>
            <template v-else-if="column.key === 'isr'">
              <div>
                <span v-for="i in text" :key="i">
                  {{ i }}
                </span>
              </div>
            </template>
            <template v-else-if="column.key === 'operation' && !record.internal">
              <a-popconfirm
                :title="
                  'topic: ' +
                  topic +
                  '，分区:' +
                  record.partition +
                  '，确认选择第一个副本作为leader？'
                "
                ok-text="确认"
                cancel-text="取消"
                @confirm="electPreferredLeader(record)"
              >
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  v-action:topic:partition-detail:preferred
                  >首选副本作为leader
                </a-button>
              </a-popconfirm>
            </template>
          </template>
          <template #expandedRowRender="{ record }">
            <p style="margin: 0">
              有效消息的时间范围：<span class="red-font">{{
                formatTime(record.beginTime)
              }}</span>
              ~
              <span class="green-font">{{ formatTime(record.endTime) }}</span>
            </p>
          </template>
        </a-table>
        <p>友情提示：点击+号展开，可以查看当前分区的有效消息的时间范围</p>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from "vue";
import { message, notification } from "ant-design-vue";
import dayjs from "dayjs";
import request from "@/utils/request";
import { KafkaOpApi, KafkaTopicApi } from "@/utils/api";

const columns = [
  {
    title: "分区",
    dataIndex: "partition",
    key: "partition",
  },
  {
    title: "leader",
    dataIndex: "leader",
    key: "leader",
  },
  {
    title: "副本",
    dataIndex: "replicas",
    key: "replicas",
  },
  {
    title: "isr",
    dataIndex: "isr",
    key: "isr",
  },
  {
    title: "最小位点",
    dataIndex: "beginOffset",
    key: "beginOffset",
  },
  {
    title: "最大位点",
    dataIndex: "endOffset",
    key: "endOffset",
  },
  {
    title: "消息总数",
    dataIndex: "diff",
    key: "diff",
  },
  {
    title: "操作",
    key: "operation",
  },
];

export default defineComponent({
  name: "PartitionInfo",
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
  emits: ["closePartitionInfoDialog"],
  setup(props, { emit }) {
    const show = ref(props.open);
    const data = ref<any[]>([]);
    const loading = ref(false);

    watch(
      () => props.open,
      (v) => {
        show.value = v;
        if (show.value) {
          getPartitionInfo();
        }
      }
    );

    function getPartitionInfo() {
      loading.value = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + props.topic,
        method: KafkaTopicApi.getPartitionInfo.method,
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
      emit("closePartitionInfoDialog", {});
    }

    function electPreferredLeader(record: any) {
      loading.value = true;
      request({
        url: KafkaOpApi.electPreferredLeader.url,
        method: KafkaOpApi.electPreferredLeader.method,
        data: { topic: props.topic, partition: record.partition },
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          message.success(res.msg);
          getPartitionInfo();
        }
      });
    }

    function formatTime(timestamp: number) {
      return timestamp != -1
        ? dayjs(timestamp).format("YYYY-MM-DD HH:mm:ss:SSS")
        : timestamp;
    }

    return {
      columns,
      show,
      data,
      loading,
      getPartitionInfo,
      handleCancel,
      electPreferredLeader,
      formatTime,
    };
  },
});
</script>

<style scoped>
.red-font {
  color: red;
}
.green-font {
  color: green;
}
</style>

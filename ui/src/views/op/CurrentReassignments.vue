<template>
  <a-modal
    title="正在进行副本重分配的分区"
    :open="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="data"
          bordered
          :rowKey="(record: any) => record.topic + record.partition"
        >
          <template #bodyCell="{ column, text, record }">
            <template v-if="column.key === 'replicas'">
              <span v-for="i in text" :key="i">
                {{ i }}
              </span>
            </template>
            <template v-if="column.key === 'addingReplicas'">
              <span v-for="i in text" :key="i">
                {{ i }}
              </span>
            </template>
            <template v-if="column.key === 'removingReplicas'">
              <span v-for="i in text" :key="i">
                {{ i }}
              </span>
            </template>
            <template v-if="column.key === 'operation'">
              <a-popconfirm
                title="取消正在进行的副本重分配任务?"
                ok-text="确认"
                cancel-text="取消"
                @confirm="cancelReassignment(record)"
              >
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  v-action:op:replication-update-detail:cancel
                  >取消
                </a-button>
              </a-popconfirm>
            </template>
          </template>
        </a-table>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from "vue";
import notification from "ant-design-vue/es/notification";
import request from "@/utils/request";
import { KafkaOpApi } from "@/utils/api";

const columns = [
  {
    title: "Topic",
    dataIndex: "topic",
    key: "topic",
  },
  {
    title: "分区",
    dataIndex: "partition",
    key: "partition",
  },
  {
    title: "副本",
    dataIndex: "replicas",
    key: "replicas",
  },
  {
    title: "正在增加的副本",
    dataIndex: "addingReplicas",
    key: "addingReplicas",
  },
  {
    title: "正在移除的副本",
    dataIndex: "removingReplicas",
    key: "removingReplicas",
  },
  {
    title: "操作",
    key: "operation",
  },
];

export default defineComponent({
  name: "CurrentReassignments",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const show = ref(props.visible);
    const data = ref<any>([]);
    const loading = ref(false);

    watch(
      () => props.visible,
      (v) => {
        show.value = v;
        if (show.value) {
          currentReassignments();
        }
      }
    );

    const currentReassignments = () => {
      loading.value = true;
      const api = KafkaOpApi.currentReassignments;
      request({
        url: api.url,
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
          }
        });
    };

    const handleCancel = () => {
      emit("closeCurrentReassignmentsDialog", {});
    };

    const cancelReassignment = (record: any) => {
      const param = { topic: record.topic, partition: record.partition };
      loading.value = true;
      const api = KafkaOpApi.cancelReassignment;
      request({
        url: api.url,
        method: api.method,
        data: param,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          currentReassignments();
        }
      });
    };

    return {
      columns,
      show,
      data,
      loading,
      currentReassignments,
      handleCancel,
      cancelReassignment,
    };
  },
});
</script>

<style scoped></style>

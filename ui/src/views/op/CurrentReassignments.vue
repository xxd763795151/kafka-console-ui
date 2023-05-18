<template>
  <a-modal
    title="正在进行副本重分配的分区"
    :visible="show"
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
          :rowKey="(record) => record.topic + record.partition"
        >
          <div slot="replicas" slot-scope="text">
            <span v-for="i in text" :key="i">
              {{ i }}
            </span>
          </div>
          <div slot="addingReplicas" slot-scope="text">
            <span v-for="i in text" :key="i">
              {{ i }}
            </span>
          </div>
          <div slot="removingReplicas" slot-scope="text">
            <span v-for="i in text" :key="i">
              {{ i }}
            </span>
          </div>
          <div slot="operation" slot-scope="record">
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
          </div>
        </a-table>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaOpApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "CurrentReassignments",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      columns: columns,
      show: this.visible,
      data: [],
      loading: false,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.currentReassignments();
      }
    },
  },
  methods: {
    currentReassignments() {
      this.loading = true;
      const api = KafkaOpApi.currentReassignments;
      request({
        url: api.url,
        method: api.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.data = res.data;
          this.yesterday = this.data.yesterday;
          this.today = this.data.today;
        }
      });
    },
    handleCancel() {
      this.$emit("closeCurrentReassignmentsDialog", {});
    },
    cancelReassignment(record) {
      const param = { topic: record.topic, partition: record.partition };
      this.loading = true;
      const api = KafkaOpApi.cancelReassignment;
      request({
        url: api.url,
        method: api.method,
        data: param,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.currentReassignments();
        }
      });
    },
  },
};

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
    scopedSlots: { customRender: "replicas" },
  },
  {
    title: "正在增加的副本",
    dataIndex: "addingReplicas",
    key: "addingReplicas",
    scopedSlots: { customRender: "addingReplicas" },
  },
  {
    title: "正在移除的副本",
    dataIndex: "removingReplicas",
    key: "removingReplicas",
    scopedSlots: { customRender: "removingReplicas" },
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
  },
];
</script>

<style scoped></style>

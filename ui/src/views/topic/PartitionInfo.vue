<template>
  <a-modal
    title="分区详情"
    :visible="show"
    :width="1500"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
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
          <ul slot="replicas" slot-scope="text">
            <li v-for="i in text" :key="i">
              {{ i }}
            </li>
          </ul>
          <div slot="isr" slot-scope="text">
            <span v-for="i in text" :key="i">
              {{ i }}
            </span>
          </div>
          <div slot="operation" slot-scope="record" v-show="!record.internal">
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
              <a-button size="small" href="javascript:;" class="operation-btn"
                >首选副本作为leader
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
import { KafkaOpApi, KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
export default {
  name: "PartitionInfo",
  props: {
    topic: {
      type: String,
      default: "",
    },
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
        this.getPartitionInfo();
      }
    },
  },
  methods: {
    getPartitionInfo() {
      this.loading = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + this.topic,
        method: KafkaTopicApi.getPartitionInfo.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.data = res.data;
        }
      });
    },
    handleCancel() {
      this.data = [];
      this.$emit("closePartitionInfoDialog", {});
    },
    electPreferredLeader(record) {
      this.loading = true;
      request({
        url: KafkaOpApi.electPreferredLeader.url,
        method: KafkaOpApi.electPreferredLeader.method,
        data: { topic: this.topic, partition: record.partition },
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.$message.success(res.msg);
          this.getPartitionInfo();
        }
      });
    },
  },
};

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
    scopedSlots: { customRender: "replicas" },
  },
  {
    title: "isr",
    dataIndex: "isr",
    key: "isr",
    scopedSlots: { customRender: "isr" },
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
    scopedSlots: { customRender: "operation" },
  },
];
</script>

<style scoped></style>

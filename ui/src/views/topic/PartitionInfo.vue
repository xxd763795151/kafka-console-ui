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
          :columns="columns"
          :data-source="data"
          :rowKey="
            (record, index) => {
              return index;
            }
          "
        >
          <ul slot="replicas" slot-scope="text">
            <ol v-for="i in text" :key="i">
              {{
                i
              }}
            </ol>
          </ul>
          <ul slot="isr" slot-scope="text">
            <ol v-for="i in text" :key="i">
              {{
                i
              }}
            </ol>
          </ul>
        </a-table>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
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
];
</script>

<style scoped></style>

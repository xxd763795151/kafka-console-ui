<template>
  <a-modal
    :title="topic + '发送统计'"
    :visible="show"
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
            icon="reload"
            size="small"
            style="float: right"
            @click="sendStatus"
          >
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

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "SendStats",
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
      yesterday: {},
      today: {},
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.sendStatus();
      }
    },
  },
  methods: {
    sendStatus() {
      this.loading = true;
      const api = KafkaTopicApi.sendStats;
      request({
        url: api.url + "?topic=" + this.topic,
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
      this.data = [];
      this.yesterday = {};
      this.today = {};
      this.$emit("closeMessageStatsDialog", {});
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
    title: "数量",
    dataIndex: "num",
    key: "num",
  },
];
</script>

<style scoped></style>

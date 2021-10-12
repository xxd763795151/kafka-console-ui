<template>
  <a-modal
    :title="'消费组: ' + group"
    :visible="show"
    :width="1800"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div v-for="(v, k) in data" :key="k">
          <h4>Topic: {{ k }} | 积压: {{ v.lag }}</h4>
          <hr />
          <a-table
            :columns="columns"
            :data-source="v.data"
            bordered
            :rowKey="(record) => record.topic + record.partition"
          >
            <span slot="clientId" slot-scope="text, record">
              <span v-if="text"> {{ text }}@{{ record.host }} </span>
            </span>
          </a-table>
        </div>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaConsumerApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "ConsumerDetail",
  props: {
    group: {
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
        this.getConsumerDetail();
      }
    },
  },
  methods: {
    getConsumerDetail() {
      this.loading = true;
      request({
        url: KafkaConsumerApi.getConsumerDetail.url + "?groupId=" + this.group,
        method: KafkaConsumerApi.getConsumerDetail.method,
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
      this.$emit("closeConsumerDetailDialog", {});
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
    title: "客户端",
    dataIndex: "clientId",
    key: "clientId",
    scopedSlots: { customRender: "clientId" },
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
</script>

<style scoped></style>

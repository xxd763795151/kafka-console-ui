<template>
  <a-modal
    title="消费端成员"
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
        <a-table
          :columns="columns"
          :data-source="data"
          bordered
          :rowKey="(record) => record.memberId"
        >
          <ul slot="partitions" slot-scope="text">
            <ol v-for="i in text" :key="i.topic + i.partition">
              {{
                i.topic
              }}:
              {{
                i.partition
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
import { KafkaConsumerApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "Member",
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
        this.getPartitionInfo();
      }
    },
  },
  methods: {
    getPartitionInfo() {
      this.loading = true;
      request({
        url: KafkaConsumerApi.getConsumerMembers.url + "?groupId=" + this.group,
        method: KafkaConsumerApi.getConsumerMembers.method,
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
      this.$emit("closeConsumerMemberDialog", {});
    },
  },
};

const columns = [
  {
    title: "成员ID",
    dataIndex: "memberId",
    key: "memberId",
  },
  {
    title: "客户端ID",
    dataIndex: "clientId",
    key: "clientId",
  },
  {
    title: "实例ID",
    dataIndex: "groupInstanceId",
    key: "groupInstanceId",
  },
  {
    title: "主机",
    dataIndex: "host",
    key: "host",
  },
  {
    title: "订阅分区信息",
    dataIndex: "partitions",
    key: "partitions",
    scopedSlots: { customRender: "partitions" },
  },
];
</script>

<style scoped></style>

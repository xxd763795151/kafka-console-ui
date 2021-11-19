<template>
  <a-modal
    title="变更副本"
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
            :data-source="data.partitions"
            bordered
            :rowKey="
            (record, index) => {
              return index;
            }
          "
        >
          <div slot="replicas" slot-scope="text">
            <span v-for="i in text" :key="i">
              {{
                i
              }}
            </span>
          </div>
        </a-table>

      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

export default {
  name: "UpdateReplica",
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
      data: {},
      loading: false,
      form: this.$form.createForm(this, { name: "coordinated" }),
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getCurrentReplicaAssignment();
      }
    },
  },
  methods: {
    getCurrentReplicaAssignment() {
      this.loading = true;
      request({
        url:
          KafkaTopicApi.getCurrentReplicaAssignment.url +
          "?topic=" +
          this.topic,
        method: KafkaTopicApi.getCurrentReplicaAssignment.method,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.data = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    handleCancel() {
      this.data = [];
      this.$emit("closeUpdateReplicaDialog", { refresh: false });
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
];
</script>

<style scoped></style>

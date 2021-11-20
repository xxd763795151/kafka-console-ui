<template>
  <a-modal
    title="变更副本"
    :visible="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :maskClosable="false"
    @cancel="handleCancel"
    okText="确认"
    cancelText="取消"
    @ok="handleOk"
  >
    <div>
      <a-spin :spinning="loading">
        <div class="replica-box">
          <label>副本数：</label
          ><a-input-number
            id="inputNumber"
            v-model="replicaNums"
            :min="1"
            :max="brokerSize"
            @change="onChange"
          />
        </div>
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
              {{ i }}
            </span>
          </div>
        </a-table>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaClusterApi, KafkaTopicApi } from "@/utils/api";
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
      brokerSize: 0,
      replicaNums: 0,
      defaultReplicaNums: 0,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getClusterInfo();
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
          if (this.data.partitions.length > 0) {
            this.replicaNums = this.data.partitions[0].replicas.length;
            this.defaultReplicaNums = this.replicaNums;
          }
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    getClusterInfo() {
      this.loading = true;
      request({
        url: KafkaClusterApi.getClusterInfo.url,
        method: KafkaClusterApi.getClusterInfo.method,
      }).then((res) => {
        this.brokerSize = res.data.nodes.length;
      });
    },
    handleCancel() {
      this.data = {};
      this.$emit("closeUpdateReplicaDialog", { refresh: false });
    },
    onChange(value) {
      if (this.data.partitions.length > 0) {
        this.data.partitions.forEach((p) => {
          if (value > p.replicas.length) {
            let num = p.replicas[p.replicas.length - 1];
            for (let i = p.replicas.length; i < value; i++) {
              p.replicas.push(++num % this.brokerSize);
            }
          }
          if (value < p.replicas.length) {
            p.replicas.pop();
          }
        });
      }
    },
    handleOk() {
      this.loading = true;
      request({
        url: KafkaTopicApi.updateReplicaAssignment.url,
        method: KafkaTopicApi.updateReplicaAssignment.method,
        data: this.data,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.$message.success(res.msg);
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
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
];
</script>

<style scoped>
.replica-box {
  margin-bottom: 1%;
}
</style>

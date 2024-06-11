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
          <label>设置副本数：</label
          ><a-input-number
            id="inputNumber"
            v-model="replicaNums"
            :min="1"
            :max="brokerSize"
            @change="onChange"
          />
        </div>
        <div class="replica-box">
          <label>是否要限流：</label
          ><a-input-number
            id="inputNumber"
            v-model="data.interBrokerThrottle"
            :min="-1"
            :max="102400"
          />
          <strong>
            |说明：broker之间副本同步带宽限制，默认值为-1表示不限制，不是-1表示限制，该值并不表示流速，至于流速配置，在
            <span style="color: red">运维->配置限流</span> 处进行操作.</strong
          >
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
        <p>
          *正在进行即尚未完成的副本变更的任务，可以在
          <span style="color: red">运维->副本变更详情</span>
          处查看，也可以在那里将正在进行的任务取消。
        </p>
        <p>
          *如果是减少副本，不用限流。如果是增加副本数，副本同步的时候如果有大量消息需要同步，可能占用大量带宽，担心会影响集群的稳定，考虑是否开启限流。同步完成可以再把该topic的限流关毕。关闭操作可以点击
          限流按钮 处理。
        </p>
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
      brokerIdList: [],
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
        this.brokerIdList = res.data.nodes.map((o) => o.id);
        this.brokerIdList.sort((a, b) => a - b);
      });
    },
    handleCancel() {
      this.data = {};
      this.$emit("closeUpdateReplicaDialog", { refresh: false });
    },
    onChange(value) {
      if (value < 1 || value > this.brokerSize) {
        return false;
      }
      if (this.data.partitions.length > 0) {
        this.data.partitions.forEach((p) => {
          if (value > p.replicas.length) {
            let min = this.brokerIdList[0];
            let max = this.brokerIdList[this.brokerSize - 1] + 1;
            let num = p.replicas[p.replicas.length - 1];
            for (let i = p.replicas.length; i < value; i++) {
              ++num;
              if (num < max) {
                p.replicas.push(num);
              } else {
                p.replicas.push((num % max) + min);
              }
            }
          }
          if (value < p.replicas.length) {
            for (let i = p.replicas.length; i > value; i--) {
              p.replicas.pop();
            }
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
          this.$emit("closeUpdateReplicaDialog", { refresh: false });
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

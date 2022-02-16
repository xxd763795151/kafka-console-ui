<template>
  <a-modal
    title="副本重分配"
    :visible="show"
    :width="800"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :form="form"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @submit="handleSubmit"
        >
          <a-form-item label="Topic">
            <a-select
              @change="handleTopicChange"
              show-search
              option-filter-prop="children"
              v-decorator="[
                'topic',
                { rules: [{ required: true, message: '请选择一个topic!' }] },
              ]"
              placeholder="请选择一个topic"
            >
              <a-select-option v-for="v in topicList" :key="v" :value="v">
                {{ v }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="分配到Broker">
            <a-select
              mode="multiple"
              option-filter-prop="children"
              v-decorator="[
                'brokers',
                {
                  initialValue: brokers,
                  rules: [{ required: true, message: '请选择一个broker!' }],
                },
              ]"
              placeholder="请选择一个broker"
            >
              <a-select-option v-for="v in brokers" :key="v" :value="v">
                <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-table
            bordered
            :columns="columns"
            :data-source="currentAssignment"
            :rowKey="
              (record, index) => {
                return index;
              }
            "
          >
          </a-table>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit">
              重新生成分配计划
            </a-button>
          </a-form-item>
        </a-form>
        <hr />
        <h2>新的分配计划</h2>
        <a-table
          bordered
          :columns="columns"
          :data-source="proposedAssignmentShow"
          :rowKey="
            (record, index) => {
              return index;
            }
          "
        >
        </a-table>
        <a-button type="danger" @click="updateAssignment"> 更新分配 </a-button>
      </a-spin>
      <hr />
      <h4>注意</h4>
      <ul>
        <li>
          副本重分配，可以将副本分配到其它broker上，通过选择上面的broker节点，根据这几个节点生成分配方案
        </li>
        <li>
          选择的broker的节点数量不能少于当前的副本数，比如有3个副本，至少需要3个broker节点
        </li>
        <li>
          数据量太大，考虑设置一下限流，毕竟重新分配后，不同broker之间可能做数据迁移
        </li>
      </ul>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi, KafkaOpApi, KafkaClusterApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
export default {
  name: "ReplicaReassign",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: this.visible,
      data: [],
      loading: false,
      form: this.$form.createForm(this, { name: "ReplicaReassignForm" }),
      topicList: [],
      partitions: [],
      brokers: [],
      currentAssignment: [],
      proposedAssignment: [],
      proposedAssignmentShow: [],
      columns,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.clearData();
        this.getTopicNameList();
        this.getClusterInfo();
      }
    },
  },
  methods: {
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          this.getProposedAssignment(values);
        }
      });
    },
    getTopicReplicaInfo(topic) {
      this.loading = true;
      request({
        url: KafkaTopicApi.getCurrentReplicaAssignment.url + "?topic=" + topic,
        method: KafkaTopicApi.getCurrentReplicaAssignment.method,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.currentAssignment = res.data.partitions;
          this.currentAssignment.forEach(
            (e) => (e.replicas = e.replicas.join(","))
          );
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    getTopicNameList() {
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
      }).then((res) => {
        if (res.code == 0) {
          this.topicList = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    getPartitionInfo(topic) {
      this.loading = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + topic,
        method: KafkaTopicApi.getPartitionInfo.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.partitions = res.data.map((v) => v.partition);
          this.partitions.splice(0, 0, -1);
        }
      });
    },
    handleTopicChange(topic) {
      // this.getPartitionInfo(topic);
      this.clearData();
      this.getTopicReplicaInfo(topic);
    },
    getClusterInfo() {
      this.loading = true;
      request({
        url: KafkaClusterApi.getClusterInfo.url,
        method: KafkaClusterApi.getClusterInfo.method,
      }).then((res) => {
        this.loading = false;
        this.brokers = [];
        res.data.nodes.forEach((node) => this.brokers.push(node.id));
      });
    },
    getProposedAssignment(params) {
      this.loading = true;
      request({
        url: KafkaOpApi.proposedAssignment.url,
        method: KafkaOpApi.proposedAssignment.method,
        data: params,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.proposedAssignmentShow = res.data;
          this.proposedAssignment = JSON.parse(
            JSON.stringify(this.proposedAssignmentShow)
          );
          this.proposedAssignmentShow.forEach(
            (e) => (e.replicas = e.replicas.join(","))
          );
        }
      });
    },
    clearData() {
      this.currentAssignment = [];
      this.proposedAssignment = [];
      this.proposedAssignmentShow = [];
    },
    handleCancel() {
      this.data = [];
      this.$emit("closeReplicaReassignDialog", { refresh: false });
    },
    updateAssignment() {
      this.form.validateFields((err, values) => {
        if (!err) {
          if (this.proposedAssignment.length == 0) {
            this.$message.warn("请先生成分配计划！");
            return;
          }
          this.loading = true;
          request({
            url: KafkaTopicApi.updateReplicaAssignment.url,
            method: KafkaTopicApi.updateReplicaAssignment.method,
            data: { partitions: this.proposedAssignment },
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.handleTopicChange(values.topic);
            } else {
              notification.error({
                message: "error",
                description: res.msg,
              });
            }
          });
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
    title: "副本所在broker",
    dataIndex: "replicas",
    key: "replicas",
    scopedSlots: { customRender: "replicas" },
  },
];
</script>

<style scoped></style>

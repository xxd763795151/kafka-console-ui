<template>
  <a-modal
    :title="'Topic: ' + topic"
    :visible="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div v-for="(v, k) in data" :key="k">
          <strong>消费组: </strong><span class="color-font">{{ k }}</span
          ><strong> | 积压: </strong><span class="color-font">{{ v.lag }}</span>
          <a-button
            type="primary"
            icon="reload"
            size="small"
            style="float: right"
            @click="getConsumerDetail"
          >
            刷新
          </a-button>
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
  name: "ConsumedDetail",
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
      showResetPartitionOffsetDialog: false,
      select: {
        topic: "",
        partition: 0,
      },
      resetPartitionOffsetForm: this.$form.createForm(this, {
        name: "resetPartitionOffsetForm",
      }),
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
        url:
          KafkaConsumerApi.getTopicSubscribedByGroups.url +
          "?topic=" +
          this.topic,
        method: KafkaConsumerApi.getTopicSubscribedByGroups.method,
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
      this.$emit("closeConsumedDetailDialog", {});
    },
    resetTopicOffsetToEndpoint(groupId, topic, type) {
      this.requestResetOffset({
        groupId: groupId,
        topic: topic,
        level: 1,
        type: type,
      });
    },
    requestResetOffset(data, callbackOnSuccess) {
      this.loading = true;
      request({
        url: KafkaConsumerApi.resetOffset.url,
        method: KafkaConsumerApi.resetOffset.method,
        data: data,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.$message.success(res.msg);
          this.getConsumerDetail();
          if (callbackOnSuccess) {
            callbackOnSuccess();
          }
        }
      });
    },
    openResetPartitionOffsetDialog(topic, partition) {
      this.showResetPartitionOffsetDialog = true;
      this.select.topic = topic;
      this.select.partition = partition;
    },
    closeResetPartitionOffsetDialog() {
      this.showResetPartitionOffsetDialog = false;
    },
    resetPartitionOffset() {
      this.resetPartitionOffsetForm.validateFields((err, values) => {
        if (!err) {
          const data = Object.assign({}, values);
          Object.assign(data, this.select);
          data.groupId = this.group;
          data.level = 2;
          data.type = 4;
          this.requestResetOffset(data, this.closeResetPartitionOffsetDialog());
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
    title: "客户端",
    dataIndex: "clientId",
    key: "clientId",
    scopedSlots: { customRender: "clientId" },
    width: 400,
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

<style scoped>
.color-font {
  color: dodgerblue;
}
#resetPartitionOffsetModal .ant-input-number {
  width: 100% !important;
}
</style>

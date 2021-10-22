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
          <strong>Topic: </strong><span class="color-font">{{ k }}</span
          ><strong> | 积压: </strong><span class="color-font">{{ v.lag }}</span>
          <strong> | 重置消费位点->: </strong>
          <a-popconfirm
            :title="
              '重置topic下列所有分区: ' + k + '的消费位点为最小位点，从头消费？'
            "
            ok-text="确认"
            cancel-text="取消"
            @confirm="resetTopicOffsetToEndpoint(group, k, 1)"
          >
            <a-button size="small" type="danger" style="margin-right: 1%"
              >最小位点
            </a-button>
          </a-popconfirm>
          <a-popconfirm
            :title="
              '重置topic下列所有分区: ' + k + '的消费位点为最新位点，继续消费？'
            "
            ok-text="确认"
            cancel-text="取消"
            @confirm="resetTopicOffsetToEndpoint(group, k, 2)"
          >
            <a-button size="small" type="danger" style="margin-right: 1%"
              >最新位点
            </a-button>
          </a-popconfirm>

          <a-button size="small" type="danger" style="margin-right: 1%"
            >时间戳
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
            <div slot="operation" slot-scope="{}">
              <a-button
                type="primary"
                size="small"
                href="javascript:;"
                class="operation-btn"
                >重置位点
              </a-button>
            </div>
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
    resetTopicOffsetToEndpoint(groupId, topic, type) {
      this.loading = true;
      request({
        url: KafkaConsumerApi.resetOffset.url,
        method: KafkaConsumerApi.resetOffset.method,
        data: { groupId: groupId, topic: topic, level: 1, type: type },
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
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
    width: 500,
  },
];
</script>

<style scoped>
.color-font {
  color: dodgerblue;
}
</style>

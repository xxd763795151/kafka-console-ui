<template>
  <a-modal
    :title="'消费组: ' + group"
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

          <a-button
            size="small"
            type="danger"
            style="margin-right: 1%"
            @click="openResetOffsetByTimeDialog(k)"
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
            <div slot="operation" slot-scope="record">
              <a-button
                type="primary"
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="
                  openResetPartitionOffsetDialog(record.topic, record.partition)
                "
                >重置位点
              </a-button>
            </div>
          </a-table>
          <p>
            <strong style="color: red"
              >注意：重置位点时，要求当前没有正在运行的消费端，否则重置的时候会报错，返回失败信息</strong
            >
          </p>
        </div>

        <a-modal
          id="resetPartitionOffsetModal"
          :visible="showResetPartitionOffsetDialog"
          :title="'重置' + select.topic + '[' + select.partition + ']消费位点'"
          :destroyOnClose="true"
          @cancel="closeResetPartitionOffsetDialog"
        >
          <template slot="footer">
            <a-button key="back" @click="closeResetPartitionOffsetDialog">
              取消
            </a-button>
            <a-button key="submit" type="primary" @click="resetPartitionOffset">
              确认
            </a-button>
          </template>

          <a-form
            :form="resetPartitionOffsetForm"
            :label-col="{ span: 8 }"
            :wrapper-col="{ span: 12 }"
          >
            <a-form-item label="重置消费位点到">
              <a-input-number
                :min="0"
                v-decorator="[
                  'offset',
                  {
                    initialValue: 0,
                    rules: [{ required: true, message: '输入消费位点!' }],
                  },
                ]"
              />
            </a-form-item>
          </a-form>
        </a-modal>
        <ResetOffsetByTime
          :visible="showResetOffsetByTimeDialog"
          :group="group"
          :topic="select.topic"
          @closeResetOffsetByTimeDialog="closeResetOffsetByTimeDialog"
        ></ResetOffsetByTime>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaConsumerApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import ResetOffsetByTime from "@/views/group/ResetOffsetByTime";

export default {
  name: "ConsumerDetail",
  components: { ResetOffsetByTime },
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
      showResetPartitionOffsetDialog: false,
      select: {
        topic: "",
        partition: 0,
      },
      resetPartitionOffsetForm: this.$form.createForm(this, {
        name: "resetPartitionOffsetForm",
      }),
      showResetOffsetByTimeDialog: false,
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
    openResetOffsetByTimeDialog(topic) {
      this.select.topic = topic;
      this.showResetOffsetByTimeDialog = true;
    },
    closeResetOffsetByTimeDialog(params) {
      this.showResetOffsetByTimeDialog = false;
      if (params.refresh) {
        this.getConsumerDetail();
      }
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
#resetPartitionOffsetModal .ant-input-number {
  width: 100% !important;
}
</style>

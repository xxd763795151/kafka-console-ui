<template>
  <a-modal
    title="消息详情"
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
        <div>
          <h4>消息信息</h4>
          <hr />
          <div class="message-detail" id="message-detail">
            <p>
              <label class="title">Topic: </label>
              <span class="m-info">{{ data.topic }}</span>
            </p>
            <p>
              <label class="title">分区: </label>
              <span class="m-info">{{ data.partition }}</span>
            </p>
            <p>
              <label class="title">偏移: </label>
              <span class="m-info">{{ data.offset }}</span>
            </p>
            <p>
              <label class="title">消息头: </label>
              <span class="m-info">{{ data.headers }}</span>
            </p>
            <p>
              <label class="title">时间类型: </label>
              <span class="m-info"
                >{{
                  data.timestampType
                }}(表示下面的时间是哪种类型：消息创建、写入日志亦或其它)</span
              >
            </p>
            <p>
              <label class="title">时间: </label>
              <span class="m-info">{{ formatTime(data.timestamp) }}</span>
            </p>
            <p>
              <label class="title">Key反序列化: </label>
              <a-select
                style="width: 120px"
                v-model="keyDeserializer"
                @change="keyDeserializerChange"
              >
                <a-select-option
                  v-for="v in deserializerList"
                  :key="v"
                  :value="v"
                >
                  {{ v }}
                </a-select-option>
              </a-select>
              <span>选一个合适反序列化器，要不可能乱码了</span>
            </p>
            <p>
              <label class="title">Key: </label>
              <span class="m-info">{{ data.key }}</span>
            </p>
            <p>
              <label class="title">消息体反序列化: </label>
              <a-select
                v-model="valueDeserializer"
                style="width: 120px"
                @change="valueDeserializerChange"
              >
                <a-select-option
                  v-for="v in deserializerList"
                  :key="v"
                  :value="v"
                >
                  {{ v }}
                </a-select-option>
              </a-select>
              <span>选一个合适反序列化器，要不可能乱码了</span>
            </p>
            <p>
              <label class="title">消息体: </label>
              <a-textarea
                type="textarea"
                :value="data.value"
                :rows="5"
                :read-only="true"
              ></a-textarea>
            </p>
          </div>
        </div>
        <div>
          <h4>消费信息</h4>
          <hr />
          <a-table
            :columns="columns"
            :data-source="data.consumers"
            bordered
            row-key="groupId"
          >
            <div slot="status" slot-scope="text">
              <span v-if="text == 'consumed'">已消费</span
              ><span v-else style="color: red">未消费</span>
            </div>
          </a-table>
        </div>
        <div>
          <h4>操作</h4>
          <hr />
          <a-popconfirm
            title="确定将当前这条消息重新发回broker？"
            ok-text="确认"
            cancel-text="取消"
            @confirm="resend"
          >
            <a-button type="primary" icon="reload"> 重新发送 </a-button>
          </a-popconfirm>
        </div>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaMessageApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import moment from "moment";

export default {
  name: "MessageDetail",
  props: {
    record: {},
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: this.visible,
      data: {},
      loading: false,
      deserializerList: [],
      keyDeserializer: "String",
      valueDeserializer: "String",
      consumerDetail: [],
      columns,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getMessageDetail();
        this.getDeserializerList();
      }
    },
  },
  methods: {
    getMessageDetail() {
      this.loading = true;
      const params = Object.assign({}, this.record, {
        keyDeserializer: this.keyDeserializer,
        valueDeserializer: this.valueDeserializer,
      });
      request({
        url: KafkaMessageApi.searchDetail.url,
        method: KafkaMessageApi.searchDetail.method,
        data: params,
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
    getDeserializerList() {
      request({
        url: KafkaMessageApi.deserializerList.url,
        method: KafkaMessageApi.deserializerList.method,
      }).then((res) => {
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.deserializerList = res.data;
        }
      });
    },
    handleCancel() {
      this.data = {};
      this.$emit("closeDetailDialog", { refresh: false });
    },
    formatTime(time) {
      return time == -1 ? -1 : moment(time).format("YYYY-MM-DD HH:mm:ss:SSS");
    },
    keyDeserializerChange() {
      this.getMessageDetail();
    },
    valueDeserializerChange() {
      this.getMessageDetail();
    },
    resend() {
      const params = Object.assign({}, this.data);
      this.loading = true;
      request({
        url: KafkaMessageApi.resend.url,
        method: KafkaMessageApi.resend.method,
        data: params,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.$message.success(res.msg);
        }
      });
    },
  },
};
const columns = [
  {
    title: "消费组",
    dataIndex: "groupId",
    key: "groupId",
  },
  {
    title: "消费情况",
    dataIndex: "status",
    key: "status",
    scopedSlots: { customRender: "status" },
  },
];
</script>

<style scoped>
.m-info {
  /*text-decoration: underline;*/
}
.title {
  width: 15%;
  display: inline-block;
  text-align: right;
  margin-right: 2%;
  font-weight: bold;
}
.ant-spin-container #message-detail textarea {
  max-width: 80% !important;
  vertical-align: top !important;
}
</style>

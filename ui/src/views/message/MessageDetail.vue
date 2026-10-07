<template>
  <a-modal
    title="消息详情"
    :open="show"
    :width="800"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
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
                :filter-option="true"
                option-filter-prop="label"
              >
                <a-select-option
                  v-for="v in deserializerList"
                  :key="v"
                  :value="v"
                  :label="String(v)"
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
                :filter-option="true"
                option-filter-prop="label"
              >
                <a-select-option
                  v-for="v in deserializerList"
                  :key="v"
                  :value="v"
                  :label="String(v)"
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
            <template #status="text">
              <span v-if="text == 'consumed'">已消费</span
              ><span v-else style="color: red">未消费</span>
            </template>
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
            <a-button type="primary" v-action:message:resend>
              <template #icon><ReloadOutlined /></template>
              重新发送
            </a-button>
          </a-popconfirm>
          <a-button
            type="dashed"
            class="mar-left"
            v-action:message:forward
            @click="openForwardDialog()"
          >
            <template #icon><PlusOutlined /></template>
            转发消息
          </a-button>
        </div>
      </a-spin>
      <ForwardMessage
        :visible="showForwardDialog"
        :record="data"
        @closeForwardDialog="closeForwardDialog"
      ></ForwardMessage>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, watch } from "vue";
import { ReloadOutlined, PlusOutlined } from "@ant-design/icons-vue";
import request from "@/utils/request";
import { KafkaMessageApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import dayjs from "dayjs";
import ForwardMessage from "@/views/message/ForwardMessage.vue";
import { message } from "ant-design-vue";

interface RecordItem {
  [key: string]: any;
}

interface ConsumerItem {
  groupId: string;
  status: string;
  [key: string]: any;
}

interface DataItem {
  topic?: string;
  partition?: number;
  offset?: number;
  headers?: string;
  timestampType?: string;
  timestamp?: number;
  key?: string;
  value?: string;
  consumers?: ConsumerItem[];
  [key: string]: any;
}

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
    customRender: ({ text }: { text: string }) => text,
  },
];

export default defineComponent({
  name: "MessageDetail",
  components: { ForwardMessage, ReloadOutlined, PlusOutlined },
  props: {
    record: {
      type: Object,
      default: () => ({}),
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const state = reactive({
      show: props.visible,
      data: {} as DataItem,
      loading: false,
      deserializerList: [] as string[],
      keyDeserializer: "String",
      valueDeserializer: "String",
      consumerDetail: [] as any[],
      showForwardDialog: false,
    });

    watch(
      () => props.visible,
      (v: boolean) => {
        state.show = v;
        if (state.show) {
          getMessageDetail();
          getDeserializerList();
        }
      }
    );

    const getMessageDetail = () => {
      state.loading = true;
      const params = Object.assign({}, props.record, {
        keyDeserializer: state.keyDeserializer,
        valueDeserializer: state.valueDeserializer,
      });
      request({
        url: KafkaMessageApi.searchDetail.url,
        method: KafkaMessageApi.searchDetail.method,
        data: params,
      }).then((res: any) => {
        state.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          state.data = res.data;
        }
      });
    };

    const getDeserializerList = () => {
      request({
        url: KafkaMessageApi.deserializerList.url,
        method: KafkaMessageApi.deserializerList.method,
      }).then((res: any) => {
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          state.deserializerList = res.data;
        }
      });
    };

    const handleCancel = () => {
      state.data = {};
      emit("closeDetailDialog", { refresh: false });
    };

    const formatTime = (time: number) => {
      return time == -1 ? -1 : dayjs(time).format("YYYY-MM-DD HH:mm:ss:SSS");
    };

    const keyDeserializerChange = () => {
      getMessageDetail();
    };

    const valueDeserializerChange = () => {
      getMessageDetail();
    };

    const resend = () => {
      const params = Object.assign({}, state.data);
      state.loading = true;
      request({
        url: KafkaMessageApi.resend.url,
        method: KafkaMessageApi.resend.method,
        data: params,
      }).then((res: any) => {
        state.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          message.success(res.msg);
        }
      });
    };

    const openForwardDialog = () => {
      state.showForwardDialog = true;
    };

    const closeForwardDialog = () => {
      state.showForwardDialog = false;
    };

    return {
      ...toRefs(state),
      columns,
      getMessageDetail,
      getDeserializerList,
      handleCancel,
      formatTime,
      keyDeserializerChange,
      valueDeserializerChange,
      resend,
      openForwardDialog,
      closeForwardDialog,
    };
  },
});
</script>

<style scoped>
.m-info {
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
.mar-left {
  margin-left: 1%;
}
</style>

<template>
  <a-modal
    :title="'消费组: ' + group"
    v-model:open="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div v-for="(v, k) in data" :key="k as string">
          <strong>Topic: </strong><span class="color-font">{{ k as string }}</span
          ><strong> | 积压: </strong><span class="color-font">{{ v.lag }}</span>
          <strong> | 重置消费位点->: </strong>
          <a-popconfirm
            :title="
              '重置topic下列所有分区: ' + k + '的消费位点为最小位点，从头消费？'
            "
            ok-text="确认"
            cancel-text="取消"
            @confirm="resetTopicOffsetToEndpoint(group, k as string, 1)"
          >
            <a-button
              size="small"
              type="primary"
              danger
              style="margin-right: 1%"
              v-action:group:consumer-detail:min
              >最小位点
            </a-button>
          </a-popconfirm>
          <a-popconfirm
            :title="
              '重置topic下列所有分区: ' + k + '的消费位点为最新位点，继续消费？'
            "
            ok-text="确认"
            cancel-text="取消"
            @confirm="resetTopicOffsetToEndpoint(group, k as string, 2)"
          >
            <a-button
              size="small"
              type="primary"
              danger
              style="margin-right: 1%"
              v-action:group:consumer-detail:last
              >最新位点
            </a-button>
          </a-popconfirm>

          <a-button
            size="small"
            type="primary"
            danger
            style="margin-right: 1%"
            @click="openResetOffsetByTimeDialog(k as string)"
            v-action:group:consumer-detail:timestamp
            >时间戳
          </a-button>
          <a-button
            type="primary"
            size="small"
            style="float: right"
            @click="getConsumerDetail"
          >
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
          <hr />
          <a-table
            :columns="columns"
            :data-source="v.data"
            bordered
            :rowKey="(record: any) => record.topic + record.partition"
          >
            <template #bodyCell="{ column, text, record }">
              <template v-if="column.key === 'clientId'">
                <span v-if="text"> {{ text }}@{{ record.host }} </span>
              </template>
              <template v-else-if="column.key === 'operation'">
                <a-button
                  type="primary"
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="
                    openResetPartitionOffsetDialog(record.topic, record.partition)
                  "
                  v-action:group:consumer-detail:any
                  >重置位点
                </a-button>
              </template>
            </template>
          </a-table>
          <p>
            <strong style="color: red"
              >注意：重置位点时，要求当前没有正在运行的消费端，否则重置的时候会报错，返回失败信息</strong
            >
          </p>
        </div>

        <a-modal
          id="resetPartitionOffsetModal"
          v-model:open="showResetPartitionOffsetDialog"
          :title="'重置' + select.topic + '[' + select.partition + ']消费位点'"
          :destroyOnClose="true"
          @cancel="closeResetPartitionOffsetDialog"
        >
          <template #footer>
            <a-button key="back" @click="closeResetPartitionOffsetDialog">
              取消
            </a-button>
            <a-button key="submit" type="primary" @click="resetPartitionOffset">
              确认
            </a-button>
          </template>

          <a-form
            ref="resetPartitionOffsetFormRef"
            :model="resetPartitionOffsetFormState"
            :label-col="{ span: 8 }"
            :wrapper-col="{ span: 12 }"
          >
            <a-form-item
              label="重置消费位点到"
              name="offset"
              :rules="[{ required: true, message: '输入消费位点!' }]"
            >
              <a-input-number
                v-model:value="resetPartitionOffsetFormState.offset"
                :min="0"
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

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import { ReloadOutlined } from '@ant-design/icons-vue';
import request from '@/utils/request';
import { KafkaConsumerApi } from '@/utils/api';
import { notification } from 'ant-design-vue';
import ResetOffsetByTime from '@/views/group/ResetOffsetByTime.vue';

export default defineComponent({
  name: 'ConsumerDetail',
  components: { ReloadOutlined, ResetOffsetByTime },
  props: {
    group: {
      type: String,
      default: '',
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['closeConsumerDetailDialog', 'update:visible'],
  setup() {
    const resetPartitionOffsetFormRef = ref();
    const resetPartitionOffsetFormState = reactive({
      offset: 0,
    });
    return {
      resetPartitionOffsetFormRef,
      resetPartitionOffsetFormState,
    };
  },
  data() {
    return {
      columns,
      show: this.visible,
      data: {} as Record<string, any>,
      loading: false,
      showResetPartitionOffsetDialog: false,
      select: {
        topic: '',
        partition: 0,
      },
      showResetOffsetByTimeDialog: false,
    };
  },
  watch: {
    visible(v: boolean) {
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
        url: KafkaConsumerApi.getConsumerDetail.url + '?groupId=' + this.group,
        method: KafkaConsumerApi.getConsumerDetail.method,
      }).then((res: any) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        } else {
          this.data = res.data;
        }
      });
    },
    handleCancel() {
      this.data = {};
      this.$emit('update:visible', false);
      this.$emit('closeConsumerDetailDialog', {});
    },
    resetTopicOffsetToEndpoint(groupId: string, topic: string, type: number) {
      this.requestResetOffset({
        groupId: groupId,
        topic: topic,
        level: 1,
        type: type,
      });
    },
    requestResetOffset(data: any, callbackOnSuccess?: () => void) {
      this.loading = true;
      request({
        url: KafkaConsumerApi.resetOffset.url,
        method: KafkaConsumerApi.resetOffset.method,
        data: data,
      }).then((res: any) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: 'error',
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
    openResetPartitionOffsetDialog(topic: string, partition: number) {
      this.showResetPartitionOffsetDialog = true;
      this.select.topic = topic;
      this.select.partition = partition;
    },
    closeResetPartitionOffsetDialog() {
      this.showResetPartitionOffsetDialog = false;
    },
    async resetPartitionOffset() {
      try {
        await this.resetPartitionOffsetFormRef.validate();
      } catch {
        return;
      }
      const values = { ...this.resetPartitionOffsetFormState };
      const data = Object.assign({}, values);
      Object.assign(data, this.select);
      (data as any).groupId = this.group;
      (data as any).level = 2;
      (data as any).type = 4;
      this.requestResetOffset(data, this.closeResetPartitionOffsetDialog);
    },
    openResetOffsetByTimeDialog(topic: string) {
      this.select.topic = topic;
      this.showResetOffsetByTimeDialog = true;
    },
    closeResetOffsetByTimeDialog(params: any) {
      this.showResetOffsetByTimeDialog = false;
      if (params.refresh) {
        this.getConsumerDetail();
      }
    },
  },
});

const columns = [
  {
    title: '分区',
    dataIndex: 'partition',
    key: 'partition',
  },
  {
    title: '客户端',
    dataIndex: 'clientId',
    key: 'clientId',
    width: 400,
  },
  {
    title: '日志位点',
    dataIndex: 'logEndOffset',
    key: 'logEndOffset',
  },
  {
    title: '消费位点',
    dataIndex: 'consumerOffset',
    key: 'consumerOffset',
  },
  {
    title: '积压',
    dataIndex: 'lag',
    key: 'lag',
  },
  {
    title: '操作',
    key: 'operation',
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

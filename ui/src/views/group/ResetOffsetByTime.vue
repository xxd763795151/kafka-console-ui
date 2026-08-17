<template>
  <a-modal
    title="重置消费位点"
    v-model:open="show"
    :width="600"
    :mask="false"
    :destroyOnClose="true"
    :maskClosable="false"
    @cancel="handleCancel"
    @ok="resetOffset"
    okText="提交"
    cancelText="取消"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :model="formState"
          :label-col="{ span: 8 }"
          :wrapper-col="{ span: 12 }"
        >
          <a-form-item
            label="重置消费位点到"
            name="dateTime"
            :rules="[{ required: true, message: '输入消费位点!' }]"
          >
            <a-date-picker
              v-model:value="formState.dateTime"
              show-time
              placeholder="选择重置到哪个时间"
            />
          </a-form-item>
        </a-form>
        <hr />
        <p>
          *注意：该时间为北京时间。这里固定为东8区的计算时间，如果所在地区不是采用北京时间（中国大部分地区都是采用的北京时间），请自行对照为当地时间重置。
        </p>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import request from '@/utils/request';
import { KafkaConsumerApi } from '@/utils/api';
import { notification } from 'ant-design-vue';
import dayjs from 'dayjs';

export default defineComponent({
  name: 'ResetOffsetByTime',
  props: {
    group: {
      type: String,
      default: '',
    },
    topic: {
      type: String,
      default: '',
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['closeResetOffsetByTimeDialog', 'update:visible'],
  setup() {
    const formState = reactive({
      dateTime: undefined as dayjs.Dayjs | undefined,
    });
    return {
      formState,
    };
  },
  data() {
    return {
      show: this.visible,
      loading: false,
    };
  },
  watch: {
    visible(v: boolean) {
      this.show = v;
    },
  },
  methods: {
    handleCancel() {
      this.$emit('update:visible', false);
      this.$emit('closeResetOffsetByTimeDialog', {});
    },
    async resetOffset() {
      const v = { ...this.formState };
      const dateStr = dayjs(v.dateTime).format('YYYY-MM-DD HH:mm:ss');
      const data: any = { dateStr };
      data.groupId = this.group;
      data.topic = this.topic;
      data.level = 1;
      data.type = 3;
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
          this.$emit('closeResetOffsetByTimeDialog', { refresh: true });
        }
      });
    },
  },
});
</script>

<style scoped></style>

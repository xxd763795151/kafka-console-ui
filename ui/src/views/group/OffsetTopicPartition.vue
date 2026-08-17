<template>
  <a-modal
    title="位移主题分区"
    v-model:open="show"
    :width="800"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        {{ group }}提交位移到位移主题的[{{ data }}]分区
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import request from '@/utils/request';
import { KafkaConsumerApi } from '@/utils/api';
import { notification } from 'ant-design-vue';

export default defineComponent({
  name: 'OffsetTopicPartition',
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
  emits: ['closeOffsetPartitionDialog', 'update:visible'],
  data() {
    return {
      show: this.visible,
      data: [] as any[],
      loading: false,
    };
  },
  watch: {
    visible(v: boolean) {
      this.show = v;
      if (this.show) {
        this.getOffsetPartition();
      }
    },
  },
  methods: {
    getOffsetPartition() {
      this.loading = true;
      request({
        url: KafkaConsumerApi.getOffsetPartition.url + '?groupId=' + this.group,
        method: KafkaConsumerApi.getOffsetPartition.method,
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
      this.data = [];
      this.$emit('update:visible', false);
      this.$emit('closeOffsetPartitionDialog', {});
    },
  },
});
</script>

<style scoped></style>

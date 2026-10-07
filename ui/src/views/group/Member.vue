<template>
  <a-modal
    title="消费端成员"
    v-model:open="show"
    :width="1300"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="data"
          bordered
          :rowKey="(record: any) => record.memberId"
        >
          <template #bodyCell="{ column, text }">
            <template v-if="column.key === 'partitions'">
              <ul>
                <ol v-for="i in text" :key="i.topic + i.partition">
                  {{ i.topic }}: {{ i.partition }}
                </ol>
              </ul>
            </template>
          </template>
        </a-table>
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
  name: 'Member',
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
  emits: ['closeConsumerMemberDialog', 'update:visible'],
  data() {
    return {
      columns,
      show: this.visible,
      data: [] as any[],
      loading: false,
    };
  },
  watch: {
    visible(v: boolean) {
      this.show = v;
      if (this.show) {
        this.getPartitionInfo();
      }
    },
  },
  methods: {
    getPartitionInfo() {
      this.loading = true;
      request({
        url: KafkaConsumerApi.getConsumerMembers.url + '?groupId=' + this.group,
        method: KafkaConsumerApi.getConsumerMembers.method,
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
      this.$emit('closeConsumerMemberDialog', {});
    },
  },
});

const columns = [
  {
    title: '成员ID',
    dataIndex: 'memberId',
    key: 'memberId',
    width: 300,
  },
  {
    title: '客户端ID',
    dataIndex: 'clientId',
    key: 'clientId',
    width: 300,
  },
  {
    title: '实例ID',
    dataIndex: 'groupInstanceId',
    key: 'groupInstanceId',
    width: 150,
  },
  {
    title: '主机',
    dataIndex: 'host',
    key: 'host',
  },
  {
    title: '订阅分区信息',
    dataIndex: 'partitions',
    key: 'partitions',
    width: 300,
  },
];
</script>

<style scoped>
ul ol {
  padding-inline-start: 0px;
}
.ant-table-row-cell-break-word ul {
  padding-inline-start: 0px;
}
</style>

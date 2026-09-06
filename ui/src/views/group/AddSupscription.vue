<template>
  <a-modal
    title="新增订阅关系"
    v-model:open="show"
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
          :model="formState"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @finish="handleSubmit"
        >
          <a-form-item
            label="消费组"
            name="groupId"
            :rules="[{ required: true, message: '输入消费组名称!' }]"
          >
            <a-input
              v-model:value="formState.groupId"
              placeholder="groupId"
            />
          </a-form-item>
          <a-form-item
            label="topic"
            name="topic"
            :rules="[{ required: true, message: '请选择一个topic!' }]"
          >
            <a-select
              v-model:value="formState.topic"
              show-search
              :filter-option="true"
              option-filter-prop="label"
              placeholder="请选择一个topic"
            >
              <a-select-option v-for="v in topicList" :key="v" :value="v" :label="String(v)">
                {{ v }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 提交 </a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import request from '@/utils/request';
import { KafkaTopicApi, KafkaConsumerApi } from '@/utils/api';
import { notification } from 'ant-design-vue';

export default defineComponent({
  name: 'AddSubscription',
  props: {
    topic: {
      type: String,
      default: '',
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['closeAddSubscriptionDialog', 'update:visible'],
  setup() {
    const formState = reactive({
      groupId: '',
      topic: undefined as string | undefined,
    });
    return {
      formState,
    };
  },
  data() {
    return {
      show: this.visible,
      data: [] as any[],
      loading: false,
      topicList: [] as string[],
    };
  },
  watch: {
    visible(v: boolean) {
      this.show = v;
      if (this.show) {
        this.getTopicNameList();
      }
    },
  },
  methods: {
    getTopicNameList() {
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
      }).then((res: any) => {
        if (res.code == 0) {
          this.topicList = res.data;
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    },
    async handleSubmit(values: Record<string, any>) {
      this.loading = true;
      request({
        url: KafkaConsumerApi.addSubscription.url,
        method: KafkaConsumerApi.addSubscription.method,
        data: values,
      }).then((res: any) => {
        this.loading = false;
        if (res.code == 0) {
          this.$message.success(res.msg);
          this.$emit('closeAddSubscriptionDialog', { refresh: true });
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    },
    handleCancel() {
      this.data = [];
      this.$emit('update:visible', false);
      this.$emit('closeAddSubscriptionDialog', { refresh: false });
    },
  },
});
</script>

<style scoped></style>

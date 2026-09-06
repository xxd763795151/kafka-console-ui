<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="search-offset-form-advanced-search">
        <a-form
          ref="formRef"
          class="ant-advanced-search-form"
          :model="formState"
          @finish="handleSearch"
        >
          <a-row :gutter="24">
            <a-col :span="9">
              <a-form-item
                label="topic"
                name="topic"
                :rules="[{ required: true, message: '请选择一个topic!' }]"
              >
                <a-select
                  class="topic-select"
                  @change="handleTopicChange"
                  show-search
                  :filter-option="true"
                  option-filter-prop="label"
                  v-model:value="formState.topic"
                  placeholder="请选择一个topic"
                >
                  <a-select-option v-for="v in topicList" :key="v" :value="v" :label="String(v)">
                    {{ v }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="分区">
                <a-select
                  class="type-select"
                  show-search
                  :filter-option="true"
                  option-filter-prop="label"
                  v-model:value="selectPartition"
                  placeholder="请选择一个分区"
                >
                  <a-select-option
                    v-for="v in partitions"
                    :key="v"
                    :value="v"
                    :label="v == -1 ? '全部' : String(v)"
                  >
                    <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="7">
              <a-form-item
                label="偏移"
                name="offset"
                :rules="[{ required: true, message: '请输入消息偏移!' }]"
              >
                <a-input
                  v-model:value="formState.offset"
                  placeholder="消息偏移"
                />
              </a-form-item>
            </a-col>
            <a-col :span="2" :style="{ textAlign: 'right' }">
              <a-form-item>
                <a-button type="primary" html-type="submit"> 搜索</a-button>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <MessageList :data="data"></MessageList>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, ref } from "vue";
import { message } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaMessageApi, KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import MessageList from "@/views/message/MessageList.vue";

interface FormState {
  topic?: string;
  offset?: string | number;
  [key: string]: any;
}

interface RecordItem {
  [key: string]: any;
}

const defaultData: RecordItem[] = [];

export default defineComponent({
  name: "SearchByOffset",
  components: { MessageList },
  props: {
    topicList: {
      type: Array,
      default: () => [],
    },
  },
  setup() {
    const formRef = ref();
    const formState = reactive<FormState>({
      topic: undefined,
      offset: undefined,
    });

    const state = reactive({
      loading: false,
      partitions: [] as number[],
      selectPartition: undefined as number | undefined,
      rangeConfig: {
        rules: [{ type: "array" as const, required: true, message: "请选择时间!" }],
      },
      data: defaultData as RecordItem[],
    });

    const handleSearch = async () => {
      const data = Object.assign({}, formState, {
        partition: state.selectPartition,
      });
      state.loading = true;
      request({
        url: KafkaMessageApi.searchByOffset.url,
        method: KafkaMessageApi.searchByOffset.method,
        data: data,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          message.success(res.msg);
          state.data = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    const getPartitionInfo = (topic: string) => {
      state.loading = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + topic,
        method: KafkaTopicApi.getPartitionInfo.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          state.partitions = res.data.map((v: any) => v.partition);
          state.partitions.splice(0, 0, -1);
        }
      });
    };

    const handleTopicChange = (topic: string) => {
      state.selectPartition = -1;
      getPartitionInfo(topic);
    };

    return {
      ...toRefs(state),
      formRef,
      formState,
      handleSearch,
      getPartitionInfo,
      handleTopicChange,
    };
  },
});
</script>

<style scoped>
.tab-content {
  width: 100%;
  height: 100%;
}

.ant-advanced-search-form {
  padding: 24px;
  background: #fbfbfb;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.ant-advanced-search-form .ant-form-item {
  display: flex;
}

.ant-advanced-search-form .ant-form-item-control-wrapper {
  flex: 1;
}

#components-form-topic-advanced-search .ant-form {
  max-width: none;
  margin-bottom: 1%;
}

#search-offset-form-advanced-search .search-result-list {
  margin-top: 16px;
  border: 1px dashed #e9e9e9;
  border-radius: 6px;
  background-color: #fafafa;
  min-height: 200px;
  text-align: center;
  padding-top: 80px;
}
.topic-select {
  width: 400px !important;
}
.type-select {
  width: 200px !important;
}
</style>

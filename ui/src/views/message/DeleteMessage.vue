<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div>
        <h4 class="hint-content">
          注意：以下删除，将删除该分区比该偏移位点小的所有消息（不包含该位点）
        </h4>
        <hr />
      </div>
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
                  <a-select-option
                    v-for="v in topicList"
                    :key="v"
                    :value="v"
                    :label="String(v)"
                  >
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
                <a-button type="primary" html-type="submit"> 执行删除</a-button>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, ref } from "vue";
import { message } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaMessageApi, KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

interface FormState {
  topic?: string;
  offset?: string | number;
  [key: string]: any;
}

export default defineComponent({
  name: "DeleteMessage",
  props: {
    topicList: {
      type: Array,
      default: () => [],
    },
  },
  setup(props) {
    const formRef = ref();
    const formState = reactive<FormState>({
      topic: undefined,
      offset: undefined,
    });

    const state = reactive({
      loading: false,
      partitions: [] as number[],
      selectPartition: undefined as number | undefined,
    });

    const handleSearch = async () => {
      const data = Object.assign({}, formState, {
        partition: state.selectPartition,
      });
      state.loading = true;
      request({
        url: KafkaMessageApi.delete.url,
        method: KafkaMessageApi.delete.method,
        data: [data],
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          message.success(res.msg);
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
        }
      });
    };

    const handleTopicChange = (topic: string) => {
      state.selectPartition =
        state.partitions.length > 0 ? state.partitions[0] : 0;
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

.hint-content {
  color: red;
}
</style>

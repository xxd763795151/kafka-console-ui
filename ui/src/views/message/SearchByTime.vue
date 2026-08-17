<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="search-time-form-advanced-search">
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
            <a-col :span="5">
              <a-form-item label="分区">
                <a-select
                  class="type-select"
                  show-search
                  :filter-option="true"
                  option-filter-prop="label"
                  v-model:value="selectPartition"
                  placeholder="请选择一个分区"
                >
                  <a-select-option v-for="v in partitions" :key="v" :value="v">
                    <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                label="时间"
                name="time"
                :rules="rangeConfig.rules"
              >
                <a-range-picker
                  v-model:value="formState.time"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                />
              </a-form-item>
            </a-col>
            <a-col :span="2" :style="{ textAlign: 'right' }">
              <a-form-item>
                <a-button type="primary" html-type="submit"> 搜索</a-button>
              </a-form-item>
            </a-col>
          </a-row>
          <hr class="hr" />
          <a-row :gutter="24">
            <a-col :span="24">
              <a-form-item
                label="最大检索数"
                name="filterNumber"
                :rules="[
                  {
                    required: true,
                    message: '输入消息数!',
                  },
                ]"
              >
                <a-input-number
                  v-model:value="formState.filterNumber"
                  :min="1"
                  :max="100000"
                />
                <span
                  >条
                  注意：这里允许最多检索10万条，但是不建议将该值设置过大，这意味着一次查询要在内存里缓存这么多的数据，可能导致内存溢出；并且更大的消息量会导致更长的检索时间</span
                >
              </a-form-item>
            </a-col>
          </a-row>
          <hr class="hr" />
          <a-row :gutter="24">
            <a-col :span="5">
              <a-form-item
                label="消息过滤"
                name="filter"
              >
                <a-select
                  class="filter-select"
                  :filter-option="true"
                  option-filter-prop="label"
                  v-model:value="formState.filter"
                  @change="onFilterChange"
                >
                  <a-select-option value="none" :label="String('不启用过滤')"> 不启用过滤 </a-select-option>
                  <a-select-option value="body" :label="String('根据消息体过滤')">
                    根据消息体过滤
                  </a-select-option>
                  <a-select-option value="header" :label="String('根据消息头过滤')">
                    根据消息头过滤
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <div v-show="showBodyFilter">
              <a-col :span="8">
                <a-form-item label="消息内容" name="value">
                  <a-input
                    class="msg-body"
                    v-model:value="formState.value"
                    placeholder="请输入消息内容"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="消息类型" name="valueDeserializer">
                  <a-select
                    v-model:value="formState.valueDeserializer"
                    class="body-type"
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
                  <span class="hint"
                    >String类型模糊匹配，数字类型绝对匹配，其它不支持</span
                  >
                </a-form-item>
              </a-col>
            </div>
            <div v-show="showHeaderFilter">
              <a-col :span="5">
                <a-form-item label="Key" name="headerKey">
                  <a-input
                    v-model:value="formState.headerKey"
                    placeholder="消息头的key"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="11">
                <a-form-item label="Value" name="headerValue">
                  <a-input
                    v-model:value="formState.headerValue"
                    placeholder="消息头对应key的value"
                  />
                  <span class="hint"
                    >消息头的value不是字符串类型，就不要输入value用来过滤了，可以只输入消息头的key，过滤存在该key的消息</span
                  >
                </a-form-item>
              </a-col>
            </div>
          </a-row>
        </a-form>
      </div>
      <p style="margin-top: 1%">
        <strong
          >检索消息条数：{{ data.searchNum }}，实际返回条数：{{
            data.realNum
          }}，允许返回的最大条数：{{
            data.maxNum
          }}，如果当前时间段消息量太大，可以缩小查询时间范围或指定某一个分区进行查询</strong
        >
      </p>
      <MessageList :data="data.data"></MessageList>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaMessageApi, KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import MessageList from "@/views/message/MessageList.vue";
import type { Dayjs } from "dayjs";

interface FormState {
  topic?: string;
  time?: [Dayjs, Dayjs];
  filterNumber?: number;
  filter?: string;
  value?: string;
  valueDeserializer?: string;
  headerKey?: string;
  headerValue?: string;
  [key: string]: any;
}

interface SearchData {
  realNum: number;
  maxNum: number;
  searchNum: number;
  data?: any[];
  [key: string]: any;
}

const defaultData: SearchData = { realNum: 0, maxNum: 0, searchNum: 0 };

export default defineComponent({
  name: "SearchByTime",
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
      time: undefined,
      filterNumber: 5000,
      filter: "none",
      value: undefined,
      valueDeserializer: "String",
      headerKey: undefined,
      headerValue: undefined,
    });

    const state = reactive({
      loading: false,
      partitions: [] as number[],
      selectPartition: undefined as number | undefined,
      rangeConfig: {
        rules: [{ type: "array" as const, required: true, message: "请选择时间!" }],
      },
      data: defaultData as SearchData,
      deserializerList: [] as string[],
      showBodyFilter: false,
      showHeaderFilter: false,
    });

    const handleSearch = async () => {
      const data = Object.assign({}, formState, {
        partition: state.selectPartition,
      });
      data.startTime = formState.time?.[0]?.valueOf();
      data.endTime = formState.time?.[1];
      state.loading = true;
      request({
        url: KafkaMessageApi.searchByTime.url,
        method: KafkaMessageApi.searchByTime.method,
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

    const onFilterChange = (e: string) => {
      switch (e) {
        case "body":
          state.showBodyFilter = true;
          state.showHeaderFilter = false;
          break;
        case "header":
          state.showHeaderFilter = true;
          state.showBodyFilter = false;
          break;
        default:
          state.showBodyFilter = false;
          state.showHeaderFilter = false;
          break;
      }
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

    onMounted(() => {
      getDeserializerList();
    });

    return {
      ...toRefs(state),
      formRef,
      formState,
      handleSearch,
      getPartitionInfo,
      handleTopicChange,
      onFilterChange,
      getDeserializerList,
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

#search-time-form-advanced-search .search-result-list {
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

.filter-select {
  width: 160px !important;
}

.body-type {
  width: 120px;
}

.msg-body {
  width: 400px;
}

.type-select {
  width: 150px !important;
}
.hint {
  font-size: smaller;
  color: green;
}
.ant-advanced-search-form {
  padding-bottom: 0px;
}
.hr {
  height: 1px;
  border: none;
  border-top: 1px dashed #0066cc;
}
</style>

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
          <a-row>
            <a-col :span="16">
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
            <a-col :span="8">
              <a-form-item label="分区">
                <a-select
                  class="type-select"
                  show-search
                  mode="multiple"
                  :filter-option="true"
                  option-filter-prop="label"
                  v-model:value="selectPartition"
                  placeholder="请选择分区"
                >
                  <a-select-option v-for="v in partitions" :key="v" :value="v">
                    <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col :span="20">
              <a-form-item
                label="时间"
                name="time"
                :rules="rangeConfig.rules"
              >
                <a-range-picker
                  v-model:value="formState.time"
                  format="YYYY-MM-DD HH:mm:ss.SSS"
                  :show-time="{
                    hideDisabledOptions: true,
                    defaultValue: [
                      dayjs('00:00:00.000', 'HH:mm:ss.SSS'),
                      dayjs('23:59:59.999', 'HH:mm:ss.SSS'),
                    ],
                  }"
                />
              </a-form-item>
            </a-col>
            <a-col :span="2" :style="{ textAlign: 'right' }">
              <a-form-item>
                <a-button type="primary" html-type="submit"> 查询</a-button>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div id="search-result-view">
        <a-collapse>
          <a-collapse-panel
            v-for="(item, index) in data"
            :key="index"
            :header="
              item.topic +
              ', 时间[' +
              item.startTime +
              ' ~ ' +
              item.endTime +
              '], 总数' +
              item.total +
              ', 查询时间' +
              item.searchTime
            "
          >
            <ul>
              <li v-for="(value, key) in item.detail" :key="key">
                分区:{{ key }}, 数量: {{ value }}
              </li>
            </ul>
          </a-collapse-panel>
        </a-collapse>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, ref } from "vue";
import request from "@/utils/request";
import { KafkaMessageApi, KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import dayjs from "dayjs";
import customParseFormat from "dayjs/plugin/customParseFormat";
import type { Dayjs } from "dayjs";

dayjs.extend(customParseFormat);

interface FormState {
  topic?: string;
  time?: [Dayjs, Dayjs];
  [key: string]: any;
}

interface StatisticsDetail {
  [partition: string]: number;
}

interface StatisticsItem {
  topic: string;
  startTime: string;
  endTime: string;
  total: number;
  searchTime: string;
  detail: StatisticsDetail;
  [key: string]: any;
}

export default defineComponent({
  name: "SendStatistics",
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
    });

    const state = reactive({
      dayjs,
      locale: {} as any,
      loading: false,
      partitions: [] as number[],
      selectPartition: [] as number[],
      rangeConfig: {
        rules: [{ type: "array" as const, required: true, message: "请选择时间!" }],
      },
      data: [] as StatisticsItem[],
    });

    const handleSearch = async () => {
      const data: any = Object.assign({}, formState);
      delete data.time;
      data.startTime = formState.time?.[0];
      data.endTime = formState.time?.[1];
      data.partition = Array.isArray(state.selectPartition)
        ? state.selectPartition
        : [state.selectPartition];
      state.loading = true;
      request({
        url: KafkaMessageApi.sendStatistics.url,
        method: KafkaMessageApi.sendStatistics.method,
        data: data,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          state.data.splice(0, 0, res.data);
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
      state.selectPartition = [-1] as any;
      getPartitionInfo(topic);
    };

    const getCurrentTime = () => {
      const date = new Date();
      const yy = date.getFullYear();
      const month = date.getMonth() + 1;
      const mm = month < 10 ? "0" + month : month;
      const day = date.getDate();
      const dd = day < 10 ? "0" + day : day;
      const hh = date.getHours();
      const minutes = date.getMinutes();
      const mf = minutes < 10 ? "0" + minutes : minutes;
      const seconds = date.getSeconds();
      const ss = seconds < 10 ? "0" + seconds : seconds;
      return yy + "-" + mm + "-" + dd + " " + hh + ":" + mf + ":" + ss;
    };

    return {
      ...toRefs(state),
      formRef,
      formState,
      handleSearch,
      getPartitionInfo,
      handleTopicChange,
      getCurrentTime,
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
  width: 500px !important;
}

.ant-calendar-picker {
  width: 500px !important;
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
  padding-right: 10%;
}
#search-result-view ul {
  list-style-type: none;
  padding-left: 0px;
  margin-top: 1%;
}

#search-result-view ul li {
  margin-top: 1%;
}

#search-result-view fieldset {
  border: 1px solid #333;
  border-radius: 5px;
}

#search-result-view legend {
  padding: 0.5em;
}
#search-result-view .ant-collapse {
  margin-top: 1%;
}
</style>

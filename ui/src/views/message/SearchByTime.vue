<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="search-time-form-advanced-search">
        <a-form
          class="ant-advanced-search-form"
          :form="form"
          @submit="handleSearch"
        >
          <a-row :gutter="24">
            <a-col :span="9">
              <a-form-item label="topic">
                <a-select
                  class="topic-select"
                  @change="handleTopicChange"
                  show-search
                  option-filter-prop="children"
                  v-decorator="[
                    'topic',
                    {
                      rules: [{ required: true, message: '请选择一个topic!' }],
                    },
                  ]"
                  placeholder="请选择一个topic"
                >
                  <a-select-option v-for="v in topicList" :key="v" :value="v">
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
                  option-filter-prop="children"
                  v-model="selectPartition"
                  placeholder="请选择一个分区"
                >
                  <a-select-option v-for="v in partitions" :key="v" :value="v">
                    <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="时间">
                <a-range-picker
                  v-decorator="['time', rangeConfig]"
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
              <a-form-item label="最大检索数">
                <a-input-number
                  v-decorator="[
                    'filterNumber',
                    {
                      initialValue: 5000,
                      rules: [
                        {
                          required: true,
                          message: '输入消息数!',
                        },
                      ],
                    },
                  ]"
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
              <a-form-item label="消息过滤">
                <a-select
                  class="filter-select"
                  option-filter-prop="children"
                  v-decorator="['filter', { initialValue: 'none' }]"
                  @change="onFilterChange"
                >
                  <a-select-option value="none"> 不启用过滤 </a-select-option>
                  <a-select-option value="body">
                    根据消息体过滤
                  </a-select-option>
                  <a-select-option value="header">
                    根据消息头过滤
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <div v-show="showBodyFilter">
              <a-col :span="8">
                <a-form-item label="消息内容">
                  <a-input
                    class="msg-body"
                    v-decorator="['value']"
                    placeholder="请输入消息内容"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="消息类型">
                  <a-select
                    v-decorator="[
                      'valueDeserializer',
                      { initialValue: 'String' },
                    ]"
                    class="body-type"
                  >
                    <a-select-option
                      v-for="v in deserializerList"
                      :key="v"
                      :value="v"
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
                <a-form-item label="Key">
                  <a-input
                    v-decorator="['headerKey']"
                    placeholder="消息头的key"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="11">
                <a-form-item label="Value">
                  <a-input
                    v-decorator="['headerValue']"
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

<script>
import request from "@/utils/request";
import { KafkaMessageApi, KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import MessageList from "@/views/message/MessageList";

export default {
  name: "SearchByTime",
  components: { MessageList },
  props: {
    topicList: {
      type: Array,
    },
  },
  data() {
    return {
      loading: false,
      form: this.$form.createForm(this, { name: "message_search_time" }),
      partitions: [],
      selectPartition: undefined,
      rangeConfig: {
        rules: [{ type: "array", required: true, message: "请选择时间!" }],
      },
      data: defaultData,
      deserializerList: [],
      showBodyFilter: false,
      showHeaderFilter: false,
    };
  },
  methods: {
    handleSearch(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          const data = Object.assign({}, values, {
            partition: this.selectPartition,
          });
          data.startTime = values.time[0].valueOf();
          data.endTime = values.time[1];
          this.loading = true;
          request({
            url: KafkaMessageApi.searchByTime.url,
            method: KafkaMessageApi.searchByTime.method,
            data: data,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.data = res.data;
            } else {
              notification.error({
                message: "error",
                description: res.msg,
              });
            }
          });
        }
      });
    },
    getPartitionInfo(topic) {
      this.loading = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + topic,
        method: KafkaTopicApi.getPartitionInfo.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.partitions = res.data.map((v) => v.partition);
          this.partitions.splice(0, 0, -1);
        }
      });
    },
    handleTopicChange(topic) {
      this.selectPartition = -1;
      this.getPartitionInfo(topic);
    },
    onFilterChange(e) {
      switch (e) {
        case "body":
          this.showBodyFilter = true;
          this.showHeaderFilter = false;
          break;
        case "header":
          this.showHeaderFilter = true;
          this.showBodyFilter = false;
          break;
        default:
          this.showBodyFilter = false;
          this.showHeaderFilter = false;
          break;
      }
    },
    getDeserializerList() {
      request({
        url: KafkaMessageApi.deserializerList.url,
        method: KafkaMessageApi.deserializerList.method,
      }).then((res) => {
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.deserializerList = res.data;
        }
      });
    },
  },
  created() {
    this.getDeserializerList();
  },
};
const defaultData = { realNum: 0, maxNum: 0, searchNum: 0 };
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

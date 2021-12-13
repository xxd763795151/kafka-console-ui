<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="components-form-advanced-search">
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
        </a-form>
      </div>
      <p style="margin-top: 1%">
        <strong
          >检索条数：{{ data.realNum }}，允许返回的最大条数：{{
            data.maxNum
          }}</strong
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
    };
  },
  methods: {
    handleSearch() {
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
  },
};
const defaultData = { realNum: 0, maxNum: 0 };
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

#components-form-advanced-search .search-result-list {
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
  width: 150px !important;
}
</style>

<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="search-time-form-advanced-search">
        <a-form
          class="ant-advanced-search-form"
          :form="form"
          @submit="handleSearch"
        >
          <a-row>
            <a-col :span="16">
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
            <a-col :span="8">
              <a-form-item label="分区">
                <a-select
                  class="type-select"
                  show-search
                  mode="multiple"
                  option-filter-prop="children"
                  v-model="selectPartition"
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
              <a-form-item label="时间">
                <a-range-picker
                  v-decorator="['time', rangeConfig]"
                  format="YYYY-MM-DD HH:mm:ss.SSS"
                  :show-time="{
                    hideDisabledOptions: true,
                    defaultValue: [
                      moment('00:00:00.000', 'HH:mm:ss.SSS'),
                      moment('23:59:59.999', 'HH:mm:ss.SSS'),
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
        <!--        <ul>-->
        <!--          <li v-for="(item, index) in data" :key="index">-->
        <!--            <fieldset>-->
        <!--              <legend>-->
        <!--                {{ item.topic }}, 时间: [{{ item.startTime }} ~-->
        <!--                {{ item.endTime }}], 总数: {{ item.total }}, 查询时间:-->
        <!--                {{ item.searchTime }}-->
        <!--              </legend>-->

        <!--            </fieldset>-->
        <!--          </li>-->
        <!--        </ul>-->
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

<script>
import request from "@/utils/request";
import { KafkaMessageApi, KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import locale from "ant-design-vue/lib/date-picker/locale/zh_CN";
import moment from "moment";

export default {
  name: "SendStatistics",
  props: {
    topicList: {
      type: Array,
    },
  },
  data() {
    return {
      moment,
      locale,
      loading: false,
      form: this.$form.createForm(this, { name: "message_send_statistics" }),
      partitions: [],
      selectPartition: [],
      rangeConfig: {
        rules: [{ type: "array", required: true, message: "请选择时间!" }],
      },
      data: [],
    };
  },
  methods: {
    handleSearch(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          const data = Object.assign({}, values);
          delete data.time;
          data.startTime = values.time[0];
          data.endTime = values.time[1];
          data.partition = Array.isArray(this.selectPartition)
            ? this.selectPartition
            : [this.selectPartition];
          this.loading = true;
          request({
            url: KafkaMessageApi.sendStatistics.url,
            method: KafkaMessageApi.sendStatistics.method,
            data: data,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.data.splice(0, 0, res.data);
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
    getCurrentTime() {
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
    },
  },
};
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
  border-radius: 5px; /* 设置圆角 */
}

#search-result-view legend {
  padding: 0.5em; /* 设置内边距 */
}
#search-result-view .ant-collapse {
  margin-top: 1%;
}
</style>

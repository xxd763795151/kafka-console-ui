<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-form
        :form="form"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 12 }"
        @submit="handleSubmit"
      >
        <a-form-item label="Topic">
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
        <a-form-item label="分区">
          <a-select
            class="type-select"
            show-search
            option-filter-prop="children"
            v-model="selectPartition"
            placeholder="请选择一个分区"
          >
            <a-select-option v-for="v in partitions" :key="v" :value="v">
              <span v-if="v == -1">默认</span> <span v-else>{{ v }}</span>
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="消息头">
          <table>
            <tbody>
              <tr v-for="(row, index) in rows" :key="index">
                <td class="w-30">
                  <a-input v-model="row.headerKey" placeholder="key" />
                </td>
                <td class="w-60">
                  <a-input v-model="row.headerValue" placeholder="value" />
                </td>
                <td>
                  <a-button
                    type="danger"
                    @click="deleteRow(index)"
                    v-show="rows.length > 1"
                    >删除</a-button
                  >
                </td>
                <td>
                  <a-button
                    type="primary"
                    @click="addRow"
                    v-show="index == rows.length - 1"
                    >添加</a-button
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </a-form-item>
        <a-form-item label="消息Key">
          <a-input v-decorator="['key', { initialValue: 'key' }]" />
        </a-form-item>
        <a-form-item label="消息体" has-feedback>
          <a-textarea
            :autosize="{ minRows: 5 }"
            v-decorator="[
              'body',
              {
                rules: [
                  {
                    required: true,
                    message: '输入消息体!',
                  },
                ],
              },
            ]"
            placeholder="输入消息体!"
          />
        </a-form-item>
        <a-form-item label="发送的消息数">
          <a-input-number
            v-decorator="[
              'num',
              {
                initialValue: 1,
                rules: [
                  {
                    required: true,
                    message: '输入消息数!',
                  },
                ],
              },
            ]"
            :min="1"
            :max="32"
          />
        </a-form-item>
        <a-form-item label="发送类型">
          <a-radio-group
            v-decorator="[
              'sync',
              {
                initialValue: 'false',
                rules: [{ required: true, message: '请选择一个发送类型!' }],
              },
            ]"
          >
            <a-radio value="false"> 异步发送 </a-radio>
            <a-radio value="true">
              同步发送（发送失败，会返回错误信息）
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
          <a-button type="primary" html-type="submit"> 提交 </a-button>
        </a-form-item>
      </a-form>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi, KafkaMessageApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
export default {
  name: "SendMessage",
  components: {},
  props: {},
  data() {
    return {
      form: this.$form.createForm(this, { name: "message_send" }),
      loading: false,
      partitions: [],
      selectPartition: undefined,
      rows: [{ headerKey: "", headerValue: "" }],
      topicList: [],
    };
  },
  methods: {
    getTopicNameList() {
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
      }).then((res) => {
        if (res.code == 0) {
          this.topicList = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
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
    addRow() {
      if (this.rows.length < 32) {
        this.rows.push({ HeaderKey: "", HeaderValue: "" });
      }
    },
    deleteRow(index) {
      this.rows.splice(index, 1);
    },
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          const param = Object.assign({}, values, {
            partition: this.selectPartition,
            headers: this.rows,
          });
          this.loading = true;
          request({
            url: KafkaMessageApi.send.url,
            method: KafkaMessageApi.send.method,
            data: param,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
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
  },
  created() {
    this.getTopicNameList();
  },
};
</script>
<style scoped>
.w-30 {
  width: 300px;
}
.w-60 {
  width: 500px;
}
</style>

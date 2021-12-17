<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-form :form="form">
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
        <a-form-item label="消息Key">
          <a-input v-decorator="['key', { initialValue: 'key' }]" />
        </a-form-item>
        <a-form-item label="消息体" has-feedback>
          <a-input
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
              'nums',
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
      </a-form>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
export default {
  name: "SendMessage",
  components: {},
  props: {
    topicList: {
      type: Array,
    },
  },
  data() {
    return {
      form: this.$form.createForm(this, { name: "message_send" }),
      loading: false,
      partitions: [],
      selectPartition: undefined,
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
  },
  created() {
    this.getTopicNameList();
  },
};
</script>

<style scoped></style>

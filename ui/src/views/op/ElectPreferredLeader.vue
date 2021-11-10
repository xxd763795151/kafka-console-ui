<template>
  <a-modal
    title="选择Preferred副本作为Leader"
    :visible="show"
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
          :form="form"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @submit="handleSubmit"
        >
          <a-form-item label="Topic">
            <a-select
              @change="handleTopicChange"
              show-search
              option-filter-prop="children"
              v-decorator="[
                'topic',
                { rules: [{ required: true, message: '请选择一个topic!' }] },
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
              show-search
              option-filter-prop="children"
              v-decorator="[
                'partition',
                { rules: [{ required: true, message: '请选择一个分区!' }] },
              ]"
              placeholder="请选择一个分区"
            >
              <a-select-option v-for="v in partitions" :key="v" :value="v">
                <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 确认 </a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi, KafkaOpApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
export default {
  name: "ElectPreferredLeader",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: this.visible,
      data: [],
      loading: false,
      form: this.$form.createForm(this, { name: "ElectPreferredLeaderForm" }),
      topicList: [],
      partitions: [],
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getTopicNameList();
      }
    },
  },
  methods: {
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          this.loading = true;
          request({
            url: KafkaOpApi.electPreferredLeader.url,
            method: KafkaOpApi.electPreferredLeader.method,
            data: values,
          }).then((res) => {
            this.loading = false;
            if (res.code != 0) {
              notification.error({
                message: "error",
                description: res.msg,
              });
            } else {
              this.$message.success(res.msg);
              this.$emit("closeElectPreferredLeaderDialog", { refresh: false });
            }
          });
        }
      });
    },
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
      this.getPartitionInfo(topic);
    },
    handleCancel() {
      this.data = [];
      this.$emit("closeElectPreferredLeaderDialog", { refresh: false });
    },
  },
};
</script>

<style scoped></style>

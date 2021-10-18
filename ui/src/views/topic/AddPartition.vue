<template>
  <a-modal
    title="增加分区"
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
          <a-form-item label="Topic名称">
            <a-input disabled="true"
              v-decorator="[
                'name',
                { rules: [{ required: true, message: '输入topic名称!' }] },
              ]"
              placeholder="topic"
            />
          </a-form-item>
          <a-form-item label="分区">
            <a-input-number
              :min="1"
              :max="128"
              v-decorator="[
                'numPartitions',
                {
                  initialValue: 1,
                  rules: [{ required: true, message: '输入分区数!' }],
                },
              ]"
            />
            <span class="ant-form-text"> 个分区 </span>
          </a-form-item>
          <a-form-item label="属性">
            <a-textarea
              rows="5"
              placeholder="格式示例如下：
max.message.bytes=1024
retention.bytes=1024
retention.ms=3600000"
              v-decorator="['configs']"
            />
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 提交 </a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
export default {
  name: "AddPartition",
  props: {
    topic: {
      type: String,
      default: "",
    },
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
      form: this.$form.createForm(this, { name: "coordinated" }),
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getPartitionInfo();
      }
    },
  },
  methods: {
    getPartitionInfo() {
      this.loading = false;
    },
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          if (values.configs) {
            const config = {};
            values.configs.split("\n").forEach((e) => {
              const c = e.split("=");
              if (c.length > 1) {
                let k = c[0].trim(),
                  v = c[1].trim();
                if (k && v) {
                  config[k] = v;
                }
              }
            });
            values.configs = config;
          }
          this.loading = true;
          request({
            url: KafkaTopicApi.creatTopic.url,
            method: KafkaTopicApi.creatTopic.method,
            data: values,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeAddPartitionDialog", { refresh: true });
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
    handleCancel() {
      this.data = [];
      this.$emit("closeAddPartitionDialog", { refresh: false });
    },
  },
};
</script>

<style scoped></style>

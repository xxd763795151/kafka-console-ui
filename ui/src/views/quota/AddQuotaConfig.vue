<template>
  <a-modal
      title="新增配置"
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
          <a-form-item label="用户">
            <a-input
                v-decorator="[
                'user',
              ]"
                placeholder="输入用户主体标识，比如：用户名!"
            />
          </a-form-item>
          <a-form-item label="客户端ID">
            <a-input
                v-decorator="[
                'client-id',
              ]"
                placeholder="输入用户客户端ID!"
            />
          </a-form-item>
          <a-form-item label="IP">
            <a-input
                v-decorator="[
                'ip',
              ]"
                placeholder="输入客户端IP!"
            />
          </a-form-item>
          <a-form-item label="生产速率">
            <a-input-number
                :min="1"
                :max="102400000"
                v-decorator="[
                'producerRate',
              ]"
            />
            <a-select default-value="MB" v-model="producerRateUnit" style="width: 100px">
              <a-select-option value="MB"> MB/s</a-select-option>
              <a-select-option value="KB"> KB/s</a-select-option>
              <a-select-option value="Byte"> Byte/s</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="消费速率">
            <a-input-number
                :min="1"
                :max="102400000"
                v-decorator="[
                'consumerRate',
              ]"
            />
            <a-select default-value="MB" v-model="consumerRateUnit" style="width: 100px">
              <a-select-option value="MB"> MB/s</a-select-option>
              <a-select-option value="KB"> KB/s</a-select-option>
              <a-select-option value="Byte"> Byte/s</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="吞吐量">
            <a-input-number
                :min="1"
                :max="102400000"
                v-decorator="[
                'requestPercentage',
              ]"
            />
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 提交</a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import {KafkaTopicApi} from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "AddQuotaConfig",
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
      form: this.$form.createForm(this, {name: "coordinated"}),
      producerRateUnit: "MB",
      consumerRateUnit: "MB",
    };
  },
  watch: {
    visible(v) {
      this.show = v;
    },
  },
  methods: {
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
              this.$emit("closeAddQuotaDialog", {refresh: true});
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
      this.$emit("closeAddQuotaDialog", {refresh: false});
    },
  },
};
</script>

<style scoped></style>

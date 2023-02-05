<script src="../../store/index.js"></script>
<template>
  <a-modal
      title="修改配置"
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
          <a-form-item label="用户" v-show="showUser">
            <a-input
                :disabled="true"
                v-decorator="[
                'user', { initialValue: record.user }
              ]"
                placeholder="输入用户主体标识，比如：用户名，未指定表示用户默认设置"
            />
          </a-form-item>
          <a-form-item label="客户端ID" v-show="showClientId">
            <a-input
                :disabled="true"
                v-decorator="[
                'client', { initialValue: record.client }
              ]"
                placeholder="输入用户客户端ID，未指定表示默认客户端设置"
            />
          </a-form-item>
          <a-form-item label="IP" v-show="showIP">
            <a-input
                :disabled="true"
                v-decorator="[
                'ip', { initialValue: record.ip }
              ]"
                placeholder="输入客户端IP"
            />
          </a-form-item>
          <a-form-item label="生产速率">
            <a-input-number
                :min="1"
                :max="102400000"
                v-decorator="[
                'producerRate', { initialValue: record.producerRate }
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
                'consumerRate', { initialValue: record.consumerRate }
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
                'requestPercentage', { initialValue: record.requestPercentage }
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
import {KafkaClientQuotaApi} from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "UpdateQuotaConfig",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    type: {
      type: String,
      default: "",
    },
    record: {
      type: Object,
      default: function () {
        return {}
      },
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
      showUser: false,
      showIP: false,
      showClientId: false,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.init();
      }
    },
  },
  methods: {
    handleSubmit() {
      this.form.validateFields((err, values) => {
        if (!err) {
          const params = {type: this.type, deleteConfigs: []};
          const unitMap = {MB: 1024 * 1024, KB: 1024, Byte: 1};
          if (values.consumerRate) {
            const num = typeof (values.consumerRate) == "string" && values.consumerRate.indexOf(" ") > 0 ? values.consumerRate.split(" ")[0] : values.consumerRate;
            params.consumerRate = num * unitMap[this.consumerRateUnit];
          } else {
            params.deleteConfigs.push("consumerRate");
          }
          if (values.producerRate) {
            const num = typeof (values.producerRate) == "string" && values.producerRate.indexOf(" ") > 0 ? values.producerRate.split(" ")[0] : values.producerRate;
            params.producerRate = num * unitMap[this.producerRateUnit];
          } else {
            params.deleteConfigs.push("producerRate");
          }
          if (values.requestPercentage) {
            params.requestPercentage = values.requestPercentage;
          } else {
            params.deleteConfigs.push("requestPercentage");
          }
          params.types = [];
          if (this.showUser) {
            params.types.push("user");
            if (values.user) {
              params.names = [values.user.trim()];
            } else {
              params.names = [""];
            }
          }
          if (this.showClientId) {
            params.types.push("client-id");
            if (values.client) {
              params.names = [values.client.trim()];
            } else {
              params.names = [""];
            }
          }
          if (this.showIP) {
            params.types.push("ip");
            if (values.ip) {
              params.names = [values.ip.trim()];
            } else {
              params.names = [""];
            }
          }
          this.loading = true;
          request({
            url: KafkaClientQuotaApi.alterClientQuotaConfigs.url,
            method: KafkaClientQuotaApi.alterClientQuotaConfigs.method,
            data: params,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeUpdateQuotaDialog", {refresh: true});
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
      this.$emit("closeUpdateQuotaDialog", {refresh: false});
    },
    init() {
      this.producerRateUnit = "MB";
      if (this.record.producerRate) {
        this.producerRateUnit = this.record.producerRate.split(" ")[1];
      }
      this.consumerRateUnit = "MB";
      if (this.record.consumerRate) {
        this.consumerRateUnit = this.record.consumerRate.split(" ")[1];
      }
      if (this.type == "user") {
        this.showUser = true;
      } else if (this.type == "client-id") {
        this.showClientId = true;
      } else if (this.type == "ip") {
        this.showIP = true;
      } else if (this.type == "user&client-id") {
        this.showUser = true;
        this.showClientId = true;
      }
    },
  },
  created() {

  },
};
</script>

<style scoped></style>

<script src="../../store/index.js"></script>
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
          <a-form-item label="用户" v-show="showUser">
            <a-input
                v-decorator="[
                'user',
              ]"
                placeholder="输入用户主体标识，比如：用户名，未指定表示用户默认设置"
            />
          </a-form-item>
          <a-form-item label="客户端ID" v-show="showClientId">
            <a-input
                v-decorator="[
                'client',
              ]"
                placeholder="输入用户客户端ID，未指定表示默认客户端设置"
            />
          </a-form-item>
          <a-form-item label="IP" v-show="showIP">
            <a-input
                v-decorator="[
                'ip',
              ]"
                placeholder="输入客户端IP"
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
import {KafkaClientQuotaApi} from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "AddQuotaConfig",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    type: {
      type: String,
      default: "",
    },
    showClientId: {
      type: Boolean,
      default: false,
    },
    showUser: {
      type: Boolean,
      default: false,
    },
    showIP: {
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
    handleSubmit() {
      this.form.validateFields((err, values) => {
        if (!err) {
          const params = Object.assign({type: this.type}, values);
          const unitMap = {MB: 1024 * 1024, KB: 1024, Byte: 1};
          if (values.consumerRate) {
            params.consumerRate = params.consumerRate * unitMap[this.consumerRateUnit];
          }
          if (values.producerRate) {
            params.producerRate = params.producerRate * unitMap[this.producerRateUnit];
          }
          params.types = [];
          params.names = [];
          if (this.showUser) {
            params.types.push("user");
            if (params.user) {
              params.names.push(params.user.trim());
            } else {
              params.names.push("");
            }
          }
          if (this.showClientId) {
            params.types.push("client-id");
            if (params.client) {
              params.names.push(params.client.trim());
            } else {
              params.names.push("");
            }
          }
          if (this.showIP) {
            params.types.push("ip");
            if (params.ip) {
              params.names.push(params.ip.trim());
            } else {
              params.names.push("");
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
      this.producerRateUnit = "MB";
      this.consumerRateUnit = "MB";
    },
    create() {
      this.producerRateUnit = "MB";
      this.consumerRateUnit = "MB";
    },
  },
};
</script>

<style scoped></style>

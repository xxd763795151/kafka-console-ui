<template>
  <a-modal
    title="编辑配置"
    :visible="show"
    :width="1000"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
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
          <a-form-item label="属性">
            <a-input
              :disabled="true"
              v-decorator="['name', { initialValue: record.name }]"
              placeholder="name"
            />
          </a-form-item>
          <a-form-item label="值">
            <a-input
              v-decorator="[
                'value',
                {
                  initialValue: record.value,
                  rules: [{ required: true, message: '输入属性值!' }],
                },
              ]"
              placeholder="value"
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
import { KafkaConfigApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
export default {
  name: "EditConfig",
  props: {
    topic: {
      type: String,
      default: "",
    },
    visible: {
      type: Boolean,
      default: false,
    },
    record: {
      default: {},
    },
    brokerId: {
      type: String,
      default: "",
    },
    isLoggerConfig: {
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
    },
  },
  methods: {
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          this.loading = true;
          const api = this.isLoggerConfig
            ? KafkaConfigApi.setBrokerLoggerConfig
            : KafkaConfigApi.setBrokerConfig;
          request({
            url: api.url,
            method: api.method,
            data: Object.assign({ entity: this.brokerId }, values),
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeEditConfigDialog", { refresh: true });
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
      this.$emit("closeEditConfigDialog", { refresh: false });
    },
  },
};
</script>

<style scoped></style>

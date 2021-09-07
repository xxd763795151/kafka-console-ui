<template>
  <a-modal
    title="增加权限"
    :visible="show"
    :confirm-loading="confirmLoading"
    :width="800"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="提交"
    cancelText="取消"
    :mask="false"
    :destroyOnClose="true"
  >
    <a-form :form="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
      <a-form-item label="用户名">
        <a-input
          v-decorator="['username', { initialValue: record.username }]"
          disabled="disabled"
        />
      </a-form-item>
      <a-form-item label="资源类型">
        <a-radio-group
          v-decorator="['resourceType', { initialValue: 'TOPIC' }]"
        >
          <a-radio value="TOPIC"> topic</a-radio>
          <a-radio value="GROUP"> 消费组</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="资源名称">
        <a-input
          v-decorator="[
            'resourceName',
            { rules: [{ required: true, message: '请输入!' }] },
          ]"
          placeholder="请输入topic或消费组名称"
        />
      </a-form-item>
      <a-form-item label="主机">
        <a-input
          v-decorator="[
            'host',
            {
              rules: [{ required: true, message: '请输入!' }],
              initialValue: '*',
            },
          ]"
          placeholder="请输入主机地址，比如：*，全部匹配"
        />
      </a-form-item>
      <a-form-item label="操作类型" has-feedback>
        <a-select
          v-decorator="[
            'operation',
            { rules: [{ required: true, message: '请选择!' }] },
          ]"
          placeholder="请选择!"
        >
          <a-select-option v-for="i in operations" :key="i">
            {{ i }}</a-select-option
          >
        </a-select>
      </a-form-item>
      <a-form-item label="权限类型">
        <a-radio-group
          v-decorator="['permissionType', { initialValue: 'ALLOW' }]"
        >
          <a-radio value="ALLOW"> 允许</a-radio>
          <a-radio value="DENY"> 拒绝</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
import { KafkaAclApi } from "@/utils/api";
import request from "@/utils/request";

export default {
  name: "AddAuth",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    record: {
      default: {},
    },
  },
  data() {
    return {
      formLayout: "horizontal",
      form: this.$form.createForm(this, { name: "AddProducerAuthForm" }),
      confirmLoading: false,
      show: this.visible,
      operations: operationList,
    };
  },
  watch: {
    visible(v) {
      if (this.show != v) {
        this.show = v;
        if (this.show) {
          this.getOperationList();
        }
      }
    },
  },
  methods: {
    handleOk() {
      const form = this.form;
      form.validateFields((e, v) => {
        if (e) {
          return;
        }
        const param = Object.assign({}, v);
        const api = KafkaAclApi.addAclAuth;
        this.confirmLoading = true;
        request({
          url: api.url,
          method: api.method,
          data: param,
        }).then((res) => {
          this.confirmLoading = false;
          if (res.code == 0) {
            this.$message.success(res.msg);
            this.$emit("addAuthDialog", { refresh: true });
          } else {
            this.$message.error(res.msg);
          }
        });
      });
    },
    handleCancel() {
      this.$emit("addAuthDialog", { refresh: false });
    },
    getOperationList() {
      request({
        url: KafkaAclApi.getOperationList.url,
        method: KafkaAclApi.getOperationList.method,
      }).then((res) => {
        if (res.code != 0) {
          this.$message.error(res.msg);
        } else {
          operationList.splice(0, operationList.length);
          operationList.push(...res.data);
        }
      });
    },
  },
};
const operationList = [];
</script>

<style scoped></style>

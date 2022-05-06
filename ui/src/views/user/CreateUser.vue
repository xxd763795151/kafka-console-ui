<template>
  <a-modal
    title="新增用户"
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
          <a-form-item label="账号">
            <a-input
              v-decorator="[
                'username',
                { rules: [{ required: true, message: '请输入用户名!' }] },
              ]"
              placeholder="请输入用户名"
            />
          </a-form-item>
          <a-form-item label="密码">
            <a-input
                v-decorator="[
                'password',
                { rules: [{ required: true, message: '请输入密码!' }] },
              ]"
                placeholder="请输入密码"
            />
          </a-form-item>
          <a-form-item label="角色">
            <a-select
                option-filter-prop="role"
                v-decorator="[
                'role',
                { rules: [{ required: true, message: '请选择一个角色!' }] },
              ]"
                placeholder="请选择一个角色"
            >
              <a-select-option v-for="v in roleList" :key="v" :value="v">
                {{ v }}
              </a-select-option>
            </a-select>
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
import notification from "ant-design-vue/es/notification";
import {DevOpsUserAPi} from "../../utils/api";
export default {
  name: "CreateUser",
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
      roleList: roleList,
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
            url: DevOpsUserAPi.createUser.url,
            method: DevOpsUserAPi.createUser.method,
            data: values,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeCreateUserDialog", { refresh: true });
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
      this.$emit("closeCreateUserDialog", { refresh: false });
    },
  },
};
const roleList = ["developer", "manager"]
</script>

<style scoped></style>

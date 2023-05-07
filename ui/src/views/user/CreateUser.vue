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
          <a-form-item label="用户名">
            <a-input
              v-decorator="[
                'username',
                { rules: [{ required: true, message: '输入用户名' }] },
              ]"
              placeholder="输入用户名"
            />
          </a-form-item>
          <a-form-item label="角色">
            <a-select
              show-search
              option-filter-prop="children"
              v-decorator="[
                'roleIds',
                { rules: [{ required: true, message: '请选择一个角色!' }] },
              ]"
              placeholder="请选择一个角色"
            >
              <a-select-option
                v-for="role in roles"
                :key="role.id"
                :value="role.id"
              >
                {{ role.roleName }}
              </a-select-option>
            </a-select>
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
import notification from "ant-design-vue/es/notification";
import { UserManageApi } from "@/utils/api";

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
      loading: false,
      form: this.$form.createForm(this, { name: "coordinated" }),
      roles: [],
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
          request({
            url: UserManageApi.addOrUpdateUser.url,
            method: UserManageApi.addOrUpdateUser.method,
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
    getRoles() {
      this.loading = true;
      request({
        url: UserManageApi.getRole.url,
        method: UserManageApi.getRole.method,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.roles = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    handleCancel() {
      this.data = [];
      this.$emit("closeCreateUserDialog", { refresh: false });
    },
  },
  created() {
    this.getRoles();
  },
};
</script>

<style scoped></style>

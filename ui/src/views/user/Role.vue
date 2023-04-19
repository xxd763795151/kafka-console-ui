<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-card :bordered="false" :style="{ height: '100%' }">
        <a-row :gutter="24">
          <a-col :md="4">
            <a-list itemLayout="horizontal" :data-source="roles">
              <a-list-item
                slot="renderItem"
                slot-scope="item, index"
                :key="index"
              >
                <a-list-item-meta
                  :style="{ marginBottom: '0', display: 'flex' }"
                >
                  <span slot="description" class="role-desc">{{
                    item.description
                  }}</span>
                  <a slot="title" class="role-name" @click="selected(item)">{{
                    item.roleName
                  }}</a>
                </a-list-item-meta>
                <a-popconfirm title="确定删除角色？">
                  <a :style="{ display: 'flex' }">
                    <a-icon type="delete" />
                  </a>
                </a-popconfirm>
              </a-list-item>
            </a-list>
            <span
              :style="{ margin: '25px', fontSize: '15px', display: 'block' }"
            >
              <a><a-icon type="plus" /> 新增角色</a>
            </span>
          </a-col>
          <a-col :md="20">
            <div class="role-info" v-if="!selectedRole.roleName">
              <a-empty />
            </div>
            <div class="role-info" v-if="selectedRole.roleName">
              <a-form :form="form">
                <a-form-item label="角色名称">
                  <a-input
                    v-decorator="[
                      'roleName',
                      {
                        rules: [{ required: true, message: '请填写角色名称!' }],
                        initialValue: selectedRole.roleName,
                      },
                    ]"
                    placeholder="请填写角色名称"
                  />
                </a-form-item>

                <a-form-item label="备注说明">
                  <a-textarea
                    :row="3"
                    v-decorator="[
                      'description',
                      {
                        rules: [{ required: true, message: '请填写备注说明!' }],
                        initialValue: selectedRole.description,
                      },
                    ]"
                    placeholder="请填写备注说明"
                  />
                </a-form-item>

                <a-form-item label="拥有权限"> </a-form-item>
                <a-form-item>
                  <a-button type="primary" :loading="loading">保存</a-button>
                </a-form-item>
              </a-form>
            </div>
          </a-col>
        </a-row>
      </a-card>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";

import { UserManageApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

export default {
  name: "Role",
  components: {},
  data() {
    return {
      form: this.$form.createForm(this),
      loading: false,
      roles: [],
      permissions: [],
      selectedRole: {},
      selectedPermissions: [],
    };
  },
  methods: {
    selected(role) {
      this.selectedRole = Object.assign({}, role);
      
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
    getPermissions() {
      this.loading = true;
      request({
        url: UserManageApi.getPermissions.url,
        method: UserManageApi.getPermissions.method,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.permissions = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
  },
  created() {
    this.getRoles();
    this.getPermissions();
  },
};
</script>

<style scoped>
.editable-row-operations a {
  margin-right: 8px;
}

.role-desc {
  text-align: left;
  display: block;
}

.role-name {
  text-align: left;
  display: block;
  font-size: 16px;
}

.role-info {
  max-width: 1000px;
  background-color: #faf9f9;
}

a-list-item-meta {
}
</style>

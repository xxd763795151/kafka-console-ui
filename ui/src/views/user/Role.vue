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
                <a-popconfirm
                  title="确定删除角色？"
                  ok-text="确认"
                  cancel-text="取消"
                  @confirm="deleteRole(item)"
                >
                  <a :style="{ display: 'flex' }">
                    <a-icon type="delete" />
                  </a>
                </a-popconfirm>
              </a-list-item>
            </a-list>
            <span
              :style="{ margin: '25px', fontSize: '15px', display: 'block' }"
            >
              <a @click="addRole()"><a-icon type="plus" /> 新增角色</a>
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

                <a-form-item label="权限配置">
                  <div
                    v-for="(menuPermission, index) in selectedPermissions"
                    :key="index"
                  >
                    <a-row>
                      <a-col :span="18" :style="{ fontWeight: 'bold' }"
                        >{{ menuPermission.name }}
                      </a-col>
                      <a-col :span="6" :style="{ textAlign: 'right' }">
                        <!--                        <a-checkbox :checked="menuPermission.checked">-->
                        <!--                          可见</a-checkbox-->
                        <!--                        >-->
                        <!--                        <a-switch v-model="menuPermission.checked" />-->
                      </a-col>
                    </a-row>
                    <a-divider type="horizontal" :style="{ margin: '0px' }" />
                    <a-row
                      :gutter="16"
                      v-for="(
                        checkboxPermission, index2
                      ) in menuPermission.children"
                      :key="index2"
                    >
                      <a-col :xl="3" :lg="24">
                        {{ checkboxPermission.name }}：
                      </a-col>
                      <a-col :xl="18" :lg="24">
                        <a-checkbox-group
                          :options="checkboxPermission.children"
                          v-model="checkboxPermission.selected"
                        />
                      </a-col>
                      <a-col :span="3" :style="{ textAlign: 'right' }">
                        <a-checkbox
                          v-model="checkboxPermission.selectAll"
                          @click="onCheckboxSelectAll(checkboxPermission)"
                        >
                          全选</a-checkbox
                        >
                      </a-col>
                    </a-row>
                  </div>
                </a-form-item>
                <a-form-item>
                  <a-button type="primary" :loading="loading" @click="onSave()"
                    >保存</a-button
                  >
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
      this.form.getFieldDecorator("description", {
        rules: [{ required: true, message: "请填写备注说明!" }],
        initialValue: this.selectedRole.description,
      });
      this.form.getFieldDecorator("roleName", {
        rules: [{ required: true, message: "请填写角色名称!" }],
        initialValue: this.selectedRole.roleName,
      });
      this.form.setFieldsValue({ roleName: this.selectedRole.roleName });
      this.form.setFieldsValue({ description: this.selectedRole.description });
      const idSet = this.selectedRole.permissionIds
        ? new Set(this.selectedRole.permissionIds)
        : new Set();
      this.selectedPermissions = [];
      let recursive = function (e, res) {
        if (e.children) {
          const children = e.children;
          children.forEach((c) => {
            const child = Object.assign({}, c);
            child.name = e.name + "-" + c.name;
            child.label = child.name;
            child.value = child.id;
            res.push(child);
            if (child.children) {
              recursive(child, res);
            }
            delete child.children;
          });
        }
      };
      // 1 级都是菜单，其它的都分到2级按钮
      this.permissions.forEach((e) => {
        const menu = Object.assign({}, e);
        if (menu.children) {
          const arr = [];
          menu.children.forEach((c) => {
            // 菜单下的按扭，按扭下面还有按扭的话，都合并
            const btn = Object.assign({}, c);
            arr.push(btn);
            if (btn.children) {
              const self = Object.assign({}, btn);
              self.name = btn.name;
              self.label = btn.name;
              self.value = btn.id;
              delete self.children;
              const btnArr = [self];
              recursive(btn, btnArr);
              btn.children = btnArr;
              const selected = btn.children
                .map((bc) => bc.id)
                .filter((id) => idSet.has(id));
              btn.selected = selected || [];
              btn.selectAll = btn.selected.length == btn.children.length;
            }
          });
          menu.children = arr;
          // menu.checked = idSet.has(menu.id);
        }
        this.selectedPermissions.push(menu);
      });
    },
    deleteRole(role) {
      if (role.adding) {
        this.roles.pop();
        return;
      }
      this.loading = true;
      request({
        url: UserManageApi.deleteRole.url + "?id=" + role.id,
        method: UserManageApi.deleteRole.method,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.$message.success(res.msg);
          this.getRoles();
          if (role.id == this.selectedRole.id) {
            this.selectedRole = {};
          }
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    addRole() {
      const role = {
        roleName: "角色名称",
        description: "角色描述",
        adding: true,
      };
      this.roles.push(role);
      this.selected(role);
    },
    onSave() {
      this.form.validateFields((err, values) => {
        if (!err) {
          const params = Object.assign({}, this.selectedRole, values);
          params.permissionIds = [];
          this.selectedPermissions.forEach((e) => {
            if (e.children) {
              e.children.forEach((child) => {
                if (child.selected) {
                  params.permissionIds.push(...child.selected);
                }
              });
            }
          });
          this.loading = true;
          request({
            url: UserManageApi.addOrUpdateRole.url,
            method: UserManageApi.addOrUpdateRole.method,
            data: params,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.getRoles();
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
    onCheckboxSelectAll(record) {
      if (!record.children) {
        record.selected = [];
        return;
      }
      if (!record.selectAll) {
        record.selected = record.children.map((bc) => bc.id);
      } else {
        record.selected = [];
      }
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

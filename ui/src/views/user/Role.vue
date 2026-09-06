<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-card :bordered="false" :style="{ height: '100%' }">
        <a-row :gutter="24">
          <a-col :md="4">
            <a-list item-layout="horizontal" :data-source="roles">
              <template #renderItem="{ item, index }">
                <a-list-item :key="index">
                  <a-list-item-meta
                    :style="{ marginBottom: '0', display: 'flex' }"
                  >
                    <template #description>
                      <span class="role-desc">{{ item.description }}</span>
                    </template>
                    <template #title>
                      <a class="role-name" @click="selected(item)">{{ item.roleName }}</a>
                    </template>
                  </a-list-item-meta>
                  <a-popconfirm
                    title="确定删除角色？"
                    ok-text="确认"
                    cancel-text="取消"
                    @confirm="deleteRole(item)"
                  >
                    <a :style="{ display: 'flex' }" v-action:user-manage:role:del>
                      <DeleteOutlined />
                    </a>
                  </a-popconfirm>
                </a-list-item>
              </template>
            </a-list>
            <span
              :style="{ margin: '25px', fontSize: '15px', display: 'block' }"
            >
              <a @click="addRole()" v-action:user-manage:role:save>
                <PlusOutlined /> 新增角色
              </a>
            </span>
          </a-col>
          <a-col :md="20">
            <div class="role-info" v-if="!selectedRole.roleName">
              <a-empty />
            </div>
            <div class="role-info" v-if="selectedRole.roleName">
              <a-form :model="formState" :rules="rules" ref="formRef">
                <h2>角色信息配置</h2>
                <a-form-item label="角色名称" name="roleName">
                  <a-input
                    v-model:value="formState.roleName"
                    placeholder="请填写角色名称"
                  />
                </a-form-item>

                <a-form-item label="备注说明" name="description">
                  <a-textarea
                    :rows="3"
                    v-model:value="formState.description"
                    placeholder="请填写备注说明"
                  />
                </a-form-item>

                <a-form-item>
                  <h2>功能权限配置</h2>
                  <div
                    v-for="(menuPermission, index) in selectedPermissions"
                    :key="index"
                  >
                    <a-row>
                      <a-col :span="18" :style="{ fontWeight: 'bold' }">
                        {{ menuPermission.name }}
                      </a-col>
                      <a-col :span="6" :style="{ textAlign: 'right' }">
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
                          v-model:value="checkboxPermission.selected"
                        />
                      </a-col>
                      <a-col :span="3" :style="{ textAlign: 'right' }">
                        <a-checkbox
                          v-model:checked="checkboxPermission.selectAll"
                          @click="onCheckboxSelectAll(checkboxPermission)"
                        >
                          全选
                        </a-checkbox>
                      </a-col>
                    </a-row>
                  </div>
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    :loading="loading"
                    @click="onSave()"
                    v-action:user-manage:role:save
                  >
                    保存
                  </a-button>
                </a-form-item>
              </a-form>
            </div>
          </a-col>
        </a-row>
      </a-card>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons-vue';
import request from '@/utils/request';
import { UserManageApi } from '@/utils/api';
import notification from 'ant-design-vue/lib/notification';

export default defineComponent({
  name: 'Role',
  components: { DeleteOutlined, PlusOutlined },
  setup() {
    const formRef = ref();

    const state = reactive({
      loading: false,
      roles: [] as any[],
      permissions: [] as any[],
      selectedRole: {} as any,
      selectedPermissions: [] as any[],
      formState: {
        roleName: '',
        description: '',
      },
      rules: {
        roleName: [{ required: true, message: '请填写角色名称!' }],
        description: [{ required: true, message: '请填写备注说明!' }],
      },
    });

    const selected = (role: any) => {
      state.selectedRole = Object.assign({}, role);
      state.formState.roleName = state.selectedRole.roleName || '';
      state.formState.description = state.selectedRole.description || '';
      const idSet = state.selectedRole.permissionIds
        ? new Set(state.selectedRole.permissionIds)
        : new Set();
      state.selectedPermissions = [];
      const recursive = (e: any, res: any[]) => {
        if (e.children) {
          const children = e.children;
          children.forEach((c: any) => {
            const child = Object.assign({}, c);
            child.name = e.name + '-' + c.name;
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
      state.permissions.forEach((e: any) => {
        const menu = Object.assign({}, e);
        if (menu.children) {
          const arr: any[] = [];
          menu.children.forEach((c: any) => {
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
                .map((bc: any) => bc.id)
                .filter((id: any) => idSet.has(id));
              btn.selected = selected || [];
              btn.selectAll = btn.selected.length == btn.children.length;
            } else {
              const self = Object.assign({}, btn);
              self.name = btn.name;
              self.label = btn.name;
              self.value = btn.id;
              const btnArr = [self];
              btn.children = btnArr;
              const selected = btn.children
                .map((bc: any) => bc.id)
                .filter((id: any) => idSet.has(id));
              btn.selected = selected || [];
              btn.selectAll = btn.selected.length == btn.children.length;
            }
          });
          menu.children = arr;
        }
        state.selectedPermissions.push(menu);
      });
    };

    const deleteRole = (role: any) => {
      if (role.adding) {
        state.roles.pop();
        return;
      }
      state.loading = true;
      request({
        url: UserManageApi.deleteRole.url + '?id=' + role.id,
        method: UserManageApi.deleteRole.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          message.success(res.msg);
          getRoles();
          if (role.id == state.selectedRole.id) {
            state.selectedRole = {};
          }
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    const addRole = () => {
      const role = {
        roleName: '角色名称',
        description: '角色描述',
        adding: true,
      };
      state.roles.push(role);
      selected(role);
    };

    const onSave = async () => {
      try {
        await formRef.value?.validate();
      } catch {
        return;
      }
      const params = Object.assign({}, state.selectedRole, state.formState);
      params.permissionIds = [];
      state.selectedPermissions.forEach((e: any) => {
        if (e.children) {
          e.children.forEach((child: any) => {
            if (child.selected) {
              params.permissionIds.push(...child.selected);
            }
          });
        }
      });
      state.loading = true;
      request({
        url: UserManageApi.addOrUpdateRole.url,
        method: UserManageApi.addOrUpdateRole.method,
        data: params,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          message.success(res.msg);
          getRoles();
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    const onCheckboxSelectAll = (record: any) => {
      if (!record.children) {
        record.selected = [];
        return;
      }
      if (!record.selectAll) {
        record.selected = record.children.map((bc: any) => bc.id);
      } else {
        record.selected = [];
      }
    };

    const getRoles = () => {
      state.loading = true;
      request({
        url: UserManageApi.getRole.url,
        method: UserManageApi.getRole.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          state.roles = res.data;
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    const getPermissions = () => {
      state.loading = true;
      request({
        url: UserManageApi.getPermissions.url,
        method: UserManageApi.getPermissions.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          state.permissions = res.data;
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    onMounted(() => {
      getRoles();
      getPermissions();
    });

    return {
      ...toRefs(state),
      formRef,
      selected,
      deleteRole,
      addRole,
      onSave,
      onCheckboxSelectAll,
      getRoles,
      getPermissions,
    };
  },
});
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

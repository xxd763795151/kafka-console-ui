<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="search-offset-form-advanced-search">
        <a-form
          class="ant-advanced-search-form"
          :model="formState"
          @submit="handleSearch"
        >
          <a-row :gutter="24">
            <a-col :span="16">
              <a-form-item label="用户名" name="username">
              <a-input
                v-model:value="formState.username"
                placeholder="请输入用户名!"
                @change="onUsernameChange"
              />
            </a-form-item>
            </a-col>
            <a-col :span="2" :style="{ textAlign: 'right' }">
              <a-form-item>
                <a-button
                  type="primary"
                  html-type="submit"
                  @click="handleSearch()"
                >
                  刷新</a-button
                >
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="operation-row-button">
        <a-button
          type="primary"
          @click="openCreateUserDialog()"
          v-action:user-manage:user:add
          >新增用户</a-button
        >
      </div>
      <a-table
        :columns="columns"
        :data-source="filteredData"
        bordered
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operation'">
            <div v-show="record.username != 'super-admin'">
              <a-popconfirm
                :title="'删除用户: ' + record.username + '？'"
                ok-text="确认"
                cancel-text="取消"
                @confirm="deleteUser(record)"
              >
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  type="primary"
                  danger
                  v-action:user-manage:user:del
                  >删除
                </a-button>
              </a-popconfirm>
              <a-popconfirm
                :title="'重置用户: ' + record.username + '密码？'"
                ok-text="确认"
                cancel-text="取消"
                @confirm="resetPassword(record)"
              >
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  v-action:user-manage:user:reset-pass
                  >重置密码
                </a-button>
              </a-popconfirm>
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="openUpdateUserRoleDialog(record)"
                v-action:user-manage:user:change-role
                >分配角色
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
      <CreateUser
        @closeCreateUserDialog="closeCreateUserDialog"
        :open="showCreateUserDialog"
      ></CreateUser>
      <MessageBox
        :open="showMessageBox"
        :message="messageBoxContent"
        @closeMessageBox="closeMessageBox"
      ></MessageBox>
      <UpdateUserRole
        :open="showUpdateUserRole"
        :user="selectUser"
        @closeUpdateUserRoleDialog="closeUpdateUserRoleDialog"
      ></UpdateUserRole>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import request from '@/utils/request';
import notification from 'ant-design-vue/lib/notification';
import { UserManageApi } from '@/utils/api';
import CreateUser from '@/views/user/CreateUser.vue';
import MessageBox from '@/components/MessageBox.vue';
import UpdateUserRole from '@/views/user/UpdateUserRole.vue';

export default defineComponent({
  name: 'User',
  components: { CreateUser, MessageBox, UpdateUserRole },
  props: {
    topicList: {
      type: Array,
    },
  },
  setup() {
    const state = reactive({
      loading: false,
      formState: {
        username: '',
      },
      data: [] as any[],
      filteredData: [] as any[],
      filterUsername: '',
      showCreateUserDialog: false,
      showMessageBox: false,
      showUpdateUserRole: false,
      messageBoxContent: '',
      selectUser: {} as any,
      columns: [
        {
          title: '用户名',
          dataIndex: 'username',
          key: 'username',
        },
        {
          title: '角色',
          dataIndex: 'roleNames',
          key: 'roleNames',
        },
        {
          title: '操作',
          key: 'operation',
        },
      ],
    });

    const handleSearch = () => {
      state.loading = true;
      request({
        url: UserManageApi.getUsers.url,
        method: UserManageApi.getUsers.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          state.data = res.data;
          filter();
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    const refresh = () => {
      handleSearch();
    };

    const filter = () => {
      state.filteredData = state.data.filter(
        (e: any) => e && e.username && e.username.indexOf(state.filterUsername) != -1
      );
    };

    const onUsernameChange = (input: any) => {
      state.filterUsername = input.target.value;
      filter();
    };

    const openCreateUserDialog = () => {
      state.showCreateUserDialog = true;
    };

    const closeCreateUserDialog = (p: any) => {
      state.showCreateUserDialog = false;
      if (p.refresh) {
        refresh();
        state.messageBoxContent = '用户初始密码：' + p.data;
        state.showMessageBox = true;
      }
    };

    const openUpdateUserRoleDialog = (user: any) => {
      state.selectUser = user;
      state.showUpdateUserRole = true;
    };

    const closeUpdateUserRoleDialog = (p: any) => {
      state.showUpdateUserRole = false;
      if (p.refresh) {
        refresh();
      }
    };

    const closeMessageBox = () => {
      state.showMessageBox = false;
    };

    const deleteUser = (user: any) => {
      state.loading = true;
      request({
        url: UserManageApi.deleteUser.url + '?id=' + user.id,
        method: UserManageApi.deleteUser.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          refresh();
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    const resetPassword = (record: any) => {
      state.loading = true;
      const params = Object.assign({}, record);
      params.resetPassword = true;
      request({
        url: UserManageApi.addOrUpdateUser.url,
        method: UserManageApi.addOrUpdateUser.method,
        data: params,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          state.messageBoxContent = '密码重置成功，新密码：' + res.data;
          state.showMessageBox = true;
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    onMounted(() => {
      handleSearch();
    });

    return {
      ...toRefs(state),
      handleSearch,
      refresh,
      filter,
      onUsernameChange,
      openCreateUserDialog,
      closeCreateUserDialog,
      openUpdateUserRoleDialog,
      closeUpdateUserRoleDialog,
      closeMessageBox,
      deleteUser,
      resetPassword,
    };
  },
});
</script>

<style scoped>
.tab-content {
  width: 100%;
  height: 100%;
}

.ant-advanced-search-form {
  padding: 24px;
  background: #fbfbfb;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.ant-advanced-search-form .ant-form-item {
  display: flex;
}

.ant-advanced-search-form input {
  width: 400px;
}

.ant-advanced-search-form .ant-form-item-control-wrapper {
  flex: 1;
}

#components-form-topic-advanced-search .ant-form {
  max-width: none;
  margin-bottom: 1%;
}

#search-offset-form-advanced-search .search-result-list {
  margin-top: 16px;
  border: 1px dashed #e9e9e9;
  border-radius: 6px;
  background-color: #fafafa;
  min-height: 200px;
  text-align: center;
  padding-top: 80px;
}

.operation-row-button {
  height: 4%;
  text-align: left;
  margin-bottom: 5px;
  margin-top: 5px;
}

.operation-btn {
  margin-right: 3%;
}
</style>

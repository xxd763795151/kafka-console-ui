<template>
  <div>
    <Header/>
    <div class="content">
      <a-spin :spinning="loading">
        <div class="user">
          <div class="operation-row-button">
            <a-button type="primary" @click="openCreateUserDialog"
            >新增</a-button
            >
          </div>
          <a-table
              :columns="columns"
              :data-source="data"
              bordered
              row-key="name"
          >
            <div slot="role" slot-scope="text, record">
              <a-select
                  @change="handleRoleChange(record.username, text)"
                  v-model="text"
                  option-filter-prop="role"
                  v-decorator="['role']"
                  style="width: 200px"
              >
                <a-select-option v-for="v in roleList" :key="v" :value="v">
                  {{ v }}
                </a-select-option>
              </a-select>
            </div>
            <div slot="operation" slot-scope="record">
              <a-popconfirm
                  :title="'删除用户: ' + record.username + ' ？'"
                  ok-text="确认"
                  cancel-text="取消"
                  @confirm="deleteUser(record.id)"
              >
                <a-button size="small" href="javascript:;" class="operation-btn"
                >删除
                </a-button>
              </a-popconfirm>
              <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="resetPassword(record.username)"
              >重置密码
              </a-button>
            </div>
          </a-table>
          <CreateUser
              :visible="showCreateUser"
              @closeCreateUserDialog="closeCreateUserDialog"
          >
          </CreateUser>
          <ResetPassword
              :visible="showResetPassword"
              :username="selectDetail.resourceName"
              @closeResetPasswordDialog="closeResetPasswordDialog"
          ></ResetPassword>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import notification from "ant-design-vue/es/notification";
import CreateUser from "@/views/user/CreateUser";
import ResetPassword from "@/views/user/ResetPassword"
import Header from "@/components/Header"
import {DevOpsUserAPi} from "../../utils/api";
export default {
  name: "DevOpsUser",
  components: {
    CreateUser,
    ResetPassword,
    Header
  },
  data() {
    return {
      queryParam: { type: "normal" },
      roleList: ["developer", "manager"],
      columns,
      showUpdateUser: false,
      deleteUserConfirm: false,
      selectDetail: {
        resourceName: "",
        resourceType: "",
        username: "",
      },
      loading: false,
      showCreateUser: false,
      showResetPassword: false,
      type: "normal",
    };
  },
  methods: {
    handleRoleChange(username, role) {
      this.loading = true;
      request({
        url: DevOpsUserAPi.updateUser.url,
        method: DevOpsUserAPi.updateUser.method,
        data: {
          "username": username,
          "role": role
        }
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.$message.success(res.msg);
          this.getDevOpsUserList();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    getDevOpsUserList() {
      Object.assign(this.queryParam, { type: this.type });
      this.loading = true;
      request({
        url: DevOpsUserAPi.userList.url,
        method: DevOpsUserAPi.userList.method,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.data = res.data;
          //this.filter();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    deleteUser(id) {
      request({
        url: DevOpsUserAPi.deleteUser.url + "?id=" + id,
        method: DevOpsUserAPi.deleteUser.method,
      }).then((res) => {
        if (res.code == 0) {
          this.$message.success(res.msg);
          this.getDevOpsUserList();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    openCreateUserDialog() {
      this.showCreateUser = true;
    },
    closeCreateUserDialog(res) {
      this.showCreateUser = false;
      if (res.refresh) {
        this.getDevOpsUserList();
      }
    },
    resetPassword(username) {
      this.selectDetail.resourceName = username;
      this.showResetPassword = true;
    },
    closeResetPasswordDialog(res) {
      this.showResetPassword = false;
      if (res.refresh) {
        this.getDevOpsUserList();
      }
    },
  },
  created() {
    this.getDevOpsUserList();
  },
};

const columns = [
  {
    title: "账号",
    dataIndex: "username",
    key: "username",
  },
  {
    title: "角色",
    dataIndex: "role",
    key: "role",
    slots: { title: "role" },
    scopedSlots: { customRender: "role" },
    width: 300
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    key: "createTime",
    slots: { title: "createTime" },
    width: 300
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
  },
];
</script>

<style scoped>
.user {
  width: 100%;
  height: 100%;
}

.operation-row-button {
  height: 4%;
  text-align: left;
  margin-bottom: 8px;
}

.operation-btn {
  margin-right: 3%;
}

</style>

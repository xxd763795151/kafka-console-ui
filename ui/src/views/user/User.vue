<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="search-offset-form-advanced-search">
        <a-form
          class="ant-advanced-search-form"
          :form="form"
          @submit="handleSearch"
        >
          <a-row :gutter="24">
            <a-col :span="16">
              <a-form-item label="用户名">
                <a-input
                  v-decorator="['username']"
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
        <a-button type="primary" @click="openCreateUserDialog()"
          >新增用户</a-button
        >
      </div>
      <a-table
        :columns="columns"
        :data-source="filteredData"
        bordered
        row-key="id"
      >
        <div slot="operation" slot-scope="record" v-show="!record.internal">
          <a-popconfirm
            :title="'删除用户: ' + record.username + '？'"
            ok-text="确认"
            cancel-text="取消"
            @confirm="deleteUser(record)"
          >
            <a-button size="small" href="javascript:;" class="operation-btn"
              >删除
            </a-button>
          </a-popconfirm>
          <a-button size="small" href="javascript:;" class="operation-btn"
            >重置密码
          </a-button>
        </div>
      </a-table>
      <CreateUser
        @closeCreateUserDialog="closeCreateUserDialog"
        :visible="showCreateUserDialog"
      ></CreateUser>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";

import notification from "ant-design-vue/lib/notification";
import { UserManageApi } from "@/utils/api";
import CreateUser from "@/views/user/CreateUser.vue";

export default {
  name: "User",
  components: { CreateUser },
  props: {
    topicList: {
      type: Array,
    },
  },
  data() {
    return {
      loading: false,
      form: this.$form.createForm(this, { name: "user" }),
      data: [],
      filteredData: [],
      filterUsername: "",
      showCreateUserDialog: false,
      columns: [
        {
          title: "用户名",
          dataIndex: "username",
          key: "username",
        },
        {
          title: "角色",
          dataIndex: "roleIds",
          key: "roleIds",
        },
        {
          title: "操作",
          key: "operation",
          scopedSlots: { customRender: "operation" },
        },
      ],
    };
  },
  methods: {
    handleSearch() {
      this.form.validateFields((err) => {
        if (!err) {
          this.loading = true;
          request({
            url: UserManageApi.getUsers.url,
            method: UserManageApi.getUsers.method,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.data = res.data;
              this.filter();
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
    refresh() {
      this.handleSearch();
    },
    filter() {
      this.filteredData = this.data.filter(
        (e) => e.username.indexOf(this.filterUsername) != -1
      );
    },
    onUsernameChange(input) {
      this.filterUsername = input.target.value;
      this.filter();
    },
    openCreateUserDialog() {
      this.showCreateUserDialog = true;
    },
    closeCreateUserDialog(p) {
      this.showCreateUserDialog = false;
      if (p.refresh) {
        this.refresh();
      }
    },
    deleteUser(user) {
      this.loading = true;
      request({
        url: UserManageApi.deleteUser.url + "?id=" + user.id,
        method: UserManageApi.deleteUser.method,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.refresh();
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
    this.handleSearch();
  },
};
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
</style>

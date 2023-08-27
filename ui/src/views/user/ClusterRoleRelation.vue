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
              <a-form-item label="角色">
                <a-input
                  v-decorator="['roleName']"
                  placeholder="请输入角色名!"
                  @change="onRoleNameChange"
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
                  刷新
                </a-button>
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
          >新增集群归属权限
        </a-button>
      </div>
      <a-table
        :columns="columns"
        :data-source="filteredData"
        bordered
        row-key="id"
      >
        <div slot="operation" slot-scope="record">
          <a-popconfirm
            title="确认删除?"
            ok-text="确认"
            cancel-text="取消"
            @confirm="deleteRelation(record)"
          >
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              v-action:user-manage:user:del
              >删除
            </a-button>
          </a-popconfirm>
        </div>
      </a-table>
      <CreateClusterRoleRelation
        @closeCreateClusterRoleRelationDialog="
          closeCreateClusterRoleRelationDialog
        "
        :visible="showCreateClusterRoleRelationDialog"
      ></CreateClusterRoleRelation>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";

import notification from "ant-design-vue/lib/notification";
import { ClusterRoleRelationApi } from "@/utils/api";
import CreateClusterRoleRelation from "@/views/user/CreateClusterRoleRelation.vue";

export default {
  name: "ClusterRoleRelation",
  components: { CreateClusterRoleRelation },
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
      filterRoleName: "",
      showCreateClusterRoleRelationDialog: false,
      columns: [
        {
          title: "角色",
          dataIndex: "roleName",
          key: "roleName",
        },
        {
          title: "集群",
          dataIndex: "clusterName",
          key: "clusterName",
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
            url: ClusterRoleRelationApi.select.url,
            method: ClusterRoleRelationApi.select.method,
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
        (e) => e.roleName.indexOf(this.filterRoleName) != -1
      );
    },
    onRoleNameChange(input) {
      this.filterRoleName = input.target.value;
      this.filter();
    },
    openCreateUserDialog() {
      this.showCreateClusterRoleRelationDialog = true;
    },
    closeCreateClusterRoleRelationDialog(p) {
      this.showCreateClusterRoleRelationDialog = false;
      if (p.refresh) {
        this.refresh();
      }
    },
    deleteRelation(user) {
      this.loading = true;
      request({
        url: ClusterRoleRelationApi.delete.url + "?id=" + user.id,
        method: ClusterRoleRelationApi.delete.method,
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

.operation-btn {
  margin-right: 3%;
}
</style>

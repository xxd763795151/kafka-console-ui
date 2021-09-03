<template>
  <div class="content">
    <div class="acl">
      <div id="components-form-demo-advanced-search">
        <a-form
          class="ant-advanced-search-form"
          :form="form"
          @submit="handleSearch"
        >
          <a-row :gutter="24">
            <a-col :span="8">
              <a-form-item :label="`用户名`">
                <a-input
                  placeholder="username"
                  class="input-w"
                  v-decorator="['username']"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="`topic`">
                <a-input
                  placeholder="topic"
                  class="input-w"
                  v-decorator="['topic']"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="`消费组`">
                <a-input
                  placeholder="groupId"
                  class="input-w"
                  v-decorator="['groupId']"
                />
              </a-form-item>
            </a-col>

            <a-col :span="24" :style="{ textAlign: 'right' }">
              <a-button type="primary" html-type="submit"> 搜索</a-button>
              <a-button :style="{ marginLeft: '8px' }" @click="handleReset">
                重置
              </a-button>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="operation-row-button">
        <a-button type="primary" @click="updateUser">新增/更新用户</a-button>
        <UpdateUser
          :visible="showUpdateUser"
          @updateUserDialogData="closeUpdateUserDialog"
        ></UpdateUser>
      </div>
      <a-table :columns="columns" :data-source="data" bordered>
        <a slot="topicList" slot-scope="topicList, record">
          <a
            href="#"
            v-for="t in topicList"
            :key="t"
            @click="onTopicDetail(t, record.username)"
            >{{ t }},
          </a>
          <AclDetail
            :visible="openAclDetailDialog"
            :selectDetail="selectDetail"
            @aclDetailDialog="closeAclDetailDialog"
          ></AclDetail>
        </a>

        <a slot="groupList" slot-scope="groupList, record">
          <a
            href="#"
            v-for="t in groupList"
            :key="t"
            @click="onGroupDetail(t, record.username)"
            >{{ t }},
          </a>
        </a>

        <a
          slot="operation"
          slot-scope="record"
          v-show="!record.user || record.user.role != 'admin'"
        >
          <a-popconfirm
            :title="'删除用户: ' + record.username + '及相关权限？'"
            ok-text="确认"
            cancel-text="取消"
            @confirm="onDeleteUser(record)"
          >
            <a href="javascript:;" class="operation-btn">删除</a>
          </a-popconfirm>
          <a
            href="javascript:;"
            class="operation-btn"
            @click="onManageProducerAuth(record)"
            >管理生产权限
            <ManageProducerAuth
              :visible="openManageProducerAuthDialog"
              :record="selectRow"
              @manageProducerAuthDialog="closeManageProducerAuthDialog"
            ></ManageProducerAuth>
          </a>

          <a
            href="javascript:;"
            class="operation-btn"
            @click="onManageConsumerAuth(record)"
            >管理消费权限
            <ManageConsumerAuth
              :visible="openManageConsumerAuthDialog"
              :record="selectRow"
              @manageConsumerAuthDialog="closeManageConsumerAuthDialog"
            ></ManageConsumerAuth>
          </a>
          <a
            href="javascript:;"
            class="operation-btn"
            @click="onAddAuth(record)"
            >增加权限
            <AddAuth
              :visible="openAddAuthDialog"
              :record="selectRow"
              @addAuthDialog="closeAddAuthDialog"
            ></AddAuth>
          </a>
        </a>
        <!--                <a-table-->
        <!--                  slot="expandedRowRender"-->
        <!--                  slot-scope="{}"-->
        <!--                  :columns="innerColumns"-->
        <!--                  :data-source="innerData"-->
        <!--                  :pagination="false"-->
        <!--                >-->
        <!--                  <span slot="status" slot-scope="{}"> <a-badge status="success" />Finished </span>-->
        <!--                  <span slot="operation" slot-scope="{}" class="table-operation">-->
        <!--                    <a>Pause</a>-->
        <!--                    <a>Stop</a>-->
        <!--                    <a-dropdown>-->
        <!--                      <a-menu slot="overlay">-->
        <!--                        <a-menu-item>-->
        <!--                          Action 1-->
        <!--                        </a-menu-item>-->
        <!--                        <a-menu-item>-->
        <!--                          Action 2-->
        <!--                        </a-menu-item>-->
        <!--                      </a-menu>-->
        <!--                      <a> More <a-icon type="down" /> </a>-->
        <!--                    </a-dropdown>-->
        <!--                  </span>-->
        <!--                </a-table>-->
      </a-table>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import notification from "ant-design-vue/es/notification";
import UpdateUser from "@/views/acl/UpdateUser";
import { KafkaAclApi } from "@/utils/api";
import ManageProducerAuth from "@/views/acl/ManageProducerAuth";
import ManageConsumerAuth from "@/views/acl/ManageConsumerAuth";
import AddAuth from "@/views/acl/AddAuth";
import AclDetail from "@/views/acl/AclDetail";

export default {
  name: "Acl",
  components: {
    UpdateUser,
    ManageProducerAuth,
    ManageConsumerAuth,
    AddAuth,
    AclDetail,
  },
  data() {
    return {
      queryParam: {},
      data: [],
      columns,
      innerColumns,
      innerData,
      selectRow: {},
      form: this.$form.createForm(this, { name: "advanced_search" }),
      showUpdateUser: false,
      deleteUserConfirm: false,
      openManageProducerAuthDialog: false,
      openManageConsumerAuthDialog: false,
      openAddAuthDialog: false,
      openAclDetailDialog: false,
      selectDetail: {
        resourceName: "",
        resourceType: "",
        username: "",
      },
    };
  },
  methods: {
    handleSearch(e) {
      e.preventDefault();
      this.form.validateFields((error, values) => {
        let queryParam = {};
        if (values.username) {
          queryParam.username = values.username;
        }
        if (values.topic) {
          queryParam.resourceType = "TOPIC";
          queryParam.resourceName = values.topic;
        } else if (values.groupId) {
          queryParam.resourceType = "GROUP";
          queryParam.resourceName = values.groupId;
        }
        Object.assign(this.queryParam, queryParam);
        getAclList(this.data, queryParam);
      });
    },

    handleReset() {
      this.form.resetFields();
    },

    updateUser() {
      this.showUpdateUser = true;
    },
    closeUpdateUserDialog(data) {
      this.showUpdateUser = data.show;
      if (data.ok) {
        getAclList(this.data, this.queryParam);
      }
    },
    onDeleteUser(row) {
      request({
        url: KafkaAclApi.deleteKafkaUser.url,
        method: KafkaAclApi.deleteKafkaUser.method,
        data: { username: row.username },
      }).then((res) => {
        getAclList(this.data, this.queryParam);
        if (res.code == 0) {
          this.$message.success(res.msg);
        } else {
          this.$message.error(res.msg);
        }
      });
    },
    onManageProducerAuth(row) {
      this.openManageProducerAuthDialog = true;
      const rowData = {};
      Object.assign(rowData, row);
      this.selectRow = rowData;
    },
    onManageConsumerAuth(row) {
      this.openManageConsumerAuthDialog = true;
      const rowData = {};
      Object.assign(rowData, row);
      this.selectRow = rowData;
    },
    onAddAuth(row) {
      this.openAddAuthDialog = true;
      const rowData = {};
      Object.assign(rowData, row);
      this.selectRow = rowData;
    },
    onTopicDetail(topic, username) {
      this.selectDetail.resourceType = "TOPIC";
      this.selectDetail.resourceName = topic;
      this.selectDetail.username = username;
      this.openAclDetailDialog = true;
    },
    onGroupDetail(group, username) {
      this.selectDetail.resourceType = "GROUP";
      this.selectDetail.resourceName = group;
      this.selectDetail.username = username;
      this.openAclDetailDialog = true;
    },
    closeManageProducerAuthDialog() {
      this.openManageProducerAuthDialog = false;
      getAclList(this.data, this.queryParam);
    },
    closeManageConsumerAuthDialog() {
      this.openManageConsumerAuthDialog = false;
      getAclList(this.data, this.queryParam);
    },
    closeAddAuthDialog() {
      this.openAddAuthDialog = false;
      getAclList(this.data, this.queryParam);
    },
    closeAclDetailDialog() {
      this.openAclDetailDialog = false;
      getAclList(this.data, this.queryParam);
    },
  },
  created() {
    getAclList(this.data, this.queryParam);
  },
};

function getAclList(data, requestParameters) {
  request({
    url: KafkaAclApi.getAclList.url,
    method: KafkaAclApi.getAclList.method,
    data: requestParameters,
  }).then((response) => {
    data.splice(0, data.length);
    if (response.code != 0) {
      notification.error({
        message: response.msg,
      });
      return;
    }
    for (let k in response.data.map) {
      let v = response.data.map[k];
      let topicList = Object.keys(v)
        .filter((e) => e.startsWith("TOPIC"))
        .map((e) => e.split("#")[1]);
      let groupList = Object.keys(v)
        .filter((e) => e.startsWith("GROUP"))
        .map((e) => e.split("#")[1]);
      data.push({
        key: k,
        username: k,
        topicList: topicList,
        groupList: groupList,
        user: response.data.map[k]["USER"],
      });
    }
  });
}

const columns = [
  { title: "用户名", dataIndex: "username", key: "username" },
  {
    title: "topic列表",
    dataIndex: "topicList",
    key: "topicList",
    slots: { title: "topicList" },
    scopedSlots: { customRender: "topicList" },
  },
  {
    title: "消费组列表",
    dataIndex: "groupList",
    key: "groupList",
    slots: { title: "groupList" },
    scopedSlots: { customRender: "groupList" },
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
  },
];

const innerColumns = [
  { title: "Date", dataIndex: "date", key: "date" },
  { title: "Name", dataIndex: "name", key: "name" },
  { title: "Status", key: "state", scopedSlots: { customRender: "status" } },
  { title: "Upgrade Status", dataIndex: "upgradeNum", key: "upgradeNum" },
  {
    title: "Action",
    dataIndex: "operation",
    key: "operation",
    scopedSlots: { customRender: "operation" },
  },
];

const innerData = [];
for (let i = 0; i < 3; ++i) {
  innerData.push({
    key: i,
    date: "2014-12-24 23:12:00",
    name: "This is production name",
    upgradeNum: "Upgraded: 56",
  });
}
</script>

<style scoped>
.acl {
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

.ant-advanced-search-form .ant-form-item-control-wrapper {
  flex: 1;
}

#components-form-demo-advanced-search .ant-form {
  max-width: none;
  margin-bottom: 1%;
}

#components-form-demo-advanced-search .search-result-list {
  margin-top: 16px;
  border: 1px dashed #e9e9e9;
  border-radius: 6px;
  background-color: #fafafa;
  min-height: 200px;
  text-align: center;
  padding-top: 80px;
}

.input-w {
  width: 400px;
}

.operation-row-button {
  height: 4%;
  text-align: left;
}

.operation-btn {
  margin-right: 1%;
}
</style>

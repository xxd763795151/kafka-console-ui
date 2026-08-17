<template>
  <a-spin :spinning="loading">
    <div class="acl">
      <div id="components-form-acl-advanced-search">
        <a-form
          class="ant-advanced-search-form"
          :model="formState"
          @finish="handleSearch"
        >
          <a-row :gutter="24">
            <a-col :span="8">
              <a-form-item :label="`用户名`" name="username">
                <a-input
                  v-model:value="formState.username"
                  placeholder="username"
                  class="input-w"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12" :style="{ textAlign: 'right' }">
              <a-button type="primary" html-type="submit"> 搜索</a-button>
              <a-button :style="{ marginLeft: '8px' }" @click="handleReset">
                重置
              </a-button>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="operation-row-button">
        <a-button
          type="primary"
          @click="updateUser"
          v-action:acl:sasl-scram:add-update
          >新增/更新用户</a-button
        >
        <span class="hint" v-show="!enableSasl"
          >未启用SASL SCRAM认证，不支持相关操作</span
        >
        <UpdateUser
          :visible="showUpdateUser"
          @updateUserDialogData="closeUpdateUserDialog"
        ></UpdateUser>
      </div>
      <a-table :columns="columns" :data-source="data" bordered>
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.dataIndex === 'username'">
            <span>{{ text }}</span>
            <a-button
              size="small"
              shape="round"
              type="dashed"
              style="float: right"
              @click="onUserDetail(text)"
              v-action:acl:sasl-scram:detail
              >详情</a-button
            >
          </template>
          <template v-else-if="column.key === 'operation'">
            <div v-show="!record.user || record.user.role != 'admin'">
              <a-popconfirm
                :title="'删除用户: ' + record.username + '？'"
                ok-text="确认"
                cancel-text="取消"
                @confirm="onDeleteUser(record)"
                v-action:acl:sasl-scram:del
              >
                <a-button size="small" href="javascript:;" class="operation-btn" type="primary" danger
                  >删除</a-button
                >
              </a-popconfirm>
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="onManageProducerAuth(record)"
                v-action:acl:sasl-scram:producer
                >管理生产权限
              </a-button>

              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="onManageConsumerAuth(record)"
                v-action:acl:sasl-scram:consumer
                >管理消费权限
              </a-button>
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="onAddAuth(record)"
                v-action:acl:sasl-scram:add-auth
                >增加权限
              </a-button>
              <a-popconfirm
                :title="'删除用户: ' + record.username + '及相关权限？'"
                ok-text="确认"
                cancel-text="取消"
                @confirm="onDeleteUserAndAuth(record)"
              >
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  v-action:acl:sasl-scram:pure
                  >彻底删除</a-button
                >
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
      <UserDetail
        :visible="openUserDetailDialog"
        :username="selectDetail.username"
        @userDetailDialog="closeUserDetailDialog"
      ></UserDetail>
      <AclDetail
        :visible="openAclDetailDialog"
        :selectDetail="selectDetail"
        @aclDetailDialog="closeAclDetailDialog"
      ></AclDetail>
      <ManageProducerAuth
        :visible="openManageProducerAuthDialog"
        :record="selectRow"
        @manageProducerAuthDialog="closeManageProducerAuthDialog"
      ></ManageProducerAuth>
      <ManageConsumerAuth
        :visible="openManageConsumerAuthDialog"
        :record="selectRow"
        @manageConsumerAuthDialog="closeManageConsumerAuthDialog"
      ></ManageConsumerAuth>
      <AddAuth
        :visible="openAddAuthDialog"
        :record="selectRow"
        @addAuthDialog="closeAddAuthDialog"
      ></AddAuth>
    </div>
  </a-spin>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'
import request from "@/utils/request";
import notification from "ant-design-vue/es/notification";
import UpdateUser from "@/views/acl/UpdateUser.vue";
import { KafkaAclApi } from "@/utils/api";
import ManageProducerAuth from "@/views/acl/ManageProducerAuth.vue";
import ManageConsumerAuth from "@/views/acl/ManageConsumerAuth.vue";
import AddAuth from "@/views/acl/AddAuth.vue";
import AclDetail from "@/views/acl/AclDetail.vue";
import UserDetail from "@/views/acl/UserDetail.vue";

export default defineComponent({
  name: "SaslScram",
  components: {
    UpdateUser,
    ManageProducerAuth,
    ManageConsumerAuth,
    AddAuth,
    AclDetail,
    UserDetail,
  },
  setup() {
    const store = useStore()
    const queryParam = reactive<any>({})
    const data = ref<any[]>([])
    const selectRow = ref<any>({})
    const showUpdateUser = ref(false)
    const deleteUserConfirm = ref(false)
    const openManageProducerAuthDialog = ref(false)
    const openManageConsumerAuthDialog = ref(false)
    const openAddAuthDialog = ref(false)
    const openAclDetailDialog = ref(false)
    const openUserDetailDialog = ref(false)
    const selectDetail = reactive({
      resourceName: "",
      resourceType: "",
      username: "",
    })
    const loading = ref(false)

    const formState = reactive({
      username: undefined as any,
    })

    const enableSasl = computed(() => (store.state as any).clusterInfo.enableSasl)

    const columns = [
      {
        title: "用户名",
        dataIndex: "username",
        key: "username",
        width: 300,
      },
      {
        title: "操作",
        key: "operation",
        width: 500,
      },
    ]

    function handleSearch(values: any) {
      const query: any = {};
      if (values.username) {
        query.username = values.username;
      }
      if (values.topic) {
        query.resourceType = "TOPIC";
        query.resourceName = values.topic;
      } else if (values.groupId) {
        query.resourceType = "GROUP";
        query.resourceName = values.groupId;
      }
      Object.assign(queryParam, query);
      getSaslScramUserList();
    }

    function handleReset() {
      formState.username = undefined
    }

    function updateUser() {
      showUpdateUser.value = true;
    }

    function closeUpdateUserDialog(data: any) {
      showUpdateUser.value = data.show;
      if (data.ok) {
        getSaslScramUserList();
      }
    }

    function onDeleteUser(row: any) {
      loading.value = true;
      request({
        url: KafkaAclApi.deleteSaslScramUser.url,
        method: KafkaAclApi.deleteSaslScramUser.method,
        data: { username: row.username },
      }).then((res: any) => {
        loading.value = false;
        getSaslScramUserList();
        if (res.code == 0) {
          message.success(res.msg);
        } else {
          message.error(res.msg);
        }
      });
    }

    function onDeleteUserAndAuth(row: any) {
      loading.value = true;
      request({
        url: KafkaAclApi.deleteKafkaUser.url,
        method: KafkaAclApi.deleteKafkaUser.method,
        data: { username: row.username },
      }).then((res: any) => {
        loading.value = false;
        getSaslScramUserList();
        if (res.code == 0) {
          message.success(res.msg);
        } else {
          message.error(res.msg);
        }
      });
    }

    function onManageProducerAuth(row: any) {
      openManageProducerAuthDialog.value = true;
      const rowData: any = {};
      Object.assign(rowData, row);
      selectRow.value = rowData;
    }

    function onManageConsumerAuth(row: any) {
      openManageConsumerAuthDialog.value = true;
      const rowData: any = {};
      Object.assign(rowData, row);
      selectRow.value = rowData;
    }

    function onAddAuth(row: any) {
      openAddAuthDialog.value = true;
      const rowData: any = {};
      Object.assign(rowData, row);
      selectRow.value = rowData;
    }

    function onTopicDetail(topic: string, username: string) {
      selectDetail.resourceType = "TOPIC";
      selectDetail.resourceName = topic;
      selectDetail.username = username;
      openAclDetailDialog.value = true;
    }

    function onGroupDetail(group: string, username: string) {
      selectDetail.resourceType = "GROUP";
      selectDetail.resourceName = group;
      selectDetail.username = username;
      openAclDetailDialog.value = true;
    }

    function onUserDetail(username: string) {
      selectDetail.username = username;
      openUserDetailDialog.value = true;
    }

    function closeManageProducerAuthDialog() {
      openManageProducerAuthDialog.value = false;
    }

    function closeManageConsumerAuthDialog() {
      openManageConsumerAuthDialog.value = false;
    }

    function closeAddAuthDialog() {
      openAddAuthDialog.value = false;
    }

    function closeAclDetailDialog(p: any) {
      openAclDetailDialog.value = false;
      if (p.refresh) {
        getSaslScramUserList();
      }
    }

    function closeUserDetailDialog() {
      openUserDetailDialog.value = false;
    }

    function getSaslScramUserList() {
      if (!enableSasl.value) {
        return;
      }
      loading.value = true;
      request({
        url: KafkaAclApi.getSaslScramUserList.url,
        method: KafkaAclApi.getSaslScramUserList.method,
        params: queryParam,
      }).then((response: any) => {
        loading.value = false;
        data.value.splice(0, data.value.length);
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
          data.value.push({
            key: k,
            username: k,
            topicList: topicList,
            groupList: groupList,
            user: response.data.map[k]["USER"],
          });
          data.value.sort((a, b) => a.username.localeCompare(b.username));
        }
      });
    }

    onMounted(() => {
      getSaslScramUserList();
    })

    return {
      queryParam,
      data,
      columns,
      selectRow,
      showUpdateUser,
      deleteUserConfirm,
      openManageProducerAuthDialog,
      openManageConsumerAuthDialog,
      openAddAuthDialog,
      openAclDetailDialog,
      openUserDetailDialog,
      selectDetail,
      loading,
      formState,
      enableSasl,
      handleSearch,
      handleReset,
      updateUser,
      closeUpdateUserDialog,
      onDeleteUser,
      onDeleteUserAndAuth,
      onManageProducerAuth,
      onManageConsumerAuth,
      onAddAuth,
      onTopicDetail,
      onGroupDetail,
      onUserDetail,
      closeManageProducerAuthDialog,
      closeManageConsumerAuthDialog,
      closeAddAuthDialog,
      closeAclDetailDialog,
      closeUserDetailDialog,
      getSaslScramUserList,
    }
  }
});
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

#components-form-acl-advanced-search .ant-form {
  max-width: none;
  margin-bottom: 1%;
}

#components-form-acl-advanced-search .search-result-list {
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
  margin-bottom: 8px;
}

.operation-btn {
  margin-right: 3%;
}

.hint {
  margin-left: 1%;
  color: red;
}
</style>

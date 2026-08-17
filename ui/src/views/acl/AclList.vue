<template>
  <div class="acl">
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
                <a-form-item :label="`主体`" name="username">
                  <a-input
                    v-model:value="formState.username"
                    placeholder="比如, 用户名"
                    class="input-w"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="`topic`" name="topic">
                  <a-input
                    v-model:value="formState.topic"
                    placeholder="topic"
                    class="input-w"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="`消费组`" name="groupId">
                  <a-input
                    v-model:value="formState.groupId"
                    placeholder="groupId"
                    class="input-w"
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
          <a-button
            type="primary"
            @click="onAddPrincipalAuth"
            v-action:acl:authority:add-principal
            >新增主体权限</a-button
          >
          <span v-show="hint != ''" class="hint"
            >broker未启用权限管理，所以不支持授权相关操作[{{ hint }}]</span
          >
        </div>
        <a-table :columns="columns" :data-source="data" bordered>
          <template #bodyCell="{ column, text, record }">
            <template v-if="column.dataIndex === 'username'">
              <span>{{ text }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'topicList'">
              <a
                href="#"
                v-for="t in text"
                :key="t"
                @click="onTopicDetail(t, record.username)"
                ><div style="border-bottom: 1px solid #e5e1e1">{{ t }}</div>
              </a>
            </template>
            <template v-else-if="column.dataIndex === 'groupList'">
              <a
                href="#"
                v-for="t in text"
                :key="t"
                @click="onGroupDetail(t, record.username)"
                ><div style="border-bottom: 1px solid #e5e1e1">{{ t }}</div>
              </a>
            </template>
            <template v-else-if="column.key === 'operation'">
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="onManageProducerAuth(record)"
                >管理生产权限
              </a-button>

              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="onManageConsumerAuth(record)"
                >管理消费权限
              </a-button>
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="onAddAuth(record)"
                >增加权限
              </a-button>
              <a-popconfirm
                :title="'清除: ' + record.username + '所有资源权限？'"
                ok-text="确认"
                cancel-text="取消"
                @confirm="onClearUserAcl(record)"
              >
                <a-button size="small" href="javascript:;" class="operation-btn"
                  >清除权限</a-button
                >
              </a-popconfirm>
            </template>
          </template>
        </a-table>
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
        <AddPrincipalAuth
          :visible="openAddPrincipalAuthDialog"
          @closeAddPrincipalAuthDialog="closeAddPrincipalAuthDialog"
        ></AddPrincipalAuth>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import request from "@/utils/request";
import notification from "ant-design-vue/es/notification";
import { KafkaAclApi } from "@/utils/api";
import ManageProducerAuth from "@/views/acl/ManageProducerAuth.vue";
import ManageConsumerAuth from "@/views/acl/ManageConsumerAuth.vue";
import AddAuth from "@/views/acl/AddAuth.vue";
import AclDetail from "@/views/acl/AclDetail.vue";
import AddPrincipalAuth from "@/views/acl/AddPrincipalAuth.vue";

export default defineComponent({
  name: "AclList",
  components: {
    ManageProducerAuth,
    ManageConsumerAuth,
    AddAuth,
    AclDetail,
    AddPrincipalAuth,
  },
  setup() {
    const queryParam = reactive<any>({})
    const data = ref<any[]>([])
    const selectRow = ref<any>({})
    const openManageProducerAuthDialog = ref(false)
    const openManageConsumerAuthDialog = ref(false)
    const openAddAuthDialog = ref(false)
    const openAclDetailDialog = ref(false)
    const openAddPrincipalAuthDialog = ref(false)
    const selectDetail = reactive({
      resourceName: "",
      resourceType: "",
      username: "",
    })
    const loading = ref(false)
    const hint = ref("")

    const formState = reactive({
      username: undefined as any,
      topic: undefined as any,
      groupId: undefined as any,
    })

    const columns = [
      {
        title: "主体标识",
        dataIndex: "username",
        key: "username",
        width: 300,
      },
      {
        title: "topic列表",
        dataIndex: "topicList",
        key: "topicList",
      },
      {
        title: "消费组列表",
        dataIndex: "groupList",
        key: "groupList",
      },
      {
        title: "操作",
        key: "operation",
        width: 500,
      },
    ]

    function handleSearch(values: any) {
      const query: any = {};
      query.username = values.username ? values.username : null;
      if (values.topic) {
        query.resourceType = "TOPIC";
        query.resourceName = values.topic;
      } else if (values.groupId) {
        query.resourceType = "GROUP";
        query.resourceName = values.groupId;
      }
      Object.assign(queryParam, query);
      getAclList();
    }

    function handleReset() {
      formState.username = undefined
      formState.topic = undefined
      formState.groupId = undefined
    }

    function onClearUserAcl(row: any) {
      loading.value = true;
      request({
        url: KafkaAclApi.clearAcl.url,
        method: KafkaAclApi.clearAcl.method,
        data: { username: row.username },
      }).then((res: any) => {
        loading.value = false;
        getAclList();
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

    function onAddPrincipalAuth() {
      openAddPrincipalAuthDialog.value = true;
    }

    function closeManageProducerAuthDialog() {
      openManageProducerAuthDialog.value = false;
      getAclList();
    }

    function closeManageConsumerAuthDialog() {
      openManageConsumerAuthDialog.value = false;
      getAclList();
    }

    function closeAddAuthDialog(p: any) {
      openAddAuthDialog.value = false;
      if (p.refresh) {
        getAclList();
      }
    }

    function closeAddPrincipalAuthDialog(p: any) {
      openAddPrincipalAuthDialog.value = false;
      if (p.refresh) {
        getAclList();
      }
    }

    function closeAclDetailDialog(p: any) {
      openAclDetailDialog.value = false;
      if (p.refresh) {
        getAclList();
      }
    }

    function getAclList() {
      loading.value = true;
      request({
        url: KafkaAclApi.getAclList.url,
        method: KafkaAclApi.getAclList.method,
        data: queryParam,
      }).then((response: any) => {
        loading.value = false;
        data.value.splice(0, data.value.length);
        if (response.code != 0) {
          notification.error({
            message: response.msg,
          });
          return;
        }
        if (!response.data.total && response.data.hint) {
          hint.value = response.data.hint;
          return;
        }
        hint.value = "";
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
      getAclList();
    })

    return {
      queryParam,
      data,
      columns,
      selectRow,
      openManageProducerAuthDialog,
      openManageConsumerAuthDialog,
      openAddAuthDialog,
      openAclDetailDialog,
      openAddPrincipalAuthDialog,
      selectDetail,
      loading,
      hint,
      formState,
      handleSearch,
      handleReset,
      onClearUserAcl,
      onManageProducerAuth,
      onManageConsumerAuth,
      onAddAuth,
      onTopicDetail,
      onGroupDetail,
      onAddPrincipalAuth,
      closeManageProducerAuthDialog,
      closeManageConsumerAuthDialog,
      closeAddAuthDialog,
      closeAddPrincipalAuthDialog,
      closeAclDetailDialog,
      getAclList,
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

<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="search-offset-form-advanced-search">
        <a-form
          class="ant-advanced-search-form"
          :model="formState"
          @finish="handleSearch"
        >
          <a-row :gutter="24">
            <a-col :span="16">
              <a-form-item label="角色" name="roleName">
                <a-input
                  v-model:value="formState.roleName"
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
        >
          新增集群归属权限
        </a-button>
      </div>
      <a-table
        :columns="columns"
        :data-source="filteredData"
        bordered
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operation'">
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
              >
                删除
              </a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
      <CreateClusterRoleRelation
        @closeCreateClusterRoleRelationDialog="
          closeCreateClusterRoleRelationDialog
        "
        :open="showCreateClusterRoleRelationDialog"
      ></CreateClusterRoleRelation>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, onMounted } from 'vue';
import request from '@/utils/request';
import notification from 'ant-design-vue/lib/notification';
import { ClusterRoleRelationApi } from '@/utils/api';
import CreateClusterRoleRelation from '@/views/user/CreateClusterRoleRelation.vue';

export default defineComponent({
  name: 'ClusterRoleRelation',
  components: { CreateClusterRoleRelation },
  props: {
    topicList: {
      type: Array,
    },
  },
  setup() {
    const state = reactive({
      loading: false,
      formState: {
        roleName: '',
      },
      data: [] as any[],
      filteredData: [] as any[],
      filterRoleName: '',
      showCreateClusterRoleRelationDialog: false,
      columns: [
        {
          title: '角色',
          dataIndex: 'roleName',
          key: 'roleName',
        },
        {
          title: '集群',
          dataIndex: 'clusterName',
          key: 'clusterName',
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
        url: ClusterRoleRelationApi.select.url,
        method: ClusterRoleRelationApi.select.method,
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
        (e: any) => e.roleName.indexOf(state.filterRoleName) != -1
      );
    };

    const onRoleNameChange = (input: any) => {
      state.filterRoleName = input.target.value;
      filter();
    };

    const openCreateUserDialog = () => {
      state.showCreateClusterRoleRelationDialog = true;
    };

    const closeCreateClusterRoleRelationDialog = (p: any) => {
      state.showCreateClusterRoleRelationDialog = false;
      if (p.refresh) {
        refresh();
      }
    };

    const deleteRelation = (user: any) => {
      state.loading = true;
      request({
        url: ClusterRoleRelationApi.delete.url + '?id=' + user.id,
        method: ClusterRoleRelationApi.delete.method,
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

    onMounted(() => {
      handleSearch();
    });

    return {
      ...toRefs(state),
      handleSearch,
      refresh,
      filter,
      onRoleNameChange,
      openCreateUserDialog,
      closeCreateClusterRoleRelationDialog,
      deleteRelation,
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

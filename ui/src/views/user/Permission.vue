<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-table
        :columns="columns"
        :data-source="data"
        v-model:expandedRowKeys="expandedRowKeys"
      >
        <template #bodyCell="{ column, text }">
          <template v-if="column.dataIndex === 'type'">
            <span v-if="text == 0" style="color: darkgreen">菜单</span>
            <span v-else>按钮</span>
          </template>
        </template>
      </a-table>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, onMounted } from 'vue';
import request from '@/utils/request';
import { UserManageApi } from '@/utils/api';
import notification from 'ant-design-vue/lib/notification';

export default defineComponent({
  name: 'Permission',
  setup() {
    const columns = [
      {
        title: '权限名称',
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: '类型',
        dataIndex: 'type',
        key: 'type',
        width: '12%',
      },
    ];

    const state = reactive({
      loading: false,
      data: [] as any[],
      columns,
      expandedRowKeys: [] as any[],
    });

    const getPermissions = () => {
      state.loading = true;
      request({
        url: UserManageApi.getPermissions.url,
        method: UserManageApi.getPermissions.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          state.data = res.data;
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    onMounted(() => {
      getPermissions();
    });

    return {
      ...toRefs(state),
      getPermissions,
    };
  },
});
</script>

<style scoped>
.editable-row-operations a {
  margin-right: 8px;
}
</style>

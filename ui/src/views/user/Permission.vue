<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-table
        :columns="columns"
        :data-source="data"
        :expanded-row-keys.sync="expandedRowKeys"
      >
        <div slot="type" slot-scope="text">
          <span v-if="text == 0" style="color: darkgreen">菜单</span
          ><span v-else>按钮</span>
        </div>
      </a-table>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";

const columns = [
  {
    title: "权限名称",
    dataIndex: "name",
    key: "name",
  },
  {
    title: "类型",
    dataIndex: "type",
    key: "type",
    width: "12%",
    slots: { title: "type" },
    scopedSlots: { customRender: "type" },
  },
];

import { UserManageApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

export default {
  name: "Permission",
  components: {},
  data() {
    return {
      loading: false,
      data: [],
      columns,
      expandedRowKeys: [],
    };
  },
  methods: {
    getPermissions() {
      this.loading = true;
      request({
        url: UserManageApi.getPermissions.url,
        method: UserManageApi.getPermissions.method,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.data = res.data;
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
    this.getPermissions();
  },
};
</script>

<style scoped>
.editable-row-operations a {
  margin-right: 8px;
}
</style>

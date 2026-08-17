<template>
  <a-modal
    :title="selectDetail.resourceName + '权限明细'"
    :open="show"
    :confirm-loading="confirmLoading"
    :width="1200"
    @cancel="handleCancel"
    :mask="false"
    :destroy-on-close="true"
    :footer="null"
    :mask-closable="false"
  >
    <a-spin :spinning="loading">
      <div>
        <a-table
          :columns="columns"
          :data-source="data"
          :row-key="
            (record, index) => {
              return index;
            }
          "
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a-popconfirm
                :title="'删除操作权限: ' + record.operation + '？'"
                ok-text="确认"
                cancel-text="取消"
                @confirm="onDelete(record)"
                v-action:acl:authority:clean
              >
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  type="primary"
                  danger
                  >删除</a-button
                >
              </a-popconfirm>
            </template>
          </template>
        </a-table>
      </div>
    </a-spin>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { KafkaAclApi } from "@/utils/api";
import request from "@/utils/request";

export default defineComponent({
  name: "AuthDetail",
  props: {
    selectDetail: {
      type: Object as () => { resourceName?: string; resourceType?: string; username?: string },
      default: () => ({ resourceName: '', resourceType: '', username: '' }),
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const confirmLoading = ref(false)
    const show = ref(props.visible)
    const data = ref<any[]>([])
    const loading = ref(false)

    const columns = [
      {
        title: "用户名",
        dataIndex: "principal",
        key: "principal",
      },
      {
        title: "资源名称",
        dataIndex: "name",
        key: "name",
      },
      {
        title: "主机",
        dataIndex: "host",
        key: "host",
      },
      {
        title: "操作类型",
        dataIndex: "operation",
        key: "operation",
      },
      {
        title: "权限类型",
        dataIndex: "permissionType",
        key: "permissionType",
      },
      {
        title: "操作",
        key: "action",
      },
    ]

    watch(() => props.visible, (v) => {
      show.value = v;
      if (show.value) {
        data.value = [];
        getAclDetail();
      }
    })

    function handleCancel() {
      emit("aclDetailDialog", { refresh: true });
    }

    function getAclDetail() {
      loading.value = true;
      const api = KafkaAclApi.getAclDetailList;
      request({
        url: api.url,
        method: api.method,
        data: props.selectDetail,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          message.error(res.msg);
        } else {
          data.value = res.data.list;
        }
      });
    }

    function onDelete(record: any) {
      const param = Object.assign({}, record);
      delete param["null"];
      const api = KafkaAclApi.deleteAcl;
      request({
        url: api.url,
        method: api.method,
        data: param,
      }).then((res: any) => {
        if (res.code != 0) {
          message.error(res.msg);
        } else {
          message.success(res.msg);
          getAclDetail();
        }
      });
    }

    return {
      confirmLoading,
      show,
      data,
      columns,
      loading,
      handleCancel,
      onDelete,
    }
  }
});
</script>

<style scoped></style>

<template>
  <div>
    <a-spin :spinning="loading">
      <a-table
        :columns="columns"
        :data-source="data"
        bordered
        :row-key="
          (record, index) => {
            return index;
          }
        "
        @change="handleChange"
      >
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.dataIndex === 'client'">
            <span v-if="text">{{ text }}</span
            ><span v-else style="color: red">默认配置</span>
          </template>
          <template v-else-if="column.dataIndex === 'user'">
            <span v-if="text">{{ text }}</span
            ><span v-else style="color: red">默认配置</span>
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-popconfirm
              title="删除当前配置？"
              ok-text="确认"
              cancel-text="取消"
              @confirm="deleteConfig(record)"
            >
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                type="primary"
                danger
                v-action:quota:del
                >删除
              </a-button>
            </a-popconfirm>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openUpdateDialog(record)"
              v-action:quota:edit
              >修改
            </a-button>
          </template>
        </template>
      </a-table>
      <UpdateQuotaConfig
        :type="type"
        :record="selectRow"
        :visible="showUpdateDialog"
        @closeUpdateQuotaDialog="closeUpdateQuotaDialog"
      ></UpdateQuotaConfig>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from "vue";
import { message } from "ant-design-vue";
import { KafkaClientQuotaApi } from "@/utils/api";
import request from "@/utils/request";
import notification from "ant-design-vue/es/notification";
import UpdateQuotaConfig from "@/views/quota/UpdateQuotaConfig.vue";

export default defineComponent({
  name: "QuotaList",
  components: { UpdateQuotaConfig },
  props: {
    columns: {
      type: Array,
    },
    data: {
      type: Array,
    },
    type: {
      type: String,
      default: "",
    },
  },
  setup(props, { emit }) {
    const record = reactive<any>({});
    const sortedInfo = ref<any>(null);
    const loading = ref<boolean>(false);
    const selectRow = reactive<any>({});
    const showUpdateDialog = ref<boolean>(false);

    const openDetailDialog = (rec: any) => {
      Object.assign(record, rec);
    };
    const handleChange = (_pagination: any, _filters: any, sorter: any) => {
      sortedInfo.value = sorter;
    };
    const deleteConfig = (rec: any) => {
      loading.value = true;
      const params: any = { type: props.type };
      params.types = [];
      params.names = [];
      if (props.type == "user") {
        params.types.push("user");
        if (rec.user) {
          params.names.push(rec.user.trim());
        } else {
          params.names.push("");
        }
      } else if (props.type == "client-id") {
        params.types.push("client-id");
        if (rec.client) {
          params.names.push(rec.client.trim());
        } else {
          params.names.push("");
        }
      }
      if (props.type == "ip") {
        params.types.push("ip");
        if (rec.ip) {
          params.names.push(rec.ip.trim());
        } else {
          params.names.push("");
        }
      }
      if (props.type == "user&client-id") {
        params.types.push("user");
        params.types.push("client-id");
        if (rec.user) {
          params.names.push(rec.user.trim());
        } else {
          params.names.push("");
        }
        if (rec.client) {
          params.names.push(rec.client.trim());
        } else {
          params.names.push("");
        }
      }
      request({
        url: KafkaClientQuotaApi.deleteClientQuotaConfigs.url,
        method: KafkaClientQuotaApi.deleteClientQuotaConfigs.method,
        data: params,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("refreshQuotaList");
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };
    const openUpdateDialog = (rec: any) => {
      Object.assign(selectRow, rec);
      showUpdateDialog.value = true;
    };
    const closeUpdateQuotaDialog = (event: any) => {
      for (const key in selectRow) {
        delete selectRow[key];
      }
      showUpdateDialog.value = false;
      if (event.refresh) {
        emit("refreshQuotaList");
      }
    };

    onMounted(() => {
      (props.columns as any[]).push({
        title: "操作",
        key: "operation",
      });
    });

    return {
      record,
      sortedInfo,
      loading,
      selectRow,
      showUpdateDialog,
      openDetailDialog,
      handleChange,
      deleteConfig,
      openUpdateDialog,
      closeUpdateQuotaDialog,
    };
  },
});
</script>

<style scoped>
.operation-btn {
  margin-right: 3%;
}
</style>

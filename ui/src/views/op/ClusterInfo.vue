<template>
  <a-modal
    title="集群信息"
    :open="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div>
          <a-button
            type="primary"
            href="javascript:;"
            class="operation-btn"
            @click="openAddClusterInfoDialog"
            v-action:op:cluster-switch:add
            >新增集群
          </a-button>
          <br /><br />
        </div>

        <a-table
          :columns="columns"
          :data-source="data"
          bordered
          :rowKey="(record: any) => record.id"
        >
          <template #bodyCell="{ column, text, record }">
            <template v-if="column.key === 'properties'">
              <div
                v-for="(p, idx) in parseProperties(record.properties)"
                :key="idx"
                >{{ p }}</div
              >
            </template>
            <template v-if="column.key === 'operation'">
              <a-button
                type="primary"
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="switchCluster(record)"
                v-action:op:cluster-switch:switch
                >切换
              </a-button>
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                @click="openUpdateClusterInfoDialog(record)"
                v-action:op:cluster-switch:edit
                >编辑
              </a-button>
              <a-popconfirm
                :title="'删除: ' + record.clusterName + '？'"
                ok-text="确认"
                cancel-text="取消"
                @confirm="deleteClusterInfo(record)"
              >
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  type="primary"
                  danger
                  v-action:op:cluster-switch:del
                  >删除
                </a-button>
              </a-popconfirm>
            </template>
          </template>
        </a-table>
        <AddClusterInfo
          :visible="showAddClusterInfoDialog"
          @closeAddClusterInfoDialog="closeAddClusterInfoDialog"
        >
        </AddClusterInfo>

        <AddClusterInfo
          :visible="showUpdateClusterInfoDialog"
          closeDialogEvent="closeUpdateClusterInfoDialog"
          @closeUpdateClusterInfoDialog="closeUpdateClusterInfoDialog"
          :cluster-info="select"
          :is-modify="true"
        >
        </AddClusterInfo>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from "vue";
import { useStore } from "vuex";
import { message } from "ant-design-vue";
import notification from "ant-design-vue/lib/notification";
import request from "@/utils/request";
import { KafkaClusterApi } from "@/utils/api";
import AddClusterInfo from "@/views/op/AddClusterInfo.vue";
import { CLUSTER } from "@/store/mutation-types";

const columns = [
  {
    title: "集群名称",
    dataIndex: "clusterName",
    key: "clusterName",
  },
  {
    title: "地址",
    dataIndex: "address",
    key: "address",
    width: 400,
  },
  {
    title: "属性",
    dataIndex: "properties",
    key: "properties",
    width: 300,
  },
  {
    title: "操作",
    key: "operation",
    width: 200,
  },
];

export default defineComponent({
  name: "Cluster",
  components: { AddClusterInfo },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const store = useStore();
    const show = ref(props.visible);
    const data = ref<any[]>([]);
    const loading = ref(false);
    const showAddClusterInfoDialog = ref(false);
    const showUpdateClusterInfoDialog = ref(false);
    const select = reactive<any>({});

    watch(
      () => props.visible,
      (v) => {
        show.value = v;
        if (show.value) {
          getClusterInfoList();
        }
      }
    );

    const switchCluster = (record: any) => {
      store.commit(CLUSTER.SWITCH, record);
    };

    const parseProperties = (properties: any): string[] => {
      if (properties == null) return [];
      if (Array.isArray(properties)) {
        return properties.map((p) => String(p)).filter((s) => s.length > 0);
      }
      if (typeof properties === "string") {
        const str = properties.trim();
        if (!str) return [];
        try {
          if (
            (str.startsWith("[") && str.endsWith("]")) ||
            (str.startsWith('"') && str.endsWith('"'))
          ) {
            const parsed = JSON.parse(str);
            if (Array.isArray(parsed)) {
              return parsed
                .map((p) => String(p).trim())
                .filter((s) => s.length > 0);
            }
          }
        } catch (e) {}
        return str
          .split(/\r?\n|,/)
          .map((s) => s.trim().replace(/^["']+|["']+$/g, ""))
          .filter((s) => s.length > 0);
      }
      return String(properties)
        .split(/\r?\n/)
        .map((s) => s.trim())
        .filter((s) => s.length > 0);
    };

    const getClusterInfoList = () => {
      loading.value = true;
      request({
        url: KafkaClusterApi.getClusterInfoList.url,
        method: KafkaClusterApi.getClusterInfoList.method,
      }).then((res: any) => {
        loading.value = false;
        data.value = res.data;
      });
    };

    const deleteClusterInfo = (record: any) => {
      request({
        url: KafkaClusterApi.deleteClusterInfo.url,
        method: KafkaClusterApi.deleteClusterInfo.method,
        data: Object.assign({}, { id: record.id }),
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          getClusterInfoList();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    const handleCancel = () => {
      data.value = [];
      emit("closeClusterInfoDialog", {});
    };

    const openAddClusterInfoDialog = () => {
      showAddClusterInfoDialog.value = true;
    };

    const closeAddClusterInfoDialog = (res: any) => {
      showAddClusterInfoDialog.value = false;
      if (res.refresh) {
        getClusterInfoList();
      }
    };

    const openUpdateClusterInfoDialog = (record: any) => {
      showUpdateClusterInfoDialog.value = true;
      const r = Object.assign({}, record);
      if (r.properties) {
        let str = "";
        r.properties.forEach((e: string) => {
          str = str + e + "\r\n";
        });
        r.properties = str;
      }
      Object.assign(select, r);
    };

    const closeUpdateClusterInfoDialog = (res: any) => {
      showUpdateClusterInfoDialog.value = false;
      if (res.refresh) {
        getClusterInfoList();
      }
    };

    return {
      columns,
      show,
      data,
      loading,
      showAddClusterInfoDialog,
      showUpdateClusterInfoDialog,
      select,
      getClusterInfoList,
      deleteClusterInfo,
      handleCancel,
      openAddClusterInfoDialog,
      closeAddClusterInfoDialog,
      openUpdateClusterInfoDialog,
      closeUpdateClusterInfoDialog,
      switchCluster,
      parseProperties,
    };
  },
});
</script>

<style scoped>
.operation-btn {
  margin-right: 3%;
}
</style>

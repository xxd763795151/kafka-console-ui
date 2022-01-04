<template>
  <a-modal
    title="集群信息"
    :visible="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
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
            >新增集群
          </a-button>
          <br /><br />
        </div>

        <a-table
          :columns="columns"
          :data-source="data"
          bordered
          :rowKey="(record) => record.id"
        >
          <div slot="properties" slot-scope="record">
            <div v-for="p in record" :key="p">{{ p }}</div>
          </div>
          <div slot="operation" slot-scope="record">
            <a-button
              type="primary"
              size="small"
              href="javascript:;"
              class="operation-btn"
              >切换
            </a-button>
            <a-button size="small" href="javascript:;" class="operation-btn"
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
                type="danger"
                >删除
              </a-button>
            </a-popconfirm>
          </div>
        </a-table>
        <AddClusterInfo
          :visible="showAddClusterInfoDialog"
          @closeAddClusterInfoDialog="closeAddClusterInfoDialog"
        >
        </AddClusterInfo>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaClusterApi } from "@/utils/api";
import AddClusterInfo from "@/views/op/AddClusterInfo";

export default {
  name: "Cluster",
  components: { AddClusterInfo },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      columns: columns,
      show: this.visible,
      data: [],
      loading: false,
      showAddClusterInfoDialog: false,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getClusterInfoList();
      }
    },
  },
  methods: {
    getClusterInfoList() {
      this.loading = true;
      request({
        url: KafkaClusterApi.getClusterInfoList.url,
        method: KafkaClusterApi.getClusterInfoList.method,
      }).then((res) => {
        this.loading = false;
        this.data = res.data;
      });
    },
    deleteClusterInfo() {},
    handleCancel() {
      this.data = [];
      this.$emit("closeClusterInfoDialog", {});
    },
    openAddClusterInfoDialog() {
      this.showAddClusterInfoDialog = true;
    },
    closeAddClusterInfoDialog(res) {
      this.showAddClusterInfoDialog = false;
      if (res.refresh) {
        this.getClusterInfoList();
      }
    },
  },
};

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
    scopedSlots: { customRender: "properties" },
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
    width: 200,
  },
];
</script>

<style scoped>
.operation-btn {
  margin-right: 3%;
}
</style>

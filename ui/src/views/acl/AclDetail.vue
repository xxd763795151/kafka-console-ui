<template>
  <a-modal
    :title="selectDetail.resourceName + '权限明细'"
    :visible="show"
    :confirm-loading="confirmLoading"
    :width="1200"
    @cancel="handleCancel"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
  >
    <a-spin :spinning="loading">
      <div>
        <a-table
          :columns="columns"
          :data-source="data"
          :rowKey="
            (record, index) => {
              return index;
            }
          "
          >>
          <a slot="action" slot-scope="record">
            <a-popconfirm
              :title="'删除操作权限: ' + record.operation + '？'"
              ok-text="确认"
              cancel-text="取消"
              @confirm="onDelete(record)"
            >
              <a-button>删除</a-button>
            </a-popconfirm>
          </a>
        </a-table>
      </div>
    </a-spin>
  </a-modal>
</template>

<script>
import { KafkaAclApi } from "@/utils/api";
import request from "@/utils/request";

export default {
  name: "AuthDetail",
  props: {
    selectDetail: {},
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      formLayout: "horizontal",
      confirmLoading: false,
      show: this.visible,
      data,
      columns,
      loading: false,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.data = [];
        this.getAclDetail();
      }
    },
  },
  methods: {
    handleCancel() {
      this.$emit("aclDetailDialog", { refresh: true });
    },
    getAclDetail() {
      this.loading = true;
      const api = KafkaAclApi.getAclDetailList;
      request({
        url: api.url,
        method: api.method,
        data: this.selectDetail,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          this.$message.error(res.msg);
        } else {
          this.data = res.data.list;
        }
      });
    },
    onDelete(record) {
      const param = Object.assign({}, record);
      delete param["null"];
      const api = KafkaAclApi.deleteAcl;
      request({
        url: api.url,
        method: api.method,
        data: param,
      }).then((res) => {
        if (res.code != 0) {
          this.$message.error(res.msg);
        } else {
          this.$message.success(res.msg);
          this.getAclDetail();
        }
      });
    },
  },
  beforeMount() {
    // this.getAclDetail();
  },
};

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
    scopedSlots: { customRender: "action" },
  },
];

const data = [];
</script>

<style scoped></style>

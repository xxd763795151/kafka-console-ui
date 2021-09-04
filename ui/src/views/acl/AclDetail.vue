<template>
  <a-modal
    :title="selectDetail.resourceName + '权限明细'"
    :visible="show"
    :confirm-loading="confirmLoading"
    :width="1200"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="提交"
    cancelText="取消"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
  >
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
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getAclDetail();
      }
    },
  },
  methods: {
    handleOk() {
      const form = this.form;
      form.validateFields((e, v) => {
        if (e) {
          return;
        }
        const param = Object.assign({}, v);
        const api = KafkaAclApi.addAclAuth;
        this.confirmLoading = true;
        request({
          url: api.url,
          method: api.method,
          data: param,
        }).then((res) => {
          this.confirmLoading = false;
          if (res.code == 0) {
            this.$message.success(res.msg);
            this.$emit("aclDetailDialog", v);
          } else {
            this.$message.error(res.msg);
          }
        });
      });
    },
    handleCancel() {
      this.$emit("aclDetailDialog", {});
    },
    getAclDetail() {
      const api = KafkaAclApi.getAclDetailList;
      request({
        url: api.url,
        method: api.method,
        data: this.selectDetail,
      }).then((res) => {
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

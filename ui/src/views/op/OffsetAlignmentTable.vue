<template>
  <a-modal
    title="位移对齐记录"
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
        <a-table :columns="columns" bordered :data-source="data" :rowKey="id">
          <ul slot="thisOffset" slot-scope="text">
            <ol v-for="(v, k) in text" :key="k">
              {{
                k
              }}:
              {{
                v
              }}
            </ol>
          </ul>
          <ul slot="thatOffset" slot-scope="text">
            <ol v-for="(v, k) in text" :key="k">
              {{
                k
              }}:
              {{
                v
              }}
            </ol>
          </ul>
          <div slot="operation" slot-scope="record">
            <a-popconfirm
              title="删除当前记录？"
              ok-text="确认"
              cancel-text="取消"
              @confirm="onDeleteOffsetAlignment(record)"
            >
              <a-button size="small" href="javascript:;" class="operation-btn"
                >删除</a-button
              >
            </a-popconfirm>
          </div>
        </a-table>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaOpApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
export default {
  name: "OffsetAlignmentTable",
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
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getAlignmentList();
      }
    },
  },
  methods: {
    getAlignmentList() {
      this.loading = true;
      request({
        url: KafkaOpApi.getOffsetAlignmentList.url,
        method: KafkaOpApi.getOffsetAlignmentList.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.data = res.data;
        }
      });
    },
    handleCancel() {
      this.data = [];
      this.$emit("closeOffsetAlignmentInfoDialog", {});
    },
    onDeleteOffsetAlignment(record) {
      this.loading = true;
      request({
        url: KafkaOpApi.deleteAlignment.url + "?id=" + record.id,
        method: KafkaOpApi.deleteAlignment.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.$message.success(res.msg);
          this.getAlignmentList();
        }
      });
    },
  },
};

const columns = [
  {
    title: "消费组",
    dataIndex: "groupId",
    key: "groupId",
  },
  {
    title: "Topic",
    dataIndex: "topic",
    key: "topic",
  },
  {
    title: "当前集群标记位点",
    dataIndex: "thisOffset",
    key: "thisOffset",
    scopedSlots: { customRender: "thisOffset" },
  },
  {
    title: "外部集群标记位点",
    dataIndex: "thatOffset",
    key: "thatOffset",
    scopedSlots: { customRender: "thatOffset" },
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    key: "updateTime",
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
  },
];
</script>

<style scoped></style>

<template>
  <div>
    <a-table
      :columns="columns"
      :data-source="data"
      bordered
      :row-key="
        (record, index) => {
          return index;
        }
      "
    >
      <div slot="operation" slot-scope="record">
        <a-button
          size="small"
          href="javascript:;"
          class="operation-btn"
          @click="openDetailDialog(record)"
          >消息详情
        </a-button>
      </div>
    </a-table>
    <MessageDetail
      :visible="showDetailDialog"
      :record="record"
      @closeDetailDialog="closeDetailDialog"
    ></MessageDetail>
  </div>
</template>

<script>
import moment from "moment";
import MessageDetail from "@/views/message/MessageDetail";
export default {
  name: "MessageList",
  components: { MessageDetail },
  props: {
    data: {
      type: Array,
    },
  },
  data() {
    return {
      columns: columns,
      showDetailDialog: false,
      record: {},
    };
  },
  methods: {
    openDetailDialog(record) {
      this.record = record;
      this.showDetailDialog = true;
    },
    closeDetailDialog() {
      this.showDetailDialog = false;
    },
  },
};
const columns = [
  {
    title: "topic",
    dataIndex: "topic",
    key: "topic",
    width: 300,
  },
  {
    title: "分区",
    dataIndex: "partition",
    key: "partition",
  },
  {
    title: "偏移",
    dataIndex: "offset",
    key: "offset",
  },
  {
    title: "时间",
    dataIndex: "timestamp",
    key: "timestamp",
    slots: { title: "timestamp" },
    scopedSlots: { customRender: "timestamp" },
    customRender: (text) => {
      return moment(text).format("YYYY-MM-DD HH:mm:ss:SSS");
    },
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
    width: 200,
  },
];
</script>

<style scoped></style>

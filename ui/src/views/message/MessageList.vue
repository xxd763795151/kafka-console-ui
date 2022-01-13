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
      @change="handleChange"
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
      showDetailDialog: false,
      record: {},
      sortedInfo: null,
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
    handleChange() {
      this.sortedInfo = arguments[2];
    },
  },
  computed: {
    columns() {
      let sortedInfo = this.sortedInfo || {};
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
            return text == -1
              ? -1
              : moment(text).format("YYYY-MM-DD HH:mm:ss:SSS");
          },
          sorter: (a, b) => a.timestamp - b.timestamp,
          sortOrder: sortedInfo.columnKey === "timestamp" && sortedInfo.order,
          sortDirections: ["ascend", "descend"],
        },
        {
          title: "操作",
          key: "operation",
          scopedSlots: { customRender: "operation" },
          width: 200,
        },
      ];
      return columns;
    },
  },
};
</script>

<style scoped></style>

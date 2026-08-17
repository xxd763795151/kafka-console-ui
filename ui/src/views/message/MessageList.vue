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
      <template #operation="record">
        <a-button
          size="small"
          href="javascript:;"
          class="operation-btn"
          @click="openDetailDialog(record)"
          v-action:message:detail
          >消息详情
        </a-button>
      </template>
    </a-table>
    <MessageDetail
      :visible="showDetailDialog"
      :record="record"
      @closeDetailDialog="closeDetailDialog"
    ></MessageDetail>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, computed } from "vue";
import dayjs from "dayjs";
import MessageDetail from "@/views/message/MessageDetail.vue";

interface RecordItem {
  topic: string;
  partition: number;
  offset: number;
  timestamp: number;
  [key: string]: any;
}

interface SortedInfo {
  columnKey?: string;
  order?: string;
  [key: string]: any;
}

export default defineComponent({
  name: "MessageList",
  components: { MessageDetail },
  props: {
    data: {
      type: Array,
      default: () => [],
    },
  },
  setup() {
    const state = reactive({
      showDetailDialog: false,
      record: {} as RecordItem,
      sortedInfo: null as SortedInfo | null,
    });

    const openDetailDialog = (record: RecordItem) => {
      state.record = record;
      state.showDetailDialog = true;
    };

    const closeDetailDialog = () => {
      state.showDetailDialog = false;
    };

    const handleChange = (_pagination: any, _filters: any, sorter: any) => {
      state.sortedInfo = sorter;
    };

    const columns = computed(() => {
      let sortedInfo = state.sortedInfo || {};
      const cols = [
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
          customRender: ({ text }: { text: number }) => {
            return text == -1
              ? -1
              : dayjs(text).format("YYYY-MM-DD HH:mm:ss:SSS");
          },
          sorter: (a: RecordItem, b: RecordItem) => a.timestamp - b.timestamp,
          sortOrder: sortedInfo.columnKey === "timestamp" && sortedInfo.order,
          sortDirections: ["ascend", "descend"],
        },
        {
          title: "操作",
          key: "operation",
          width: 200,
        },
      ];
      return cols;
    });

    return {
      ...toRefs(state),
      openDetailDialog,
      closeDetailDialog,
      handleChange,
      columns,
    };
  },
});
</script>

<style scoped></style>

<template>
  <a-modal
    title="位移对齐记录"
    :open="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          bordered
          :data-source="data"
          :rowKey="(record: any) => record.id"
        >
          <template #bodyCell="{ column, text, record }">
            <template v-if="column.key === 'thisOffset'">
              <ul>
                <ol v-for="(v, k) in text" :key="k">
                  {{
                    k
                  }}:
                  {{
                    v
                  }}
                </ol>
              </ul>
            </template>
            <template v-if="column.key === 'thatOffset'">
              <ul>
                <ol v-for="(v, k) in text" :key="k">
                  {{
                    k
                  }}:
                  {{
                    v
                  }}
                </ol>
              </ul>
            </template>
            <template v-if="column.key === 'operation'">
              <a-popconfirm
                title="删除当前记录？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="onDeleteOffsetAlignment(record)"
              >
                <a-button size="small" href="javascript:;" class="operation-btn" type="primary" danger
                  >删除</a-button
                >
              </a-popconfirm>
            </template>
          </template>
        </a-table>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from "vue";
import { message } from "ant-design-vue";
import notification from "ant-design-vue/es/notification";
import request from "@/utils/request";
import { KafkaOpApi } from "@/utils/api";

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
  },
  {
    title: "外部集群标记位点",
    dataIndex: "thatOffset",
    key: "thatOffset",
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    key: "updateTime",
  },
  {
    title: "操作",
    key: "operation",
  },
];

export default defineComponent({
  name: "OffsetAlignmentTable",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const show = ref(props.visible);
    const data = ref<any[]>([]);
    const loading = ref(false);

    watch(
      () => props.visible,
      (v) => {
        show.value = v;
        if (show.value) {
          getAlignmentList();
        }
      }
    );

    const getAlignmentList = () => {
      loading.value = true;
      request({
        url: KafkaOpApi.getOffsetAlignmentList.url,
        method: KafkaOpApi.getOffsetAlignmentList.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          data.value = res.data;
        }
      });
    };

    const handleCancel = () => {
      data.value = [];
      emit("closeOffsetAlignmentInfoDialog", {});
    };

    const onDeleteOffsetAlignment = (record: any) => {
      loading.value = true;
      request({
        url: KafkaOpApi.deleteAlignment.url + "?id=" + record.id,
        method: KafkaOpApi.deleteAlignment.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          message.success(res.msg);
          getAlignmentList();
        }
      });
    };

    return {
      columns,
      show,
      data,
      loading,
      getAlignmentList,
      handleCancel,
      onDeleteOffsetAlignment,
    };
  },
});
</script>

<style scoped></style>

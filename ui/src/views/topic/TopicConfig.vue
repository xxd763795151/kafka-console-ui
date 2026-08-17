<template>
  <a-modal
    :title="topic + '配置'"
    :open="show"
    :width="1400"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div>
          <a-input-search
            placeholder="属性"
            style="width: 200px"
            v-model:value="search"
            @input="searchData"
            @search="searchData"
          />
          <br /><br />
        </div>

        <a-table
          :columns="columns"
          :data-source="filterData"
          bordered
          :rowKey="(record) => record.name"
        >
          <template #bodyCell="{ column, text, record }">
            <template v-if="column.key === 'operation'">
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                v-show="!record.readOnly"
                @click="openEditConfigDialog(record)"
                v-action:topic:property-config:edit
                >编辑
              </a-button>
              <a-popconfirm
                :title="'删除配置项: ' + record.name + '？'"
                ok-text="确认"
                cancel-text="取消"
                v-show="isDynamic(record.source)"
                @confirm="deleteTopicConfig(record)"
              >
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  type="primary"
                  danger
                  v-action:topic:property-config:del
                  >删除
                </a-button>
              </a-popconfirm>
            </template>
          </template>
        </a-table>
        <EditConfig
          :open="showEditConfigDialog"
          :record="selectData"
          :topic="topic"
          @closeEditConfigDialog="closeEditConfigDialog"
        ></EditConfig>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from "vue";
import { message, notification } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaConfigApi } from "@/utils/api";
import EditConfig from "@/views/topic/EditConfig.vue";

const columns = [
  {
    title: "属性",
    dataIndex: "name",
    key: "name",
    width: 300,
  },
  {
    title: "值",
    dataIndex: "value",
    key: "value",
  },
  {
    title: "属性源",
    dataIndex: "source",
    key: "source",
    width: 200,
  },
  {
    title: "操作",
    key: "operation",
    width: 150,
  },
];

export default defineComponent({
  name: "TopicConfig",
  components: { EditConfig },
  props: {
    topic: {
      type: String,
      default: "",
    },
    open: {
      type: Boolean,
      default: false,
    },
  },
  emits: ["closeTopicConfigDialog"],
  setup(props, { emit }) {
    const show = ref(props.open);
    const data = ref<any[]>([]);
    const loading = ref(false);
    const search = ref("");
    const filterData = ref<any[]>([]);
    const showEditConfigDialog = ref(false);
    const selectData = ref<any>({});

    watch(
      () => props.open,
      (v) => {
        show.value = v;
        if (show.value) {
          getTopicConfig();
        }
      }
    );

    function getTopicConfig() {
      loading.value = true;
      const api = KafkaConfigApi.getTopicConfig;
      request({
        url: api.url + "?topic=" + props.topic,
        method: api.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          data.value = res.data;
          searchData();
        }
      });
    }

    function deleteTopicConfig(record: any) {
      selectData.value = record;
      loading.value = true;
      const api = KafkaConfigApi.deleteTopicConfig;
      request({
        url: api.url,
        method: api.method,
        data: {
          name: record.name,
          value: record.value,
          entity: props.topic,
        },
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          message.success(res.msg);
          getTopicConfig();
        }
      });
    }

    function searchData() {
      filterData.value = data.value.filter(
        (e) => e.name.indexOf(search.value) >= 0
      );
    }

    function handleCancel() {
      data.value = [];
      emit("closeTopicConfigDialog", {});
    }

    function openEditConfigDialog(record: any) {
      showEditConfigDialog.value = true;
      selectData.value = record;
    }

    function closeEditConfigDialog(params: { refresh: boolean }) {
      showEditConfigDialog.value = false;
      if (params.refresh) {
        getTopicConfig();
      }
    }

    function isDynamic(source: string) {
      return source.startsWith("DYNAMIC_");
    }

    return {
      columns,
      show,
      data,
      loading,
      search,
      filterData,
      showEditConfigDialog,
      selectData,
      getTopicConfig,
      deleteTopicConfig,
      searchData,
      handleCancel,
      openEditConfigDialog,
      closeEditConfigDialog,
      isDynamic,
    };
  },
});
</script>

<style scoped>
.operation-btn {
  margin-right: 3%;
}
</style>

<template>
  <div class="content" v-action:topic:load>
    <a-spin :spinning="loading">
      <div class="topic">
        <div id="components-form-topic-advanced-search">
          <a-form
            class="ant-advanced-search-form"
            :model="queryParam"
            @submit="handleSearch"
          >
            <a-row :gutter="24">
              <a-col :span="8">
                <a-form-item label="topic">
                  <a-input
                    placeholder="topic"
                    class="input-w"
                    v-model:value="queryParam.topic"
                    @change="onTopicUpdate"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="类型">
                  <a-select
                    class="type-select"
                    v-model:value="type"
                    placeholder="选择类型"
                    :filter-option="true"
                    option-filter-prop="label"
                    @change="getTopicList"
                  >
                    <a-select-option value="all" :label="'所有'"> 所有</a-select-option>
                    <a-select-option value="normal" :label="'普通'"> 普通</a-select-option>
                    <a-select-option value="system" :label="'系统'"> 系统</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>

              <a-col :span="8" :style="{ textAlign: 'right' }">
                <a-form-item>
                  <a-button
                    type="primary"
                    html-type="submit"
                    v-action:topic:load
                  >
                    刷新</a-button
                  >
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>
        <div class="operation-row-button">
          <a-button
            type="primary"
            @click="openCreateTopicDialog"
            v-action:topic:add
            >新增</a-button
          >
          <a-popconfirm
            title="删除这些Topic?"
            ok-text="确认"
            cancel-text="取消"
            @confirm="deleteTopics(selectedRowKeys)"
          >
            <a-button
              type="primary"
              danger
              class="btn-left"
              :disabled="!hasSelected"
              :loading="loading"
              v-action:topic:batch-del
            >
              批量删除
            </a-button>
          </a-popconfirm>
          <span style="margin-left: 8px">
            <template v-if="hasSelected">
              {{ `已选择 ${selectedRowKeys.length} 个Topic` }}
            </template>
          </span>
        </div>
        <a-table
          :columns="columns"
          :data-source="filteredData"
          :row-selection="{
            selectedRowKeys: selectedRowKeys,
            onChange: onSelectChange,
          }"
          bordered
          row-key="name"
        >
          <template #bodyCell="{ column, text, record }">
            <template v-if="column.key === 'partitions'">
              <a href="#" @click.prevent="openPartitionInfoDialog(record.name)"
                >{{ text }}
              </a>
            </template>
            <template v-else-if="column.key === 'internal'">
              <span v-if="text" style="color: red">是</span><span v-else>否</span>
            </template>
            <template v-else-if="column.key === 'operation'">
              <template v-if="!record.internal">
                <a-popconfirm
                  :title="'删除topic: ' + record.name + '？'"
                  ok-text="确认"
                  cancel-text="取消"
                  @confirm="deleteTopic(record.name)"
                >
                  <a-button
                    size="small"
                    href="javascript:;"
                    class="operation-btn"
                    type="primary"
                    danger
                    v-action:topic:del
                    >删除
                  </a-button>
                </a-popconfirm>
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="openPartitionInfoDialog(record.name)"
                  v-action:topic:partition-detail
                  >分区详情
                </a-button>
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="openAddPartitionDialog(record.name)"
                  v-action:topic:partition-add
                  >增加分区
                </a-button>
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="openConsumedDetailDialog(record.name)"
                  v-action:topic:consumer-detail
                  >消费详情
                </a-button>
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="openTopicConfigDialog(record.name)"
                  v-action:topic:property-config
                  >属性配置
                </a-button>
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="openUpdateReplicaDialog(record.name)"
                  v-action:topic:replication-modify
                  >变更副本
                </a-button>
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="openMessageStatsDialog(record.name)"
                  v-action:topic:send-count
                  >发送统计
                </a-button>
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="openThrottleDialog(record.name)"
                  v-action:topic:replication-sync-throttle
                  >限流
                </a-button>
              </template>
            </template>
          </template>
        </a-table>
        <PartitionInfo
          :topic="selectDetail.resourceName"
          :open="showPartitionInfo"
          @closePartitionInfoDialog="closePartitionInfoDialog"
        ></PartitionInfo>
        <CreateTopic
          :open="showCreateTopic"
          @closeCreateTopicDialog="closeCreateTopicDialog"
        >
        </CreateTopic>
        <AddPartition
          :open="showAddPartition"
          :topic="selectDetail.resourceName"
          @closeAddPartitionDialog="closeAddPartitionDialog"
        ></AddPartition>
        <ConsumedDetail
          :open="showConsumedDetailDialog"
          :topic="selectDetail.resourceName"
          @closeConsumedDetailDialog="closeConsumedDetailDialog"
        >
        </ConsumedDetail>
        <TopicConfig
          :open="showTopicConfigDialog"
          :topic="selectDetail.resourceName"
          @closeTopicConfigDialog="closeTopicConfigDialog"
        ></TopicConfig>
        <UpdateReplica
          :open="showUpdateReplicaDialog"
          :topic="selectDetail.resourceName"
          @closeUpdateReplicaDialog="closeUpdateReplicaDialog"
        ></UpdateReplica>
        <ConfigTopicThrottle
          :open="showThrottleDialog"
          :topic="selectDetail.resourceName"
          @closeThrottleDialog="closeThrottleDialog"
        ></ConfigTopicThrottle>
        <SendStats
          :open="showSendStatsDialog"
          :topic="selectDetail.resourceName"
          @closeMessageStatsDialog="closeMessageStatsDialog"
        ></SendStats>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, computed } from "vue";
import { message, notification } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import PartitionInfo from "@/views/topic/PartitionInfo.vue";
import CreateTopic from "@/views/topic/CreateTopic.vue";
import AddPartition from "@/views/topic/AddPartition.vue";
import ConsumedDetail from "@/views/topic/ConsumedDetail.vue";
import TopicConfig from "@/views/topic/TopicConfig.vue";
import UpdateReplica from "@/views/topic/UpdateReplica.vue";
import ConfigTopicThrottle from "@/views/topic/ConfigTopicThrottle.vue";
import SendStats from "@/views/topic/SendStats.vue";

const columns = [
  {
    title: "topic",
    dataIndex: "name",
    key: "name",
    width: 300,
  },
  {
    title: "分区数",
    dataIndex: "partitions",
    key: "partitions",
  },
  {
    title: "内部topic",
    dataIndex: "internal",
    key: "internal",
  },
  {
    title: "操作",
    key: "operation",
    width: 800,
  },
];

export default defineComponent({
  name: "Topic",
  components: {
    PartitionInfo,
    CreateTopic,
    AddPartition,
    ConsumedDetail,
    TopicConfig,
    UpdateReplica,
    ConfigTopicThrottle,
    SendStats,
  },
  setup() {
    const queryParam = reactive<{ type: string; topic?: string }>({
      type: "normal",
    });
    const data = ref<any[]>([]);
    const showUpdateUser = ref(false);
    const deleteUserConfirm = ref(false);
    const selectDetail = reactive({
      resourceName: "",
      resourceType: "",
      username: "",
    });
    const showPartitionInfo = ref(false);
    const loading = ref(false);
    const showCreateTopic = ref(false);
    const showAddPartition = ref(false);
    const showConsumedDetailDialog = ref(false);
    const showTopicConfigDialog = ref(false);
    const showUpdateReplicaDialog = ref(false);
    const showThrottleDialog = ref(false);
    const showSendStatsDialog = ref(false);
    const filterTopic = ref("");
    const filteredData = ref<any[]>([]);
    const type = ref("normal");
    const selectedRowKeys = ref<any[]>([]);

    const hasSelected = computed(() => selectedRowKeys.value.length > 0);

    function handleSearch(e: Event) {
      e.preventDefault();
      getTopicList();
    }

    function handleReset() {
      queryParam.topic = "";
    }

    function getTopicList() {
      Object.assign(queryParam, { type: type.value });
      loading.value = true;
      request({
        url: KafkaTopicApi.getTopicList.url,
        method: KafkaTopicApi.getTopicList.method,
        params: queryParam,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          data.value = res.data;
          filter();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    }

    function deleteTopics(topics: any[]) {
      request({
        url: KafkaTopicApi.deleteTopic.url,
        method: KafkaTopicApi.deleteTopic.method,
        data: topics,
      }).then((res: any) => {
        if (res.code == 0) {
          message.success(res.msg);
          getTopicList();
          selectedRowKeys.value = [];
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    }

    function deleteTopic(topic: string) {
      deleteTopics([topic]);
    }

    function onTopicUpdate(input: Event) {
      const target = input.target as HTMLInputElement;
      filterTopic.value = target.value;
      filter();
    }

    function filter() {
      filteredData.value = data.value.filter(
        (e) => e.name.indexOf(filterTopic.value) != -1
      );
    }

    function openPartitionInfoDialog(topic: string) {
      selectDetail.resourceName = topic;
      showPartitionInfo.value = true;
    }

    function closePartitionInfoDialog() {
      showPartitionInfo.value = false;
    }

    function openCreateTopicDialog() {
      showCreateTopic.value = true;
    }

    function closeCreateTopicDialog(res: { refresh: boolean }) {
      showCreateTopic.value = false;
      if (res.refresh) {
        getTopicList();
      }
    }

    function openAddPartitionDialog(topic: string) {
      selectDetail.resourceName = topic;
      showAddPartition.value = true;
    }

    function closeAddPartitionDialog(res: { refresh: boolean }) {
      showAddPartition.value = false;
      if (res.refresh) {
        getTopicList();
      }
    }

    function openConsumedDetailDialog(topic: string) {
      showConsumedDetailDialog.value = true;
      selectDetail.resourceName = topic;
    }

    function closeConsumedDetailDialog() {
      showConsumedDetailDialog.value = false;
    }

    function openTopicConfigDialog(topic: string) {
      showTopicConfigDialog.value = true;
      selectDetail.resourceName = topic;
    }

    function closeTopicConfigDialog() {
      showTopicConfigDialog.value = false;
    }

    function openUpdateReplicaDialog(topic: string) {
      showUpdateReplicaDialog.value = true;
      selectDetail.resourceName = topic;
    }

    function closeUpdateReplicaDialog() {
      showUpdateReplicaDialog.value = false;
    }

    function openMessageStatsDialog(topic: string) {
      showSendStatsDialog.value = true;
      selectDetail.resourceName = topic;
    }

    function closeMessageStatsDialog() {
      showSendStatsDialog.value = false;
    }

    function openThrottleDialog(topic: string) {
      showThrottleDialog.value = true;
      selectDetail.resourceName = topic;
    }

    function closeThrottleDialog() {
      showThrottleDialog.value = false;
    }

    function onSelectChange(keys: any[]) {
      selectedRowKeys.value = keys;
    }

    getTopicList();
    selectedRowKeys.value = [];

    return {
      queryParam,
      data,
      columns,
      showUpdateUser,
      deleteUserConfirm,
      selectDetail,
      showPartitionInfo,
      loading,
      showCreateTopic,
      showAddPartition,
      showConsumedDetailDialog,
      showTopicConfigDialog,
      showUpdateReplicaDialog,
      showThrottleDialog,
      showSendStatsDialog,
      filterTopic,
      filteredData,
      type,
      selectedRowKeys,
      hasSelected,
      handleSearch,
      handleReset,
      getTopicList,
      deleteTopics,
      deleteTopic,
      onTopicUpdate,
      filter,
      openPartitionInfoDialog,
      closePartitionInfoDialog,
      openCreateTopicDialog,
      closeCreateTopicDialog,
      openAddPartitionDialog,
      closeAddPartitionDialog,
      openConsumedDetailDialog,
      closeConsumedDetailDialog,
      openTopicConfigDialog,
      closeTopicConfigDialog,
      openUpdateReplicaDialog,
      closeUpdateReplicaDialog,
      openMessageStatsDialog,
      closeMessageStatsDialog,
      openThrottleDialog,
      closeThrottleDialog,
      onSelectChange,
    };
  },
});
</script>

<style scoped>
.topic {
  width: 100%;
  height: 100%;
}

.ant-advanced-search-form {
  padding: 24px;
  background: #fbfbfb;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.ant-advanced-search-form .ant-form-item {
  display: flex;
}

.ant-advanced-search-form .ant-form-item-control-wrapper {
  flex: 1;
}

#components-form-topic-advanced-search .ant-form {
  max-width: none;
  margin-bottom: 1%;
}

#components-form-topic-advanced-search .search-result-list {
  margin-top: 16px;
  border: 1px dashed #e9e9e9;
  border-radius: 6px;
  background-color: #fafafa;
  min-height: 200px;
  text-align: center;
  padding-top: 80px;
}

.input-w {
  width: 400px;
}

.operation-row-button {
  height: 4%;
  text-align: left;
  margin-bottom: 8px;
}

.operation-btn {
  margin-right: 3%;
}

.type-select {
  width: 200px !important;
}

.btn-left {
  margin-left: 1%;
}
</style>

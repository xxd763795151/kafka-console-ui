<template>
  <a-modal
    title="副本重分配"
    :open="show"
    :width="800"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :model="formState"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @finish="handleSubmit"
        >
          <a-form-item
            label="Topic"
            name="topic"
            :rules="[{ required: true, message: '请选择一个topic!' }]"
          >
            <a-select
              v-model:value="formState.topic"
              @change="handleTopicChange"
              show-search
              :filter-option="true"
              option-filter-prop="label"
              placeholder="请选择一个topic"
            >
              <a-select-option v-for="v in topicList" :key="v" :value="v" :label="String(v)">
                {{ v }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item
            label="分配到Broker"
            name="brokers"
            :rules="[{ required: true, message: '请选择一个broker!' }]"
          >
            <a-select
              v-model:value="formState.brokers"
              mode="multiple"
              :filter-option="true"
              option-filter-prop="label"
              placeholder="请选择一个broker"
            >
              <a-select-option v-for="v in brokers" :key="v" :value="v">
                <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-table
            bordered
            :columns="columns"
            :data-source="currentAssignment"
            :rowKey="
              (record: any, index: number) => {
                return index;
              }
            "
          >
          </a-table>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit">
              重新生成分配计划
            </a-button>
          </a-form-item>
        </a-form>
        <hr />
        <h2>新的分配计划</h2>
        <a-table
          bordered
          :columns="columns"
          :data-source="proposedAssignmentShow"
          :rowKey="
            (record: any, index: number) => {
              return index;
            }
          "
        >
        </a-table>
        <a-button type="primary" danger @click="updateAssignment"> 更新分配 </a-button>
      </a-spin>
      <hr />
      <h4>注意</h4>
      <ul>
        <li>
          副本重分配，可以将副本分配到其它broker上，通过选择上面的broker节点，根据这几个节点生成分配方案
        </li>
        <li>
          选择的broker的节点数量不能少于当前的副本数，比如有3个副本，至少需要3个broker节点
        </li>
        <li>
          数据量太大，考虑设置一下限流，毕竟重新分配后，不同broker之间可能做数据迁移
        </li>
      </ul>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import notification from "ant-design-vue/es/notification";
import request from "@/utils/request";
import { KafkaTopicApi, KafkaOpApi, KafkaClusterApi } from "@/utils/api";

const columns = [
  {
    title: "分区",
    dataIndex: "partition",
    key: "partition",
  },
  {
    title: "副本所在broker",
    dataIndex: "replicas",
    key: "replicas",
  },
];

export default defineComponent({
  name: "ReplicaReassign",
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
    const topicList = ref<any[]>([]);
    const partitions = ref<any[]>([]);
    const brokers = ref<any[]>([]);
    const currentAssignment = ref<any[]>([]);
    const proposedAssignment = ref<any[]>([]);
    const proposedAssignmentShow = ref<any[]>([]);

    const formState = reactive({
      topic: undefined as any,
      brokers: [] as any[],
    });

    watch(
      () => props.visible,
      (v) => {
        show.value = v;
        if (show.value) {
          clearData();
          getTopicNameList();
          getClusterInfo();
        }
      }
    );

    const handleSubmit = (values: any) => {
      getProposedAssignment(values);
    };

    const getTopicReplicaInfo = (topic: string) => {
      loading.value = true;
      request({
        url: KafkaTopicApi.getCurrentReplicaAssignment.url + "?topic=" + topic,
        method: KafkaTopicApi.getCurrentReplicaAssignment.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          currentAssignment.value = res.data.partitions;
          currentAssignment.value.forEach(
            (e: any) => (e.replicas = e.replicas.join(","))
          );
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    const getTopicNameList = () => {
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
      }).then((res: any) => {
        if (res.code == 0) {
          topicList.value = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    const getPartitionInfo = (topic: string) => {
      loading.value = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + topic,
        method: KafkaTopicApi.getPartitionInfo.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          partitions.value = res.data.map((v: any) => v.partition);
          partitions.value.splice(0, 0, -1);
        }
      });
    };

    const handleTopicChange = (topic: string) => {
      clearData();
      getTopicReplicaInfo(topic);
    };

    const getClusterInfo = () => {
      loading.value = true;
      request({
        url: KafkaClusterApi.getClusterInfo.url,
        method: KafkaClusterApi.getClusterInfo.method,
      }).then((res: any) => {
        loading.value = false;
        brokers.value = [];
        formState.brokers = [];
        res.data.nodes.forEach((node: any) => brokers.value.push(node.id));
      });
    };

    const getProposedAssignment = (params: any) => {
      loading.value = true;
      request({
        url: KafkaOpApi.proposedAssignment.url,
        method: KafkaOpApi.proposedAssignment.method,
        data: params,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          proposedAssignmentShow.value = res.data;
          proposedAssignment.value = JSON.parse(
            JSON.stringify(proposedAssignmentShow.value)
          );
          proposedAssignmentShow.value.forEach(
            (e: any) => (e.replicas = e.replicas.join(","))
          );
        }
      });
    };

    const clearData = () => {
      currentAssignment.value = [];
      proposedAssignment.value = [];
      proposedAssignmentShow.value = [];
    };

    const handleCancel = () => {
      data.value = [];
      emit("closeReplicaReassignDialog", { refresh: false });
    };

    const updateAssignment = () => {
      if (formState.topic == null) {
        message.warn("请先选择Topic！");
        return;
      }
      if (proposedAssignment.value.length == 0) {
        message.warn("请先生成分配计划！");
        return;
      }
      loading.value = true;
      request({
        url: KafkaTopicApi.updateReplicaAssignment.url,
        method: KafkaTopicApi.updateReplicaAssignment.method,
        data: { partitions: proposedAssignment.value },
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          handleTopicChange(formState.topic);
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    return {
      columns,
      show,
      data,
      loading,
      topicList,
      partitions,
      brokers,
      currentAssignment,
      proposedAssignment,
      proposedAssignmentShow,
      formState,
      handleSubmit,
      getTopicReplicaInfo,
      getTopicNameList,
      getPartitionInfo,
      handleTopicChange,
      getClusterInfo,
      getProposedAssignment,
      clearData,
      handleCancel,
      updateAssignment,
    };
  },
});
</script>

<style scoped></style>

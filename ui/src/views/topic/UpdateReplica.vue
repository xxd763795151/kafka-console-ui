<template>
  <a-modal
    title="变更副本"
    :open="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :maskClosable="true"
    @cancel="handleCancel"
    okText="确认"
    cancelText="取消"
    @ok="handleOk"
  >
    <div>
      <a-spin :spinning="loading">
        <div class="replica-box">
          <label>设置副本数：</label>
          <a-input-number
            id="inputNumber"
            v-model:value="replicaNums"
            :min="1"
            :max="brokerSize"
            @change="onChange"
          />
        </div>
        <div class="replica-box">
          <label>是否要限流：</label>
          <a-input-number
            id="inputNumber"
            v-model:value="data.interBrokerThrottle"
            :min="-1"
            :max="102400"
          />
          <strong>
            |说明：broker之间副本同步带宽限制，默认值为-1表示不限制，不是-1表示限制，该值并不表示流速，至于流速配置，在
            <span style="color: red">运维->配置限流</span> 处进行操作.</strong
          >
        </div>
        <a-table
          :columns="columns"
          :data-source="data.partitions"
          bordered
          :rowKey="
            (record, index) => {
              return index;
            }
          "
        >
          <template #bodyCell="{ column, text, record }">
            <template v-if="column.key === 'replicas'">
              <div>
                <span v-for="i in text" :key="i">
                  {{ i }}
                </span>
              </div>
            </template>
          </template>
        </a-table>
        <p>
          *正在进行即尚未完成的副本变更的任务，可以在
          <span style="color: red">运维->副本变更详情</span>
          处查看，也可以在那里将正在进行的任务取消。
        </p>
        <p>
          *如果是减少副本，不用限流。如果是增加副本数，副本同步的时候如果有大量消息需要同步，可能占用大量带宽，担心会影响集群的稳定，考虑是否开启限流。同步完成可以再把该topic的限流关毕。关闭操作可以点击
          限流按钮 处理。
        </p>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from "vue";
import { message, notification } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaClusterApi, KafkaTopicApi } from "@/utils/api";

const columns = [
  {
    title: "Topic",
    dataIndex: "topic",
    key: "topic",
  },
  {
    title: "分区",
    dataIndex: "partition",
    key: "partition",
  },
  {
    title: "副本",
    dataIndex: "replicas",
    key: "replicas",
  },
];

export default defineComponent({
  name: "UpdateReplica",
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
  emits: ["closeUpdateReplicaDialog"],
  setup(props, { emit }) {
    const show = ref(props.open);
    const data = reactive<any>({});
    const loading = ref(false);
    const brokerSize = ref(0);
    const brokerIdList = ref<any[]>([]);
    const replicaNums = ref(0);
    const defaultReplicaNums = ref(0);

    watch(
      () => props.open,
      (v) => {
        show.value = v;
        if (show.value) {
          getClusterInfo();
          getCurrentReplicaAssignment();
        }
      }
    );

    function getCurrentReplicaAssignment() {
      loading.value = true;
      request({
        url:
          KafkaTopicApi.getCurrentReplicaAssignment.url +
          "?topic=" +
          props.topic,
        method: KafkaTopicApi.getCurrentReplicaAssignment.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          Object.assign(data, res.data);
          if (data.partitions && data.partitions.length > 0) {
            replicaNums.value = data.partitions[0].replicas.length;
            defaultReplicaNums.value = replicaNums.value;
          }
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    }

    function getClusterInfo() {
      loading.value = true;
      request({
        url: KafkaClusterApi.getClusterInfo.url,
        method: KafkaClusterApi.getClusterInfo.method,
      }).then((res: any) => {
        brokerSize.value = res.data.nodes.length;
        brokerIdList.value = res.data.nodes.map((o: any) => o.id);
        brokerIdList.value.sort((a, b) => a - b);
      });
    }

    function handleCancel() {
      Object.keys(data).forEach((key) => delete (data as any)[key]);
      emit("closeUpdateReplicaDialog", { refresh: false });
    }

    function onChange(value: number) {
      if (value < 1 || value > brokerSize.value) {
        return false;
      }
      if (data.partitions && data.partitions.length > 0) {
        data.partitions.forEach((p: any) => {
          if (value > p.replicas.length) {
            const index =
              brokerIdList.value.indexOf(p.replicas[p.replicas.length - 1]) + 1;
            const number = Math.min(
              brokerIdList.value.length - p.replicas.length,
              value - p.replicas.length
            );
            for (let i = 0; i < number; i++) {
              p.replicas.push(
                brokerIdList.value[(index + i) % brokerIdList.value.length]
              );
            }
          }
          if (value < p.replicas.length) {
            for (let i = p.replicas.length; i > value; i--) {
              p.replicas.pop();
            }
          }
        });
      }
    }

    function handleOk() {
      loading.value = true;
      request({
        url: KafkaTopicApi.updateReplicaAssignment.url,
        method: KafkaTopicApi.updateReplicaAssignment.method,
        data: data,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("closeUpdateReplicaDialog", { refresh: false });
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    }

    return {
      columns,
      show,
      data,
      loading,
      brokerSize,
      brokerIdList,
      replicaNums,
      defaultReplicaNums,
      getCurrentReplicaAssignment,
      getClusterInfo,
      handleCancel,
      onChange,
      handleOk,
    };
  },
});
</script>

<style scoped>
.replica-box {
  margin-bottom: 1%;
}
</style>

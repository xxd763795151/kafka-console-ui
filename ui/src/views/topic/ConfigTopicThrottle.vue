<template>
  <a-modal
    :title="topic + '限流'"
    :open="show"
    :width="1000"
    :mask="false"
    :maskClosable="false"
    okText="确认"
    cancelText="取消"
    :destroyOnClose="true"
    @cancel="handleCancel"
    @ok="ok"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          ref="formRef"
          :model="formState"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
        >
          <a-form-item
            label="操作"
            name="operation"
            :rules="[{ required: true, message: '请选择一个操作!' }]"
          >
            <a-radio-group
              @change="onChange"
              v-model:value="formState.operation"
            >
              <a-radio value="ON"> 配置限流 </a-radio>
              <a-radio value="OFF"> 移除所有分区限流配置 </a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item
            label="选择分区"
            v-show="showPartition"
            name="partitions"
            :rules="[{ required: true, message: '请选择一个分区!' }]"
          >
            <a-select
              mode="multiple"
              v-model:value="formState.partitions"
              placeholder="请选择一个分区"
              :filter-option="true"
              option-filter-prop="label"
            >
              <a-select-option v-for="v in partitions" :key="v" :value="v" :label="v == -1 ? '全部' : String(v)">
                <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
        <hr />
        <h4>说明：</h4>
        该限流表示topic的副本的在不同broker之间数据同步占用带宽的限制，该配置是一个topic级别的配置项。如未配置速率，即使配置了这个限流也不会进行实际的限流操作。配置速率在
        <span style="color: red">运维->配置限流</span> 处进行操作.
        <h4>如何检查是否对哪些分区启用限流：</h4>
        topic的限流是通过下面这两项配置的：
        <ul>
          <li>leader.replication.throttled.replicas</li>
          <li>follower.replication.throttled.replicas</li>
        </ul>
        只需通过
        <strong>属性配置</strong>
        查看这两项配置的值，格式为："0:0,1:0"，左侧为分区，右侧为broker
        id。示例表示：[分区0的副本：在broker 0上，分区1的副本：在broker 0上]。
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from "vue";
import { message, notification } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";

export default defineComponent({
  name: "ConfigTopicThrottle",
  props: {
    open: {
      type: Boolean,
      default: false,
    },
    topic: {
      type: String,
      default: "",
    },
  },
  emits: ["closeThrottleDialog"],
  setup(props, { emit }) {
    const show = ref(props.open);
    const loading = ref(false);
    const partitions = ref<any[]>([]);
    const showPartition = ref(true);
    const formRef = ref();

    const formState = reactive({
      operation: "ON",
      partitions: [-1],
    });

    watch(
      () => props.open,
      (v) => {
        show.value = v;
        if (show.value) {
          formState.operation = "ON";
          formState.partitions = [-1];
          getPartitionInfo();
          showPartition.value = true;
        }
      }
    );

    function handleCancel() {
      emit("closeThrottleDialog", { refresh: false });
    }

    function getPartitionInfo() {
      loading.value = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + props.topic,
        method: KafkaTopicApi.getPartitionInfo.method,
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          partitions.value = res.data.map((e: any) => e.partition);
          partitions.value.splice(0, 0, -1);
        }
      });
    }

    async function ok() {
      try {
        await formRef.value?.validate();
      } catch {
        return;
      }
      const data = Object.assign({}, formState, { topic: props.topic });
      loading.value = true;
      request({
        url: KafkaTopicApi.configThrottle.url,
        method: KafkaTopicApi.configThrottle.method,
        data: data,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("closeThrottleDialog", { refresh: false });
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    }

    function onChange(e: any) {
      showPartition.value = !(e.target.value == "OFF");
    }

    return {
      show,
      loading,
      partitions,
      showPartition,
      formRef,
      formState,
      handleCancel,
      getPartitionInfo,
      ok,
      onChange,
    };
  },
});
</script>

<style scoped></style>

<template>
  <a-modal
    title="解除限流"
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
            label="Broker"
            name="brokerList"
            :rules="[{ required: true, message: '请选择一个broker!' }]"
          >
            <a-select
              v-model:value="formState.brokerList"
              mode="multiple"
              :filter-option="true"
              option-filter-prop="label"
              placeholder="请选择一个broker"
            >
              <a-select-option
                v-for="v in brokers"
                :key="v"
                :value="v"
                :label="v == -1 ? '全部' : String(v)"
              >
                <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
        <hr />
        <h4>如何检查是否配置的有限流速率：</h4>
        kafka的限流速率是通过下面这两项配置的：
        <ul>
          <li>leader.replication.throttled.rate</li>
          <li>follower.replication.throttled.rate</li>
        </ul>
        只需通过
        <strong>集群->属性配置</strong>
        查看是否存在这两项配置，如果不存在，便是没有配置限流速率。如果未配置限流速率，即使指定某个topic的分区副本进行限流，没有速率也不限流。
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import notification from "ant-design-vue/lib/notification";
import request from "@/utils/request";
import { KafkaClusterApi, KafkaOpApi } from "@/utils/api";

export default defineComponent({
  name: "RemoveThrottle",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const show = ref(props.visible);
    const loading = ref(false);
    const brokers = ref<any[]>([]);
    const formRef = ref();

    const formState = reactive({
      brokerList: [] as any[],
    });

    watch(
      () => props.visible,
      (v) => {
        show.value = v;
        if (show.value) {
          getClusterInfo();
        }
      }
    );

    const handleCancel = () => {
      emit("closeRemoveThrottleDialog", { refresh: false });
    };

    const getClusterInfo = () => {
      loading.value = true;
      request({
        url: KafkaClusterApi.getClusterInfo.url,
        method: KafkaClusterApi.getClusterInfo.method,
      }).then((res: any) => {
        loading.value = false;
        brokers.value = [];
        formState.brokerList = [];
        res.data.nodes.forEach((node: any) => brokers.value.push(node.id));
      });
    };

    const ok = async () => {
      try {
        await formRef.value?.validate();
      } catch {
        return;
      }
      const data = Object.assign({}, formState);
      loading.value = true;
      request({
        url: KafkaOpApi.removeThrottle.url,
        method: KafkaOpApi.removeThrottle.method,
        data: data,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("closeRemoveThrottleDialog", { refresh: false });
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    return {
      show,
      loading,
      brokers,
      formRef,
      formState,
      handleCancel,
      getClusterInfo,
      ok,
    };
  },
});
</script>

<style scoped></style>

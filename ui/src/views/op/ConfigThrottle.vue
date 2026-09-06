<template>
  <a-modal
    title="限流配置"
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
          <a-form-item
            label="带宽"
            name="throttle"
            :rules="[{ required: true, message: '输入带宽!' }]"
          >
            <a-input-number
              v-model:value="formState.throttle"
              :min="1"
              :max="1024"
            />
            <a-select v-model:value="unit" style="width: 100px" :filter-option="true" option-filter-prop="label">
              <a-select-option value="MB" :label="'MB/s'"> MB/s </a-select-option>
              <a-select-option value="KB" :label="'KB/s'"> KB/s </a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
        <hr />
        <div><h4>注意：</h4></div>
        <ul>
          <li>该限速带宽，指的是broker之间副本进行同步时占用的带宽</li>
          <li>该配置是broker级别配置，是针对broker上topic的副本</li>
          <li>
            在当前页面对指定broker限流配置后，并不是说设置后该broker上的所有topic副本同步就被限制为当前流速了。这仅仅是速率设置，如果需要对某topic的副本同步进行限流，还需要去
            Topic->限流 处操作，只有进行限流操作的topic，该限速才会对其生效
          </li>
          <li>
            上面这句话的意思就是，这里只配置topic副本同步的速率，要使这个配置真正在某个topic上生效，还要开启这个topic的限流
          </li>
        </ul>
        <h4>如何检查限流配置是否成功：</h4>
        kafka的限流速率是通过下面这两项配置的：
        <ul>
          <li>leader.replication.throttled.rate</li>
          <li>follower.replication.throttled.rate</li>
        </ul>
        只需通过
        <strong>集群->属性配置</strong>
        查看是否存在这两项配置，如果存在便是配置的有限流，值的大小就是速率，单位：kb/s
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
  name: "ConfigThrottle",
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
    const unit = ref("MB");
    const formRef = ref();

    const formState = reactive({
      brokerList: [] as any[],
      throttle: 1,
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
      emit("closeConfigThrottleDialog", { refresh: false });
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
      const data = Object.assign({}, formState, { unit: unit.value });
      loading.value = true;
      request({
        url: KafkaOpApi.configThrottle.url,
        method: KafkaOpApi.configThrottle.method,
        data: data,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("closeConfigThrottleDialog", { refresh: false });
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
      unit,
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

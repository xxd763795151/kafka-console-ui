<template>
  <a-modal
    title="新增配置"
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
          <a-form-item label="用户" v-show="showUser" name="user">
            <a-input
              v-model:value="formState.user"
              placeholder="输入用户主体标识，比如：用户名，未指定表示用户默认设置"
            />
          </a-form-item>
          <a-form-item label="客户端ID" v-show="showClientId" name="client">
            <a-input
              v-model:value="formState.client"
              placeholder="输入用户客户端ID，未指定表示默认客户端设置"
            />
          </a-form-item>
          <a-form-item label="IP" v-show="showIP" name="ip">
            <a-input v-model:value="formState.ip" placeholder="输入客户端IP" />
          </a-form-item>
          <a-form-item label="生产速率" name="producerRate">
            <a-input-number
              :min="1"
              :max="102400000"
              v-model:value="formState.producerRate"
            />
            <a-select
              v-model:value="producerRateUnit"
              style="width: 100px"
            >
              <a-select-option value="MB"> MB/s</a-select-option>
              <a-select-option value="KB"> KB/s</a-select-option>
              <a-select-option value="Byte"> Byte/s</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="消费速率" name="consumerRate">
            <a-input-number
              :min="1"
              :max="102400000"
              v-model:value="formState.consumerRate"
            />
            <a-select
              v-model:value="consumerRateUnit"
              style="width: 100px"
            >
              <a-select-option value="MB"> MB/s</a-select-option>
              <a-select-option value="KB"> KB/s</a-select-option>
              <a-select-option value="Byte"> Byte/s</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="吞吐量" name="requestPercentage">
            <a-input-number
              :min="1"
              :max="102400000"
              v-model:value="formState.requestPercentage"
            />
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 提交</a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, watch } from "vue";
import { message } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaClientQuotaApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default defineComponent({
  name: "AddQuotaConfig",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    type: {
      type: String,
      default: "",
    },
    showClientId: {
      type: Boolean,
      default: false,
    },
    showUser: {
      type: Boolean,
      default: false,
    },
    showIP: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const show = ref<boolean>(props.visible);
    const loading = ref<boolean>(false);
    const producerRateUnit = ref<string>("MB");
    const consumerRateUnit = ref<string>("MB");
    const formState = reactive<any>({
      user: undefined,
      client: undefined,
      ip: undefined,
      producerRate: undefined,
      consumerRate: undefined,
      requestPercentage: undefined,
    });

    watch(
      () => props.visible,
      (v: boolean) => {
        show.value = v;
      }
    );

    const handleSubmit = () => {
      const values = { ...formState };
      const params: any = Object.assign({ type: props.type }, values);
      const unitMap: any = { MB: 1024 * 1024, KB: 1024, Byte: 1 };
      if (values.consumerRate) {
        params.consumerRate =
          params.consumerRate * unitMap[consumerRateUnit.value];
      }
      if (values.producerRate) {
        params.producerRate =
          params.producerRate * unitMap[producerRateUnit.value];
      }
      params.types = [];
      params.names = [];
      if (props.showUser) {
        params.types.push("user");
        if (params.user) {
          params.names.push(params.user.trim());
        } else {
          params.names.push("");
        }
      }
      if (props.showClientId) {
        params.types.push("client-id");
        if (params.client) {
          params.names.push(params.client.trim());
        } else {
          params.names.push("");
        }
      }
      if (props.showIP) {
        params.types.push("ip");
        if (params.ip) {
          params.names.push(params.ip.trim());
        } else {
          params.names.push("");
        }
      }
      loading.value = true;
      request({
        url: KafkaClientQuotaApi.alterClientQuotaConfigs.url,
        method: KafkaClientQuotaApi.alterClientQuotaConfigs.method,
        data: params,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("closeAddQuotaDialog", { refresh: true });
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };
    const handleCancel = () => {
      formState.user = undefined;
      formState.client = undefined;
      formState.ip = undefined;
      formState.producerRate = undefined;
      formState.consumerRate = undefined;
      formState.requestPercentage = undefined;
      emit("closeAddQuotaDialog", { refresh: false });
      producerRateUnit.value = "MB";
      consumerRateUnit.value = "MB";
    };
    const create = () => {
      producerRateUnit.value = "MB";
      consumerRateUnit.value = "MB";
    };

    return {
      show,
      loading,
      formState,
      producerRateUnit,
      consumerRateUnit,
      handleSubmit,
      handleCancel,
      create,
    };
  },
});
</script>

<style scoped></style>

<template>
  <a-modal
    title="修改配置"
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
              disabled
              v-model:value="formState.user"
              placeholder="输入用户主体标识，比如：用户名，未指定表示用户默认设置"
            />
          </a-form-item>
          <a-form-item label="客户端ID" v-show="showClientId" name="client">
            <a-input
              disabled
              v-model:value="formState.client"
              placeholder="输入用户客户端ID，未指定表示默认客户端设置"
            />
          </a-form-item>
          <a-form-item label="IP" v-show="showIP" name="ip">
            <a-input
              disabled
              v-model:value="formState.ip"
              placeholder="输入客户端IP"
            />
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
  name: "UpdateQuotaConfig",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    type: {
      type: String,
      default: "",
    },
    record: {
      type: Object,
      default: () => {
        return {};
      },
    },
  },
  setup(props, { emit }) {
    const show = ref<boolean>(props.visible);
    const loading = ref<boolean>(false);
    const producerRateUnit = ref<string>("MB");
    const consumerRateUnit = ref<string>("MB");
    const showUser = ref<boolean>(false);
    const showIP = ref<boolean>(false);
    const showClientId = ref<boolean>(false);
    const formState = reactive<any>({
      user: undefined,
      client: undefined,
      ip: undefined,
      producerRate: undefined,
      consumerRate: undefined,
      requestPercentage: undefined,
    });

    const init = () => {
      producerRateUnit.value = "MB";
      if (props.record.producerRate) {
        const parts = props.record.producerRate.split(" ");
        producerRateUnit.value = parts.length > 1 ? parts[1] : "MB";
        formState.producerRate = parts.length > 0 ? Number(parts[0]) : undefined;
      } else {
        formState.producerRate = undefined;
      }
      consumerRateUnit.value = "MB";
      if (props.record.consumerRate) {
        const parts = props.record.consumerRate.split(" ");
        consumerRateUnit.value = parts.length > 1 ? parts[1] : "MB";
        formState.consumerRate = parts.length > 0 ? Number(parts[0]) : undefined;
      } else {
        formState.consumerRate = undefined;
      }
      formState.user = props.record.user;
      formState.client = props.record.client;
      formState.ip = props.record.ip;
      formState.requestPercentage = props.record.requestPercentage;
      if (props.type == "user") {
        showUser.value = true;
      } else if (props.type == "client-id") {
        showClientId.value = true;
      } else if (props.type == "ip") {
        showIP.value = true;
      } else if (props.type == "user&client-id") {
        showUser.value = true;
        showClientId.value = true;
      }
    };

    watch(
      () => props.visible,
      (v: boolean) => {
        show.value = v;
        if (v) {
          init();
        }
      }
    );

    const handleSubmit = () => {
      const values = { ...formState };
      const params: any = { type: props.type, deleteConfigs: [] };
      const unitMap: any = { MB: 1024 * 1024, KB: 1024, Byte: 1 };
      if (values.consumerRate) {
        const num =
          typeof values.consumerRate == "string" &&
          values.consumerRate.indexOf(" ") > 0
            ? (values.consumerRate as string).split(" ")[0]
            : values.consumerRate;
        params.consumerRate = Number(num) * unitMap[consumerRateUnit.value];
      } else {
        params.deleteConfigs.push("consumerRate");
      }
      if (values.producerRate) {
        const num =
          typeof values.producerRate == "string" &&
          values.producerRate.indexOf(" ") > 0
            ? (values.producerRate as string).split(" ")[0]
            : values.producerRate;
        params.producerRate = Number(num) * unitMap[producerRateUnit.value];
      } else {
        params.deleteConfigs.push("producerRate");
      }
      if (values.requestPercentage) {
        params.requestPercentage = values.requestPercentage;
      } else {
        params.deleteConfigs.push("requestPercentage");
      }
      params.types = [];
      params.names = [];
      if (showUser.value) {
        params.types.push("user");
        if (values.user) {
          params.names.push(values.user.trim());
        } else {
          params.names.push("");
        }
      }
      if (showClientId.value) {
        params.types.push("client-id");
        if (values.client) {
          params.names.push(values.client.trim());
        } else {
          params.names.push("");
        }
      }
      if (showIP.value) {
        params.types.push("ip");
        if (values.ip) {
          params.names.push(values.ip.trim());
        } else {
          params.names.push("");
        }
      }
      if (showUser.value && showClientId.value) {
        params.types.push("user");
        params.types.push("client-id");
        if (values.user) {
          params.names.push(values.user.trim());
        } else {
          params.names.push("");
        }
        if (values.client) {
          params.names.push(values.client.trim());
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
          emit("closeUpdateQuotaDialog", { refresh: true });
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };
    const handleCancel = () => {
      emit("closeUpdateQuotaDialog", { refresh: false });
    };

    return {
      show,
      loading,
      formState,
      producerRateUnit,
      consumerRateUnit,
      showUser,
      showIP,
      showClientId,
      handleSubmit,
      handleCancel,
      init,
    };
  },
});
</script>

<style scoped></style>

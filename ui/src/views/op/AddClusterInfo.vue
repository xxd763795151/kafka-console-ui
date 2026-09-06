<template>
  <a-modal
    title="增加集群配置"
    :open="show"
    :width="1000"
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
            label="集群名称"
            name="clusterName"
            :rules="[{ required: true, message: '输入集群名称!' }]"
          >
            <a-input
              v-model:value="formState.clusterName"
              placeholder="输入集群名称"
            />
          </a-form-item>
          <a-form-item
            label="集群地址"
            name="address"
            :rules="[{ required: true, message: '输入集群地址!' }]"
          >
            <a-input
              v-model:value="formState.address"
              placeholder="输入集群地址"
            />
          </a-form-item>
          <a-form-item label="属性" name="properties">
            <a-textarea
              v-model:value="formState.properties"
              rows="5"
              placeholder='可选参数，集群其它属性配置：
request.timeout.ms=10000
security.protocol=SASL_PLAINTEXT
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="name" password="password";
'
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
import { defineComponent, reactive, ref, watch, computed } from "vue";
import { useStore } from "vuex";
import { message } from "ant-design-vue";
import notification from "ant-design-vue/es/notification";
import request from "@/utils/request";
import { KafkaClusterApi } from "@/utils/api";
import { getClusterInfo } from "@/utils/local-cache";
import { CLUSTER } from "@/store/mutation-types";

const defaultInfo = { clusterName: "", address: "", properties: "" };

export default defineComponent({
  name: "AddClusterInfo",
  props: {
    open: {
      type: Boolean,
      default: false,
    },
    visible: {
      type: Boolean,
      default: false,
    },
    isModify: {
      type: Boolean,
      default: false,
    },
    clusterInfo: {
      type: Object,
      default: () => defaultInfo,
    },
    closeDialogEvent: {
      type: String,
      default: "closeAddClusterInfoDialog",
    },
  },
  setup(props, { emit }) {
    const store = useStore();
    const modalOpen = computed(
      () => props.open || props.visible
    );
    const show = ref(modalOpen.value);
    const loading = ref(false);
    const data = ref<any[]>([]);

    const parsePropertiesToText = (properties: any): string => {
      if (properties == null) return "";
      if (Array.isArray(properties)) {
        return properties
          .map((p) => String(p).trim())
          .filter((s) => s.length > 0)
          .join("\n");
      }
      if (typeof properties === "string") {
        const str = properties.trim();
        if (!str) return "";
        try {
          if (str.startsWith("[") && str.endsWith("]")) {
            const parsed = JSON.parse(str);
            if (Array.isArray(parsed)) {
              return parsed
                .map((p) =>
                  String(p).trim().replace(/^["']+|["']+$/g, "")
                )
                .filter((s) => s.length > 0)
                .join("\n");
            }
          }
        } catch (e) {}
        return str
          .split(/\r?\n|,/)
          .map((s) => s.trim().replace(/^["']+|["']+$/g, ""))
          .filter((s) => s.length > 0)
          .join("\n");
      }
      return String(properties);
    };

    const formState = reactive({
      clusterName: props.clusterInfo?.clusterName || "",
      address: props.clusterInfo?.address || "",
      properties: parsePropertiesToText(props.clusterInfo?.properties || ""),
    });

    watch(modalOpen, (v) => {
      show.value = v;
      if (v) {
        formState.clusterName = props.clusterInfo?.clusterName || "";
        formState.address = props.clusterInfo?.address || "";
        formState.properties = parsePropertiesToText(
          props.clusterInfo?.properties || ""
        );
      } else {
        formState.clusterName = "";
        formState.address = "";
        formState.properties = "";
      }
    });

    const switchCluster = (data: any) => {
      store.commit(CLUSTER.SWITCH, data);
    };

    const handleSubmit = (values: any) => {
      loading.value = true;
      const api = props.isModify
        ? KafkaClusterApi.updateClusterInfo
        : KafkaClusterApi.addClusterInfo;
      const submitData = props.isModify
        ? Object.assign({}, props.clusterInfo, values)
        : Object.assign({}, values);
      request({
        url: api.url,
        method: api.method,
        data: submitData,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit(props.closeDialogEvent, { refresh: true });
          if (props.isModify) {
            const clusterInfo = getClusterInfo();
            if (
              clusterInfo &&
              clusterInfo.id &&
              clusterInfo.id == props.clusterInfo?.id
            ) {
              switchCluster(submitData);
            }
          }
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    const handleCancel = () => {
      data.value = [];
      emit(props.closeDialogEvent, { refresh: false });
    };

    return {
      show,
      loading,
      data,
      formState,
      handleSubmit,
      handleCancel,
    };
  },
});
</script>

<style scoped></style>

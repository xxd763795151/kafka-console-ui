<template>
  <a-modal
    title="转发消息"
    :open="show"
    :width="600"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div>
          <h4>选择集群</h4>
          <hr />
          <div class="message-detail" id="message-detail">
            <a-form
              ref="formRef"
              :model="formState"
              :label-col="{ span: 5 }"
              :wrapper-col="{ span: 18 }"
              @finish="handleSubmit"
            >
              <a-form-item
                label="集群"
                name="targetClusterId"
                :rules="[{ required: true, message: '请选择一个集群!' }]"
              >
                <a-select
                  class="select-width"
                  @change="clusterChange"
                  v-model:value="formState.targetClusterId"
                  placeholder="请选择一个集群"
                  :filter-option="true"
                  option-filter-prop="label"
                >
                  <a-select-option
                    v-for="v in clusterList"
                    :key="v.id"
                    :value="v.id"
                    :label="String(v.clusterName)"
                  >
                    {{ v.clusterName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item
                label="Topic"
                name="targetTopic"
                :rules="[{ required: true, message: '请选择一个topic!' }]"
              >
                <a-select
                  class="select-width"
                  show-search
                  :filter-option="true"
                  option-filter-prop="label"
                  v-model:value="formState.targetTopic"
                  placeholder="请选择一个topic"
                >
                  <a-select-option v-for="v in topicList" :key="v" :value="v" :label="String(v)">
                    {{ v }}
                  </a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item
                label="相同分区"
                name="samePartition"
                :rules="[{ required: true, message: '请选择!' }]"
              >
                <a-radio-group v-model:value="formState.samePartition">
                  <a-radio value="false"> 否</a-radio>
                  <a-radio value="true"> 是</a-radio>
                </a-radio-group>
                <span class="mar-left">和原消息保持同一个分区</span>
              </a-form-item>
              <a-form-item>
                <div class="form-footer">
                  <a-button type="primary" html-type="submit"> 提交</a-button>
                </div>
              </a-form-item>
            </a-form>
          </div>
        </div>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, watch, ref } from "vue";
import { message } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaClusterApi, KafkaMessageApi, KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import dayjs from "dayjs";

interface ClusterItem {
  id: number;
  clusterName: string;
  [key: string]: any;
}

interface RecordItem {
  [key: string]: any;
}

interface FormState {
  targetClusterId?: number;
  targetTopic?: string;
  samePartition?: string;
  [key: string]: any;
}

export default defineComponent({
  name: "ForwardMessage",
  props: {
    record: {
      type: Object,
      default: () => ({}),
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const formRef = ref();
    const formState = reactive<FormState>({
      targetClusterId: undefined,
      targetTopic: undefined,
      samePartition: "false",
    });

    const state = reactive({
      show: props.visible,
      data: {} as RecordItem,
      loading: false,
      showForwardDialog: false,
      targetClusterId: -1,
      clusterList: [] as ClusterItem[],
      partition: -1,
      topicList: [] as string[],
    });

    watch(
      () => props.visible,
      (v: boolean) => {
        state.show = v;
        if (state.show) {
          getClusterList();
        }
      }
    );

    const getClusterList = () => {
      state.loading = true;
      request({
        url: KafkaClusterApi.getClusterInfoList.url,
        method: KafkaClusterApi.getClusterInfoList.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          state.clusterList = res.data;
          state.targetClusterId = state.clusterList[0].id;
          formState.targetClusterId = state.clusterList[0].id;
        }
      });
    };

    const handleSubmit = async () => {
      const params = {
        message: Object.assign({}, props.record),
      };
      forward({ ...params, ...formState });
    };

    const handleCancel = () => {
      emit("closeForwardDialog", { refresh: false });
    };

    const formatTime = (time: number) => {
      return time == -1 ? -1 : dayjs(time).format("YYYY-MM-DD HH:mm:ss:SSS");
    };

    const clusterChange = (e: number) => {
      getTopicNameList(e);
    };

    const forward = (params: any) => {
      state.loading = true;
      request({
        url: KafkaMessageApi.forward.url,
        method: KafkaMessageApi.forward.method,
        data: params,
      }).then((res: any) => {
        state.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          message.success(res.msg);
        }
      });
    };

    const openForwardDialog = () => {
      state.showForwardDialog = true;
    };

    const closeForwardDialog = () => {
      state.showForwardDialog = false;
    };

    const getTopicNameList = (clusterInfoId: number) => {
      state.loading = true;
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
        headers: {
          "X-Specific-Cluster-Info-Id": clusterInfoId,
        },
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          state.topicList = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    return {
      ...toRefs(state),
      formRef,
      formState,
      getClusterList,
      handleSubmit,
      handleCancel,
      formatTime,
      clusterChange,
      forward,
      openForwardDialog,
      closeForwardDialog,
      getTopicNameList,
    };
  },
});
</script>

<style scoped>
.m-info {
}

.title {
  width: 15%;
  display: inline-block;
  text-align: right;
  margin-right: 2%;
  font-weight: bold;
}

.ant-spin-container #message-detail textarea {
  max-width: 80% !important;
  vertical-align: top !important;
}

.center {
  text-align: center;
}

.mar-left {
  margin-left: 1%;
}

.select-width {
  width: 80%;
}

.form-footer {
  text-align: center;
  margin-top: 3%;
}
</style>

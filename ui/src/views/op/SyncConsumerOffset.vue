<template>
  <a-modal
    title="同步消费位点"
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
            label="消费组"
            name="groupId"
            :rules="[{ required: true, message: '请选择一个消费组!' }]"
          >
            <a-select
              v-model:value="formState.groupId"
              @change="handleGroupChange"
              show-search
              :filter-option="true"
              option-filter-prop="label"
              placeholder="请选择一个消费组"
            >
              <a-select-option v-for="v in groupIdList" :key="v" :value="v" :label="String(v)">
                {{ v }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item
            label="topic"
            name="topic"
            :rules="[{ required: true, message: '请选择一个topic!' }]"
          >
            <a-select
              v-model:value="formState.topic"
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
            label="kafka地址"
            name="address"
            :rules="[{ required: true, message: '输入待同步kafka地址!' }]"
          >
            <a-input
              v-model:value="formState.address"
              placeholder="输入待同步kafka地址"
            />
          </a-form-item>
          <a-form-item label="kafka属性" name="properties">
            <a-textarea
              v-model:value="formState.properties"
              rows="5"
              placeholder="除了地址，其它kafka属性配置，如：
request.timeout.ms=6000"
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
import { defineComponent, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import notification from "ant-design-vue/es/notification";
import request from "@/utils/request";
import { KafkaConsumerApi, KafkaOpApi } from "@/utils/api";

export default defineComponent({
  name: "SyncConsumerOffset",
  props: {
    topic: {
      type: String,
      default: "",
    },
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
    const groupIdList = ref<any[]>([]);

    const formState = reactive({
      groupId: undefined as any,
      topic: undefined as any,
      address: "",
      properties: undefined as any,
    });

    watch(
      () => props.visible,
      (v) => {
        show.value = v;
        if (show.value) {
          getGroupIdList();
        }
      }
    );

    const getGroupIdList = () => {
      request({
        url: KafkaConsumerApi.getGroupIdList.url,
        method: KafkaConsumerApi.getGroupIdList.method,
      }).then((res: any) => {
        if (res.code == 0) {
          groupIdList.value = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    const handleSubmit = (values: any) => {
      if (values.properties) {
        const properties: any = {};
        values.properties.split("\n").forEach((e: string) => {
          const c = e.split("=");
          if (c.length > 1) {
            let k = c[0].trim(),
              v = c[1].trim();
            for (let j = 2; j < c.length; j++) {
              v += "=" + c[j];
            }
            if (k && v) {
              properties[k] = v;
            }
          }
        });
        values.properties = properties;
      } else {
        values.properties = {};
      }
      loading.value = true;
      request({
        url: KafkaOpApi.syncConsumerOffset.url,
        method: KafkaOpApi.syncConsumerOffset.method,
        data: values,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit("closeSyncConsumerOffsetDialog", { refresh: true });
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    const handleCancel = () => {
      groupIdList.value = [];
      topicList.value = [];
      emit("closeSyncConsumerOffsetDialog", { refresh: false });
    };

    const handleGroupChange = (groupId: string) => {
      loading.value = true;
      request({
        url: KafkaConsumerApi.getSubscribeTopicList.url + "?groupId=" + groupId,
        method: KafkaConsumerApi.getSubscribeTopicList.method,
      }).then((res: any) => {
        loading.value = false;
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

    return {
      show,
      data,
      loading,
      topicList,
      groupIdList,
      formState,
      getGroupIdList,
      handleSubmit,
      handleCancel,
      handleGroupChange,
    };
  },
});
</script>

<style scoped></style>

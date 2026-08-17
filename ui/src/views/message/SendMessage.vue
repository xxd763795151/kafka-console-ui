<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-form
        ref="formRef"
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
            class="topic-select"
            @change="handleTopicChange"
            show-search
            :filter-option="true"
            option-filter-prop="label"
            v-model:value="formState.topic"
            placeholder="请选择一个topic"
          >
            <a-select-option v-for="v in topicList" :key="v" :value="v" :label="String(v)">
              {{ v }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="分区">
          <a-select
            class="type-select"
            show-search
            :filter-option="true"
            option-filter-prop="label"
            v-model:value="selectPartition"
            placeholder="请选择一个分区"
          >
            <a-select-option v-for="v in partitions" :key="v" :value="v">
              <span v-if="v == -1">默认</span> <span v-else>{{ v }}</span>
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="消息头">
          <table>
            <tbody>
              <tr v-for="(row, index) in rows" :key="index">
                <td class="w-30">
                  <a-input v-model:value="row.headerKey" placeholder="key" />
                </td>
                <td class="w-60">
                  <a-input v-model:value="row.headerValue" placeholder="value" />
                </td>
                <td>
                  <a-button
                    type="primary"
                    danger
                    @click="deleteRow(index)"
                    v-show="rows.length > 1"
                    >删除</a-button
                  >
                </td>
                <td>
                  <a-button
                    type="primary"
                    @click="addRow"
                    v-show="index == rows.length - 1"
                    >添加</a-button
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </a-form-item>
        <a-form-item label="消息Key" name="key">
          <a-input v-model:value="formState.key" />
        </a-form-item>
        <a-form-item
          label="消息体"
          name="body"
          has-feedback
          :rules="[
            {
              required: true,
              message: '输入消息体!',
            },
          ]"
        >
          <a-textarea
            :autosize="{ minRows: 5 }"
            v-model:value="formState.body"
            placeholder="输入消息体!"
          />
        </a-form-item>
        <a-form-item
          label="发送的消息数"
          name="num"
          :rules="[
            {
              required: true,
              message: '输入消息数!',
            },
          ]"
        >
          <a-input-number
            v-model:value="formState.num"
            :min="1"
            :max="32"
          />
        </a-form-item>
        <a-form-item
          label="发送类型"
          name="sync"
          :rules="[{ required: true, message: '请选择一个发送类型!' }]"
        >
          <a-radio-group v-model:value="formState.sync">
            <a-radio value="false"> 异步发送 </a-radio>
            <a-radio value="true">
              同步发送（发送失败，会返回错误信息）
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
          <a-button type="primary" html-type="submit"> 提交 </a-button>
        </a-form-item>
      </a-form>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import request from "@/utils/request";
import { KafkaTopicApi, KafkaMessageApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

interface HeaderRow {
  headerKey: string;
  headerValue: string;
  [key: string]: any;
}

interface FormState {
  topic?: string;
  key?: string;
  body?: string;
  num?: number;
  sync?: string;
  [key: string]: any;
}

export default defineComponent({
  name: "SendMessage",
  components: {},
  props: {
    topicList: {
      type: Array,
      default: () => [],
    },
  },
  setup() {
    const formRef = ref();
    const formState = reactive<FormState>({
      topic: undefined,
      key: "key",
      body: undefined,
      num: 1,
      sync: "false",
    });

    const state = reactive({
      loading: false,
      partitions: [] as number[],
      selectPartition: undefined as number | undefined,
      rows: [{ headerKey: "", headerValue: "" }] as HeaderRow[],
    });

    const getTopicNameList = () => {
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
      }).then((res: any) => {
        if (res.code == 0) {
          state.rows = [{ headerKey: "", headerValue: "" }];
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    const getPartitionInfo = (topic: string) => {
      state.loading = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + topic,
        method: KafkaTopicApi.getPartitionInfo.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          state.partitions = res.data.map((v: any) => v.partition);
          state.partitions.splice(0, 0, -1);
        }
      });
    };

    const handleTopicChange = (topic: string) => {
      state.selectPartition = -1;
      getPartitionInfo(topic);
    };

    const addRow = () => {
      if (state.rows.length < 32) {
        state.rows.push({ headerKey: "", headerValue: "" });
      }
    };

    const deleteRow = (index: number) => {
      state.rows.splice(index, 1);
    };

    const handleSubmit = async () => {
      const param = Object.assign({}, formState, {
        partition: state.selectPartition,
        headers: state.rows,
      });
      state.loading = true;
      request({
        url: KafkaMessageApi.send.url,
        method: KafkaMessageApi.send.method,
        data: param,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          message.success(res.msg);
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };

    onMounted(() => {
      getTopicNameList();
    });

    return {
      ...toRefs(state),
      formRef,
      formState,
      getTopicNameList,
      getPartitionInfo,
      handleTopicChange,
      addRow,
      deleteRow,
      handleSubmit,
    };
  },
});
</script>
<style scoped>
.w-30 {
  width: 300px;
}
.w-60 {
  width: 500px;
}
</style>

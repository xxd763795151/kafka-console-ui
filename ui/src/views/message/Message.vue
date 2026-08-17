<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-tabs default-active-key="1" size="large" tabPosition="top">
        <a-tab-pane
          v-if="isAuthorized('message:search-time')"
          key="1"
          tab="根据时间查询消息"
        >
          <SearchByTime :topic-list="topicList"></SearchByTime>
        </a-tab-pane>
        <a-tab-pane
          key="2"
          v-if="isAuthorized('message:search-offset')"
          tab="根据偏移查询消息"
        >
          <SearchByOffset :topic-list="topicList"></SearchByOffset>
        </a-tab-pane>
        <a-tab-pane key="3" tab="在线发送" v-if="isAuthorized('message:send')">
          <SendMessage :topic-list="topicList"></SendMessage>
        </a-tab-pane>

        <a-tab-pane key="4" tab="在线删除" v-if="isAuthorized('message:del')">
          <DeleteMessage :topic-list="topicList"></DeleteMessage>
        </a-tab-pane>

        <a-tab-pane
          key="5"
          tab="发送统计"
          v-if="isAuthorized('message:send-statistics')"
        >
          <SendStatistics :topic-list="topicList"></SendStatistics>
        </a-tab-pane>
      </a-tabs>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, onMounted } from "vue";
import SearchByTime from "@/views/message/SearchByTime.vue";
import SearchByOffset from "@/views/message/SearchByOffset.vue";
import SendStatistics from "@/views/message/SendStatistics.vue";
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import SendMessage from "@/views/message/SendMessage.vue";
import DeleteMessage from "./DeleteMessage.vue";
import { isAuthorized, isUnauthorized } from "@/utils/auth";

interface TopicItem {
  [key: string]: any;
}

export default defineComponent({
  name: "Message",
  components: {
    DeleteMessage,
    SearchByTime,
    SearchByOffset,
    SendMessage,
    SendStatistics,
  },
  setup() {
    const state = reactive({
      loading: false,
      topicList: [] as TopicItem[],
    });

    const getTopicNameList = () => {
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
      }).then((res: any) => {
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

    onMounted(() => {
      getTopicNameList();
    });

    return {
      ...toRefs(state),
      isAuthorized,
      isUnauthorized,
      getTopicNameList,
    };
  },
});
</script>

<style scoped></style>

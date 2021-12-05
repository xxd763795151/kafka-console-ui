<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-tabs default-active-key="1" size="large" tabPosition="top">
        <a-tab-pane key="1" tab="根据时间查询消息">
          <SearchByTime :topic-list="topicList"></SearchByTime>
        </a-tab-pane>
        <a-tab-pane key="2" tab="根据位移查询消息" force-render>
          根据位移查询消息
        </a-tab-pane>
        <a-tab-pane key="3" tab="消息发送"> 消息发送1 </a-tab-pane>
      </a-tabs>
    </a-spin>
  </div>
</template>

<script>
import SearchByTime from "@/views/message/SearchByTime";
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
export default {
  name: "Message",
  components: { SearchByTime },
  data() {
    return {
      loading: false,
      topicList: [],
    };
  },
  methods: {
    getTopicNameList() {
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
      }).then((res) => {
        if (res.code == 0) {
          this.topicList = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
  },
  created() {
    this.getTopicNameList();
  },
};
</script>

<style scoped></style>

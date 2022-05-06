<template>
  <div>
    <Header/>
    <div class="content">
      <a-spin :spinning="loading">
        <a-tabs default-active-key="1" size="large" tabPosition="top">
          <a-tab-pane key="1" tab="根据时间查询消息">
            <SearchByTime :topic-list="topicList"></SearchByTime>
          </a-tab-pane>
          <a-tab-pane key="2" tab="根据偏移查询消息">
            <SearchByOffset :topic-list="topicList"></SearchByOffset>
          </a-tab-pane>
          <a-tab-pane key="3" tab="在线发送">
            <SendMessage :topic-list="topicList"></SendMessage>
          </a-tab-pane>
        </a-tabs>
      </a-spin>
    </div>
  </div>
</template>

<script>
import SearchByTime from "@/views/message/SearchByTime";
import SearchByOffset from "@/views/message/SearchByOffset";
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import SendMessage from "@/views/message/SendMessage";
import Header from "@/components/Header"
export default {
  name: "Message",
  components: { SearchByTime, SearchByOffset, SendMessage, Header },
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

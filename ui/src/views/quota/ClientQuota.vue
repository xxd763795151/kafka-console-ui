<template>
  <div class="content">
    <a-spin :spinning="loading">
      <a-tabs default-active-key="1" size="large" tabPosition="top">
        <a-tab-pane key="1" tab="客户端ID">
        </a-tab-pane>
        <a-tab-pane key="2" tab="用户">
        </a-tab-pane>
        <a-tab-pane key="3" tab="客户端ID和用户">
        </a-tab-pane>
        <a-tab-pane key="4" tab="IP">
        </a-tab-pane>
        <a-tab-pane key="5" tab="使用说明">
        </a-tab-pane>
      </a-tabs>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import {KafkaTopicApi} from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

export default {
  name: "ClientQuota",
  components: {},
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

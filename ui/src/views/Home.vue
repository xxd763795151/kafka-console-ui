<template>
  <div class="home">
    <a-card title="kafka console 配置" style="width: 100%">
      <!--      <a slot="extra" href="#">more</a>-->
      <p v-for="(v, k) in config" :key="k">{{ k }}={{ v }}</p>
    </a-card>
  </div>
</template>

<script>
// @ is an alias to /src
import request from "@/utils/request";
import { KafkaConfigApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
export default {
  name: "Home",
  components: {},
  data() {
    return {
      config: {},
    };
  },

  created() {
    request({
      url: KafkaConfigApi.getConfig.url,
      method: KafkaConfigApi.getConfig.method,
    }).then((res) => {
      if (res.code == 0) {
        this.config = res.data;
      } else {
        notification.error({
          message: "error",
          description: res.msg,
        });
      }
    });
  },
};
</script>

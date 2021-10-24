<template>
  <div class="content">
    <div class="content-module">
      <a-card title="数据同步" style="width: 100%; text-align: left">
        <p>
          <a-button type="primary" @click="openSyncConsumerOffsetDialog">
            同步消费位点
          </a-button>
          <label>说明：</label>
          <span
            >同步其它集群中指定消费组与订阅的topic的消费位点到当前集群上，该消费组在当前集群已存在，且双方订阅的topic分区信息一致</span
          >
        </p>
      </a-card>
    </div>
    <div class="content-module">
      <a-card title="副本管理" style="width: 100%; text-align: left">
        <p>
          <a-button type="primary"> 首选副本作为leader </a-button>
          <label>说明：</label>
          <span>将集群中所有分区leader副本设置为首选副本</span>
        </p>
      </a-card>
    </div>
    <SyncConsumerOffset
      :visible="syncData.showSyncConsumerOffsetDialog"
      @closeSyncConsumerOffsetDialog="closeSyncConsumerOffsetDialog"
    >
    </SyncConsumerOffset>
  </div>
</template>

<script>
import SyncConsumerOffset from "@/views/op/SyncConsumerOffset";
export default {
  name: "Operation",
  components: { SyncConsumerOffset },
  data() {
    return {
      syncData: {
        showSyncConsumerOffsetDialog: false,
      },
    };
  },
  methods: {
    openSyncConsumerOffsetDialog() {
      this.syncData.showSyncConsumerOffsetDialog = true;
    },
    closeSyncConsumerOffsetDialog() {
      this.syncData.showSyncConsumerOffsetDialog = false;
    },
  },
};
</script>

<style scoped>
.content-module {
  margin-bottom: 1%;
}
.content-module button {
  margin-right: 1%;
}
</style>

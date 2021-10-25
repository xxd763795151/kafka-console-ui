<template>
  <div class="content">
    <div class="content-module">
      <a-card title="副本管理" style="width: 100%; text-align: left">
        <p>
          <a-button type="primary"> 首选副本作为leader </a-button>
          <label>说明：</label>
          <span>将集群中所有分区leader副本设置为首选副本</span>
        </p>
      </a-card>
    </div>
    <div class="content-module">
      <a-card title="数据同步" style="width: 100%; text-align: left">
        <!--        <p>-->
        <!--          <a-button type="primary" @click="openSyncConsumerOffsetDialog">-->
        <!--            数据同步方案-->
        <!--          </a-button>-->
        <!--          <label>说明：</label>-->
        <!--          <span-->
        <!--          >数据同步方案</span-->
        <!--          >-->
        <!--        </p>-->
        <p>
          <a-button type="primary" @click="openMinOffsetAlignmentDialog">
            最小位移对齐
          </a-button>
          <label>说明：</label>
          <span
            >同步消费位点时需要获取两端集群中订阅分区的最小位移进行消费位点计算，如需后面同步消费位点，在进行数据同步前，先进行最小位移对齐</span
          >
        </p>
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
    <SyncConsumerOffset
      :visible="syncData.showSyncConsumerOffsetDialog"
      @closeSyncConsumerOffsetDialog="closeSyncConsumerOffsetDialog"
    >
    </SyncConsumerOffset>
    <MinOffsetAlignment
      :visible="syncData.showMinOffsetAlignmentDialog"
      @closeMinOffsetAlignmentDialog="closeMinOffsetAlignmentDialog"
    >
    </MinOffsetAlignment>
  </div>
</template>

<script>
import SyncConsumerOffset from "@/views/op/SyncConsumerOffset";
import MinOffsetAlignment from "@/views/op/MinOffsetAlignment";
export default {
  name: "Operation",
  components: { SyncConsumerOffset, MinOffsetAlignment },
  data() {
    return {
      syncData: {
        showSyncConsumerOffsetDialog: false,
        showMinOffsetAlignmentDialog: false,
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
    openMinOffsetAlignmentDialog() {
      this.syncData.showMinOffsetAlignmentDialog = true;
    },
    closeMinOffsetAlignmentDialog() {
      this.syncData.showMinOffsetAlignmentDialog = false;
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

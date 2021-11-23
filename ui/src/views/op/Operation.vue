<template>
  <div class="content">
    <div class="content-module">
      <a-card title="Broker管理" style="width: 100%; text-align: left">
        <p>
          <a-button type="primary"> 配置限流 </a-button>
          <label>说明：</label>
          <span
            >设置指定broker上的topic的副本之间数据同步占用的带宽，这个设置是broker级别的，但是设置后还要去对应的topic上进行限流配置，指定对这个topic的相关副本进行限制</span
          >
        </p>
        <p>
          <a-button type="primary"> 解除限流 </a-button>
          <label>说明：</label>
          <span>解除指定broker上的topic副本之间数据同步占用的带宽限制</span>
        </p>
      </a-card>
    </div>
    <div class="content-module">
      <a-card title="副本管理" style="width: 100%; text-align: left">
        <p>
          <a-button type="primary" @click="openElectPreferredLeaderDialog">
            首选副本作为leader
          </a-button>
          <label>说明：</label>
          <span>将集群中所有分区leader副本设置为首选副本</span>
        </p>
        <p>
          <a-button type="primary"> 副本变更详情 </a-button>
          <label>说明：</label>
          <span>查看正在进行副本变更/重分配的任务，或者将其取消</span>
        </p>
      </a-card>
    </div>
    <div class="content-module">
      <a-card title="数据同步" style="width: 100%; text-align: left">
        <p v-show="true">
          <a-button type="primary" @click="openDataSyncSchemeDialog">
            数据同步方案
          </a-button>
          <label>说明：</label>
          <span>新老集群迁移、数据同步解决方案</span>
        </p>
        <p>
          <a-button type="primary" @click="openMinOffsetAlignmentDialog">
            最小位移对齐
          </a-button>
          <label>说明：</label>
          <span
            >同步消费位点时需要获取两端集群中订阅分区的最小位移进行消费位点计算，如需后面同步消费位点，在进行数据同步前，先进行最小位移对齐，
            点击右侧查看：</span
          ><a href="javascript:;" @click="openOffsetAlignmentInfoDialog"
            >对齐信息</a
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
    <OffsetAlignmentTable
      :visible="syncData.showOffsetAlignmentInfoDialog"
      @closeOffsetAlignmentInfoDialog="closeOffsetAlignmentInfoDialog"
    ></OffsetAlignmentTable>
    <ElectPreferredLeader
      :visible="replicationManager.showElectPreferredLeaderDialog"
      @closeElectPreferredLeaderDialog="closeElectPreferredLeaderDialog"
    ></ElectPreferredLeader>
    <DataSyncScheme
      :visible="syncData.showDataSyncSchemeDialog"
      @closeDataSyncSchemeDialog="closeDataSyncSchemeDialog"
    >
    </DataSyncScheme>
  </div>
</template>

<script>
import SyncConsumerOffset from "@/views/op/SyncConsumerOffset";
import MinOffsetAlignment from "@/views/op/MinOffsetAlignment";
import OffsetAlignmentTable from "@/views/op/OffsetAlignmentTable";
import ElectPreferredLeader from "@/views/op/ElectPreferredLeader";
import DataSyncScheme from "@/views/op/DataSyncScheme";
export default {
  name: "Operation",
  components: {
    SyncConsumerOffset,
    MinOffsetAlignment,
    OffsetAlignmentTable,
    ElectPreferredLeader,
    DataSyncScheme,
  },
  data() {
    return {
      syncData: {
        showSyncConsumerOffsetDialog: false,
        showMinOffsetAlignmentDialog: false,
        showOffsetAlignmentInfoDialog: false,
        showDataSyncSchemeDialog: false,
      },
      replicationManager: {
        showElectPreferredLeaderDialog: false,
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
    openOffsetAlignmentInfoDialog() {
      this.syncData.showOffsetAlignmentInfoDialog = true;
    },
    closeOffsetAlignmentInfoDialog() {
      this.syncData.showOffsetAlignmentInfoDialog = false;
    },
    openDataSyncSchemeDialog() {
      this.syncData.showDataSyncSchemeDialog = true;
    },
    closeDataSyncSchemeDialog() {
      this.syncData.showDataSyncSchemeDialog = false;
    },
    openElectPreferredLeaderDialog() {
      this.replicationManager.showElectPreferredLeaderDialog = true;
    },
    closeElectPreferredLeaderDialog() {
      this.replicationManager.showElectPreferredLeaderDialog = false;
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

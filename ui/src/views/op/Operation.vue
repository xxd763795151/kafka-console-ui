<template>
  <div class="content">
    <a-spin :spinning="loading">
      <div class="content-module">
        <a-card title="集群管理" style="width: 100%; text-align: left">
          <p v-action:op:cluster-switch>
            <a-button type="primary" @click="openClusterInfoDialog">
              集群切换
            </a-button>
            <label>说明：</label>
            <span
              >多集群管理：增加、删除集群配置，切换选中集群为当前操作集群。</span
            >
          </p>
        </a-card>
      </div>
      <div class="content-module">
        <a-card title="Broker管理" style="width: 100%; text-align: left">
          <p v-action:op:config-throttle>
            <a-button type="primary" @click="openConfigThrottleDialog">
              配置限流
            </a-button>
            <label>说明：</label>
            <span
              >设置指定broker上的topic的副本之间数据同步占用的带宽，这个设置是broker级别的，但是设置后还要去对应的topic上进行限流配置，指定对这个topic的相关副本进行限制</span
            >
          </p>
          <p v-action:op:remove-throttle>
            <a-button type="primary" @click="openRemoveThrottleDialog">
              解除限流
            </a-button>
            <label>说明：</label>
            <span>解除指定broker上的topic副本之间数据同步占用的带宽限制</span>
          </p>
        </a-card>
      </div>
      <div class="content-module">
        <a-card title="副本管理" style="width: 100%; text-align: left">
          <p v-action:op:replication-preferred>
            <a-button type="primary" @click="openElectPreferredLeaderDialog">
              首选副本作为leader
            </a-button>
            <label>说明：</label>
            <span>将集群中所有分区leader副本设置为首选副本</span>
          </p>
          <p v-action:op:replication-update-detail>
            <a-button type="primary" @click="openCurrentReassignmentsDialog">
              副本变更详情
            </a-button>
            <label>说明：</label>
            <span>查看正在进行副本变更/重分配的任务，或者将其取消</span>
          </p>
          <p v-action:op:replication-reassign>
            <a-button type="primary" @click="openReplicaReassignDialog">
              副本重分配
            </a-button>
            <label>说明：</label>
            <span
              >副本所在节点重新分配，打个比方，集群有6个节点，分区1的3个副本在节点1、2、3上，现在将它们重新分配到3、4、5上</span
            >
          </p>
        </a-card>
      </div>
      <div class="content-module" v-show="false">
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
      <div class="content-module">
        <a-card title="控制台数据" style="width: 100%; text-align: left">
          <p v-action:op:console-import>
            <a-button type="primary" @click="handleImport"> 导入 </a-button>
            <label>说明：</label>
            <span>将其它控制台数据导入当前控制台内</span>
          </p>
          <p v-action:op:console-export>
            <a-button type="primary" @click="handleExport"> 导出 </a-button>
            <label>说明：</label>
            <span>将当前控制台的数据作为文本导出</span>
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
      <ConfigThrottle
        :visible="brokerManager.showConfigThrottleDialog"
        @closeConfigThrottleDialog="closeConfigThrottleDialog"
      >
      </ConfigThrottle>
      <RemoveThrottle
        :visible="brokerManager.showRemoveThrottleDialog"
        @closeRemoveThrottleDialog="closeRemoveThrottleDialog"
      >
      </RemoveThrottle>
      <CurrentReassignments
        :visible="replicationManager.showCurrentReassignmentsDialog"
        @closeCurrentReassignmentsDialog="closeCurrentReassignmentsDialog"
      ></CurrentReassignments>
      <ClusterInfo
        :visible="clusterManager.showClusterInfoDialog"
        @closeClusterInfoDialog="closeClusterInfoDialog"
      ></ClusterInfo>
      <ReplicaReassign
        :visible="replicationManager.showReplicaReassignDialog"
        @closeReplicaReassignDialog="closeReplicaReassignDialog"
      >
      </ReplicaReassign>
      <input
        type="file"
        ref="fileInput"
        accept=".json"
        style="display: none"
        @change="handleFileChange"
      />
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import notification from "ant-design-vue/lib/notification";
import SyncConsumerOffset from "@/views/op/SyncConsumerOffset.vue";
import MinOffsetAlignment from "@/views/op/MinOffsetAlignment.vue";
import OffsetAlignmentTable from "@/views/op/OffsetAlignmentTable.vue";
import ElectPreferredLeader from "@/views/op/ElectPreferredLeader.vue";
import DataSyncScheme from "@/views/op/DataSyncScheme.vue";
import ConfigThrottle from "@/views/op/ConfigThrottle.vue";
import RemoveThrottle from "@/views/op/RemoveThrottle.vue";
import CurrentReassignments from "@/views/op/CurrentReassignments.vue";
import ClusterInfo from "@/views/op/ClusterInfo.vue";
import ReplicaReassign from "@/views/op/ReplicaReassign.vue";
import request from "@/utils/request";
import { KafkaOpApi } from "@/utils/api";

export default defineComponent({
  name: "Operation",
  components: {
    SyncConsumerOffset,
    MinOffsetAlignment,
    OffsetAlignmentTable,
    ElectPreferredLeader,
    DataSyncScheme,
    ConfigThrottle,
    RemoveThrottle,
    CurrentReassignments,
    ClusterInfo,
    ReplicaReassign,
  },
  setup() {
    const syncData = reactive({
      showSyncConsumerOffsetDialog: false,
      showMinOffsetAlignmentDialog: false,
      showOffsetAlignmentInfoDialog: false,
      showDataSyncSchemeDialog: false,
    });

    const replicationManager = reactive({
      showElectPreferredLeaderDialog: false,
      showCurrentReassignmentsDialog: false,
      showReplicaReassignDialog: false,
    });

    const brokerManager = reactive({
      showConfigThrottleDialog: false,
      showRemoveThrottleDialog: false,
    });

    const clusterManager = reactive({
      showClusterInfoDialog: false,
    });

    const loading = ref(false);
    const fileInput = ref<HTMLInputElement | null>(null);

    const openSyncConsumerOffsetDialog = () => {
      syncData.showSyncConsumerOffsetDialog = true;
    };
    const closeSyncConsumerOffsetDialog = () => {
      syncData.showSyncConsumerOffsetDialog = false;
    };
    const openMinOffsetAlignmentDialog = () => {
      syncData.showMinOffsetAlignmentDialog = true;
    };
    const closeMinOffsetAlignmentDialog = () => {
      syncData.showMinOffsetAlignmentDialog = false;
    };
    const openOffsetAlignmentInfoDialog = () => {
      syncData.showOffsetAlignmentInfoDialog = true;
    };
    const closeOffsetAlignmentInfoDialog = () => {
      syncData.showOffsetAlignmentInfoDialog = false;
    };
    const openDataSyncSchemeDialog = () => {
      syncData.showDataSyncSchemeDialog = true;
    };
    const closeDataSyncSchemeDialog = () => {
      syncData.showDataSyncSchemeDialog = false;
    };
    const openElectPreferredLeaderDialog = () => {
      replicationManager.showElectPreferredLeaderDialog = true;
    };
    const closeElectPreferredLeaderDialog = () => {
      replicationManager.showElectPreferredLeaderDialog = false;
    };
    const openConfigThrottleDialog = () => {
      brokerManager.showConfigThrottleDialog = true;
    };
    const closeConfigThrottleDialog = () => {
      brokerManager.showConfigThrottleDialog = false;
    };
    const openRemoveThrottleDialog = () => {
      brokerManager.showRemoveThrottleDialog = true;
    };
    const closeRemoveThrottleDialog = () => {
      brokerManager.showRemoveThrottleDialog = false;
    };
    const openCurrentReassignmentsDialog = () => {
      replicationManager.showCurrentReassignmentsDialog = true;
    };
    const closeCurrentReassignmentsDialog = () => {
      replicationManager.showCurrentReassignmentsDialog = false;
    };
    const openClusterInfoDialog = () => {
      clusterManager.showClusterInfoDialog = true;
    };
    const closeClusterInfoDialog = () => {
      clusterManager.showClusterInfoDialog = false;
    };
    const openReplicaReassignDialog = () => {
      replicationManager.showReplicaReassignDialog = true;
    };
    const closeReplicaReassignDialog = () => {
      replicationManager.showReplicaReassignDialog = false;
    };

    const handleExport = () => {
      try {
        loading.value = true;

        request({
          url: KafkaOpApi.consoleExport.url,
          method: KafkaOpApi.consoleExport.method,
          responseType: "blob",
        }).then((response: any) => {
          loading.value = false;
          const blob = new Blob([response.data], { type: "application/json" });
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement("a");
          link.href = url;

          const headers = response.headers || {};
          const contentDisposition = headers["content-disposition"];
          let fileName = "console_data.json";
          if (contentDisposition) {
            const fileNameMatch = contentDisposition.match(/filename="?(.+)"?/);
            if (fileNameMatch && fileNameMatch.length === 2) {
              fileName = fileNameMatch[1];
            }
          }

          link.download = fileName;
          document.body.appendChild(link);
          link.click();

          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);

          message.success({ content: "数据导出成功", key: "export" });
        });
      } catch (error) {
        message.error({ content: "数据导出失败", key: "export" });
      }
    };

    const handleImport = () => {
      fileInput.value?.click();
    };

    const handleFileChange = (event: Event) => {
      const target = event.target as HTMLInputElement;
      const file = target.files?.[0];
      if (!file) return;

      if (!file.name.toLowerCase().endsWith(".json")) {
        message.error("请选择JSON文件");
        return;
      }

      try {
        loading.value = true;

        const formData = new FormData();
        formData.append("file", file);
        formData.append("overwriteExisting", "true");
        formData.append("importType", "ALL");

        request({
          url: KafkaOpApi.consoleImport.url,
          method: KafkaOpApi.consoleImport.method,
          headers: {
            "Content-Type": "multipart/form-data",
          },
          data: formData,
        }).then((response: any) => {
          loading.value = false;
          if (response.code == 0) {
            message.success(response.msg);
          } else {
            notification.error({
              message: "error",
              description: `导入失败：${response.msg}`,
            });
          }
        });
      } catch (error) {
        message.error({
          content: "文件导入失败，请检查文件格式",
          key: "import",
        });
      } finally {
        target.value = "";
      }
    };

    return {
      syncData,
      replicationManager,
      brokerManager,
      clusterManager,
      loading,
      fileInput,
      openSyncConsumerOffsetDialog,
      closeSyncConsumerOffsetDialog,
      openMinOffsetAlignmentDialog,
      closeMinOffsetAlignmentDialog,
      openOffsetAlignmentInfoDialog,
      closeOffsetAlignmentInfoDialog,
      openDataSyncSchemeDialog,
      closeDataSyncSchemeDialog,
      openElectPreferredLeaderDialog,
      closeElectPreferredLeaderDialog,
      openConfigThrottleDialog,
      closeConfigThrottleDialog,
      openRemoveThrottleDialog,
      closeRemoveThrottleDialog,
      openCurrentReassignmentsDialog,
      closeCurrentReassignmentsDialog,
      openClusterInfoDialog,
      closeClusterInfoDialog,
      openReplicaReassignDialog,
      closeReplicaReassignDialog,
      handleExport,
      handleImport,
      handleFileChange,
    };
  },
});
</script>

<style scoped>
.content-module {
  margin-bottom: 1%;
}
.content-module button {
  margin-right: 1%;
}
</style>

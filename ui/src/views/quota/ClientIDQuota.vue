<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="search-offset-form-advanced-search">
        <a-form
          class="ant-advanced-search-form"
          :form="form"
          @submit="handleSearch"
        >
          <a-row :gutter="24">
            <a-col :span="16">
              <a-form-item label="客户端ID">
                <a-input
                  v-decorator="['id']"
                  placeholder="请输入生产者/消费者客户端ID!"
                />
              </a-form-item>
            </a-col>
            <a-col :span="2" :style="{ textAlign: 'right' }">
              <a-form-item>
                <a-button type="primary" html-type="submit"> 搜索</a-button>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="operation-row-button">
        <a-button
          type="primary"
          @click="openAddQuotaDialog"
          v-action:quota:client:add
          >新增配置
        </a-button>
      </div>
      <QuotaList
        type="client-id"
        :columns="columns"
        :data="data"
        @refreshQuotaList="refresh"
      ></QuotaList>
      <AddQuotaConfig
        type="client-id"
        :visible="showAddQuotaDialog"
        :showClientId="true"
        @closeAddQuotaDialog="closeAddQuotaDialog"
      ></AddQuotaConfig>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaClientQuotaApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import QuotaList from "@/views/quota/QuotaList.vue";
import AddQuotaConfig from "@/views/quota/AddQuotaConfig.vue";

export default {
  name: "ClientIDQuota",
  components: { QuotaList, AddQuotaConfig },
  props: {
    topicList: {
      type: Array,
    },
  },
  data() {
    return {
      loading: false,
      form: this.$form.createForm(this, { name: "client_id_quota" }),
      data: [],
      showAlterQuotaDialog: false,
      showAddQuotaDialog: false,
      columns: [
        {
          title: "客户端ID",
          dataIndex: "client",
          key: "client",
          slots: { title: "client" },
          scopedSlots: { customRender: "client" },
        },
        {
          title: "生产速率(带宽/秒)",
          dataIndex: "producerRate",
          key: "producerRate",
        },
        {
          title: "消费速率(带宽/秒)",
          dataIndex: "consumerRate",
          key: "consumerRate",
        },
        {
          title: "吞吐量(请求占比*100)",
          dataIndex: "requestPercentage",
          key: "requestPercentage",
        },
      ],
    };
  },
  methods: {
    handleSearch() {
      this.form.validateFields((err, values) => {
        if (!err) {
          this.loading = true;
          const params = { types: ["client-id"] };
          if (values.id) {
            params.names = [values.id.trim()];
          }
          request({
            url: KafkaClientQuotaApi.getClientQuotaConfigs.url,
            method: KafkaClientQuotaApi.getClientQuotaConfigs.method,
            data: params,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.data = res.data;
            } else {
              notification.error({
                message: "error",
                description: res.msg,
              });
            }
          });
        }
      });
    },
    openAddQuotaDialog() {
      this.showAddQuotaDialog = true;
    },
    closeAddQuotaDialog(p) {
      if (p.refresh) {
        this.handleSearch();
      }
      this.showAddQuotaDialog = false;
    },
    refresh() {
      this.handleSearch();
    },
  },
  created() {
    this.handleSearch();
  },
};
</script>

<style scoped>
.tab-content {
  width: 100%;
  height: 100%;
}

.ant-advanced-search-form {
  padding: 24px;
  background: #fbfbfb;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.ant-advanced-search-form .ant-form-item {
  display: flex;
}

.ant-advanced-search-form input {
  width: 400px;
}

.ant-advanced-search-form .ant-form-item-control-wrapper {
  flex: 1;
}

#components-form-topic-advanced-search .ant-form {
  max-width: none;
  margin-bottom: 1%;
}

#search-offset-form-advanced-search .search-result-list {
  margin-top: 16px;
  border: 1px dashed #e9e9e9;
  border-radius: 6px;
  background-color: #fafafa;
  min-height: 200px;
  text-align: center;
  padding-top: 80px;
}

.operation-row-button {
  height: 4%;
  text-align: left;
  margin-bottom: 5px;
  margin-top: 5px;
}
</style>

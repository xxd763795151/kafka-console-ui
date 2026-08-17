<template>
  <div class="tab-content">
    <a-spin :spinning="loading">
      <div id="search-offset-form-advanced-search">
        <a-form
          class="ant-advanced-search-form"
          :model="searchForm"
          @finish="handleSearch"
        >
          <a-row :gutter="24">
            <a-col :span="16">
              <a-form-item label="用户标识" name="user">
                <a-input
                  v-model:value="searchForm.user"
                  placeholder="请输入用户标识，如：用户名!"
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
          v-action:quota:user:add
          >新增配置
        </a-button>
      </div>
      <QuotaList
        type="user"
        :columns="columns"
        :data="data"
        @refreshQuotaList="refresh"
      ></QuotaList>
      <AddQuotaConfig
        type="user"
        :visible="showAddQuotaDialog"
        :showUser="true"
        @closeAddQuotaDialog="closeAddQuotaDialog"
      ></AddQuotaConfig>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from "vue";
import request from "@/utils/request";
import { KafkaClientQuotaApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import QuotaList from "@/views/quota/QuotaList.vue";
import AddQuotaConfig from "@/views/quota/AddQuotaConfig.vue";

export default defineComponent({
  name: "UserQuota",
  components: { QuotaList, AddQuotaConfig },
  props: {
    topicList: {
      type: Array,
    },
  },
  setup() {
    const loading = ref<boolean>(false);
    const data = ref<any[]>([]);
    const showAlterQuotaDialog = ref<boolean>(false);
    const showAddQuotaDialog = ref<boolean>(false);
    const searchForm = reactive<any>({
      user: undefined,
    });
    const columns = ref<any[]>([
      {
        title: "用户标识",
        dataIndex: "user",
        key: "user",
        width: 300,
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
    ]);

    const handleSearch = () => {
      const values = { ...searchForm };
      loading.value = true;
      const params: any = { types: ["user"] };
      if (values.user) {
        params.names = [values.user.trim()];
      }
      request({
        url: KafkaClientQuotaApi.getClientQuotaConfigs.url,
        method: KafkaClientQuotaApi.getClientQuotaConfigs.method,
        data: params,
      }).then((res: any) => {
        loading.value = false;
        if (res.code == 0) {
          data.value = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    };
    const openAddQuotaDialog = () => {
      showAddQuotaDialog.value = true;
    };
    const closeAddQuotaDialog = (p: any) => {
      if (p.refresh) {
        handleSearch();
      }
      showAddQuotaDialog.value = false;
    };
    const refresh = () => {
      handleSearch();
    };

    onMounted(() => {
      handleSearch();
    });

    return {
      loading,
      data,
      showAlterQuotaDialog,
      showAddQuotaDialog,
      columns,
      searchForm,
      handleSearch,
      openAddQuotaDialog,
      closeAddQuotaDialog,
      refresh,
    };
  },
});
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

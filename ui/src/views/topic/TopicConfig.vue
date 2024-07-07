<template>
  <a-modal
    :title="topic + '配置'"
    :visible="show"
    :width="1400"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div>
          <a-input-search
            placeholder="属性"
            style="width: 200px"
            v-model="search"
            @input="searchData"
            @search="searchData"
          />
          <br /><br />
        </div>

        <a-table
          :columns="columns"
          :data-source="filterData"
          bordered
          :rowKey="(record) => record.name"
        >
          <div slot="operation" slot-scope="record">
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              v-show="!record.readOnly"
              @click="openEditConfigDialog(record)"
              v-action:topic:property-config:edit
              >编辑
            </a-button>
            <a-popconfirm
              :title="'删除配置项: ' + record.name + '？'"
              ok-text="确认"
              cancel-text="取消"
              v-show="isDynamic(record.source)"
              @confirm="deleteTopicConfig(record)"
            >
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                v-action:topic:property-config:del
                >删除
              </a-button>
            </a-popconfirm>
          </div>
        </a-table>
        <EditConfig
          :visible="showEditConfigDialog"
          :record="selectData"
          :topic="topic"
          @closeEditConfigDialog="closeEditConfigDialog"
        ></EditConfig>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaConfigApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import EditConfig from "@/views/topic/EditConfig";

export default {
  name: "TopicConfig",
  components: { EditConfig },
  props: {
    topic: {
      type: String,
      default: "",
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      columns: columns,
      show: this.visible,
      data: [],
      loading: false,
      search: "",
      filterData: [],
      showEditConfigDialog: false,
      selectData: {},
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getTopicConfig();
      }
    },
  },
  methods: {
    getTopicConfig() {
      this.loading = true;
      const api = KafkaConfigApi.getTopicConfig;
      request({
        url: api.url + "?topic=" + this.topic,
        method: api.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.data = res.data;
          this.searchData();
        }
      });
    },
    deleteTopicConfig(record) {
      this.selectData = record;
      this.loading = true;
      const api = KafkaConfigApi.deleteTopicConfig;
      request({
        url: api.url,
        method: api.method,
        data: {
          name: record.name,
          value: record.value,
          entity: this.topic,
        },
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.getTopicConfig();
        }
      });
    },
    searchData() {
      this.filterData = this.data.filter(
        (e) => e.name.indexOf(this.search) >= 0
      );
    },
    handleCancel() {
      this.data = [];
      this.$emit("closeTopicConfigDialog", {});
    },
    openEditConfigDialog(record) {
      this.showEditConfigDialog = true;
      this.selectData = record;
    },
    closeEditConfigDialog(params) {
      this.showEditConfigDialog = false;
      if (params.refresh) {
        this.getTopicConfig();
      }
    },
    isDynamic(source) {
      return source.startsWith("DYNAMIC_");
    },
  },
};

const columns = [
  {
    title: "属性",
    dataIndex: "name",
    key: "name",
    width: 300,
  },
  {
    title: "值",
    dataIndex: "value",
    key: "value",
  },
  {
    title: "属性源",
    dataIndex: "source",
    key: "source",
    width: 200,
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
    width: 150,
  },
];
</script>

<style scoped>
.operation-btn {
  margin-right: 3%;
}
</style>

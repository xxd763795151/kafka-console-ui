<template>
  <div>
    <a-table
        :columns="columns"
        :data-source="data"
        bordered
        :row-key="
        (record, index) => {
          return index;
        }
      "
        @change="handleChange"
    >
      <div slot="client" slot-scope="text">
        <span v-if="text" >{{text}}</span><span v-else style="color: red">默认配置</span>
      </div>
    </a-table>
  </div>
</template>

<script>

export default {
  name: "QuotaList",
  components: {},
  props: {
    columns: {
      type: Array,
    },
    data: {
      type: Array,
    },
  },
  data() {
    return {
      showDetailDialog: false,
      record: {},
      sortedInfo: null,
    };
  },
  methods: {
    openDetailDialog(record) {
      this.record = record;
      this.showDetailDialog = true;
    },
    closeDetailDialog() {
      this.showDetailDialog = false;
    },
    handleChange() {
      this.sortedInfo = arguments[2];
    },
  },
};
</script>

<style scoped></style>

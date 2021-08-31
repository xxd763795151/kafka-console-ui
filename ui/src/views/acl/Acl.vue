<template>
  <div class="content">
    <div class="acl">
      <a-table :columns="columns" :data-source="data" bordered>
        <a slot="operation" slot-scope="{}">删除</a>
        <!--        <a-table-->
        <!--          slot="expandedRowRender"-->
        <!--          slot-scope="{}"-->
        <!--          :columns="innerColumns"-->
        <!--          :data-source="innerData"-->
        <!--          :pagination="false"-->
        <!--        >-->
        <!--          <span slot="status" slot-scope="{}"> <a-badge status="success" />Finished </span>-->
        <!--          <span slot="operation" slot-scope="{}" class="table-operation">-->
        <!--            <a>Pause</a>-->
        <!--            <a>Stop</a>-->
        <!--            <a-dropdown>-->
        <!--              <a-menu slot="overlay">-->
        <!--                <a-menu-item>-->
        <!--                  Action 1-->
        <!--                </a-menu-item>-->
        <!--                <a-menu-item>-->
        <!--                  Action 2-->
        <!--                </a-menu-item>-->
        <!--              </a-menu>-->
        <!--              <a> More <a-icon type="down" /> </a>-->
        <!--            </a-dropdown>-->
        <!--          </span>-->
        <!--        </a-table>-->
      </a-table>
    </div>
  </div>
</template>

<script>
export default {
  name: "Acl",
  data() {
    return {
      data,
      columns,
      innerColumns,
      innerData,
    };
  },
  methods: {},
};

const columns = [
  { title: "用户名", dataIndex: "username", key: "username" },
  { title: "topic列表", dataIndex: "topicList", key: "topicList" },
  { title: "消费组列表", dataIndex: "groupList", key: "groupList" },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
  },
];

const data = [];
for (let i = 0; i < 30; ++i) {
  data.push({
    key: i,
    username: "amdin",
    topicList: ["test_topic1", "test_topic2", "test_topic3"].join(", "),
    groupList: [
      "test_topic1_consumer",
      "test_topic2_consumer",
      "test_topic3_consumer",
    ].join(", "),
  });
}

const innerColumns = [
  { title: "Date", dataIndex: "date", key: "date" },
  { title: "Name", dataIndex: "name", key: "name" },
  { title: "Status", key: "state", scopedSlots: { customRender: "status" } },
  { title: "Upgrade Status", dataIndex: "upgradeNum", key: "upgradeNum" },
  {
    title: "Action",
    dataIndex: "operation",
    key: "operation",
    scopedSlots: { customRender: "operation" },
  },
];

const innerData = [];
for (let i = 0; i < 3; ++i) {
  innerData.push({
    key: i,
    date: "2014-12-24 23:12:00",
    name: "This is production name",
    upgradeNum: "Upgraded: 56",
  });
}
</script>

<style scoped>
.acl {
  width: 100%;
  height: 100%;
}
</style>

<template>
  <a-modal
    title="数据同步方案"
    :open="show"
    :width="800"
    :mask="false"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      不同方案针对场景不一样。
      <h3>方案一</h3>
      双读、双写。
      <p />
      <h3>方案二</h3>
      将新集群的节点添加进来，副本重分配，最后移除老节点。
      <p />
      <h3>方案三</h3>
      迁移流程：<a
        href="https://blog.csdn.net/x763795151/article/details/121070563"
        >kafka新老集群平滑迁移实践</a
      >，需要使用到下面的同步功能。
      <p />
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from "vue";

export default defineComponent({
  name: "DataSyncScheme",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const show = ref(props.visible);

    watch(
      () => props.visible,
      (v) => {
        show.value = v;
      }
    );

    const handleCancel = () => {
      emit("closeDataSyncSchemeDialog", { refresh: false });
    };

    return {
      show,
      handleCancel,
    };
  },
});
</script>

<style scoped></style>

<template>
  <a-modal
    title="新增集群归属权限"
    :open="show"
    :width="800"
    :mask="false"
    :destroy-on-close="true"
    :footer="null"
    :mask-closable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :model="formState"
          :rules="rules"
          ref="formRef"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @finish="handleSubmit"
        >
          <a-form-item label="角色" name="roleId">
            <a-select
              show-search
              :filter-option="true"
              option-filter-prop="label"
              v-model:value="formState.roleId"
              placeholder="请选择一个角色"
            >
              <a-select-option
                v-for="role in roles"
                :key="role.id"
                :value="role.id"
                :label="role.roleName"
              >
                {{ role.roleName }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="集群" name="clusterInfoId">
            <a-select
              show-search
              :filter-option="true"
              option-filter-prop="label"
              v-model:value="formState.clusterInfoId"
              placeholder="请选择集群"
            >
              <a-select-option
                v-for="clusterInfo in clusterInfoList"
                :key="clusterInfo.id"
                :value="clusterInfo.id"
                :label="clusterInfo.clusterName"
              >
                {{ clusterInfo.clusterName }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 提交</a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, onMounted, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import request from '@/utils/request';
import notification from 'ant-design-vue/es/notification';
import {
  UserManageApi,
  KafkaClusterApi,
  ClusterRoleRelationApi,
} from '@/utils/api';

export default defineComponent({
  name: 'CreateClusterRoleRelation',
  props: {
    open: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['closeCreateClusterRoleRelationDialog'],
  setup(props, { emit }) {
    const formRef = ref();

    const state = reactive({
      show: props.open,
      data: [] as any[],
      loading: false,
      roles: [] as any[],
      clusterInfoList: [] as any[],
      formState: {
        roleId: undefined as any,
        clusterInfoId: undefined as any,
      },
      rules: {
        roleId: [{ required: true, message: '请选择一个角色!' }],
        clusterInfoId: [{ required: true, message: '请选择集群!' }],
      },
    });

    watch(
      () => props.open,
      (v) => {
        state.show = v;
        if (state.show) {
          getRoles();
        }
      }
    );

    const handleSubmit = async (values: any) => {
      state.loading = true;
      request({
        url: ClusterRoleRelationApi.add.url,
        method: ClusterRoleRelationApi.add.method,
        data: values,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit('closeCreateClusterRoleRelationDialog', {
            refresh: true,
            data: res.data,
          });
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    const getRoles = () => {
      state.loading = true;
      request({
        url: UserManageApi.getRole.url,
        method: UserManageApi.getRole.method,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          state.roles = res.data;
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    const getClusterInfoList = () => {
      request({
        url: KafkaClusterApi.getClusterInfoListForSelect.url,
        method: KafkaClusterApi.getClusterInfoListForSelect.method,
      }).then((res: any) => {
        if (res.code == 0) {
          state.clusterInfoList = res.data;
          state.clusterInfoList.splice(0, 0, { id: -1, clusterName: '全部' });
        } else {
          notification.error({
            message: 'error',
            description: res.msg,
          });
        }
      });
    };

    const handleCancel = () => {
      state.data = [];
      emit('closeCreateClusterRoleRelationDialog', { refresh: true });
    };

    onMounted(() => {
      getRoles();
      getClusterInfoList();
    });

    return {
      ...toRefs(state),
      formRef,
      handleSubmit,
      getRoles,
      getClusterInfoList,
      handleCancel,
    };
  },
});
</script>

<style scoped></style>

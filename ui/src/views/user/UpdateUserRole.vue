<template>
  <a-modal
    title="分配用户角色"
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
          @submit="handleSubmit"
        >
          <a-form-item label="用户名" name="username">
            <a-input
              :disabled="true"
              v-model:value="formState.username"
            />
          </a-form-item>
          <a-form-item label="角色" name="roleIds">
            <a-select
              show-search
              :filter-option="true"
              option-filter-prop="label"
              v-model:value="formState.roleIds"
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
import { UserManageApi } from '@/utils/api';

export default defineComponent({
  name: 'UpdateUserRole',
  props: {
    open: {
      type: Boolean,
      default: false,
    },
    user: {
      type: Object,
      default: () => ({}),
    },
  },
  emits: ['closeUpdateUserRoleDialog'],
  setup(props, { emit }) {
    const formRef = ref();

    const state = reactive({
      show: props.open,
      loading: false,
      roles: [] as any[],
      formState: {
        username: '',
        roleIds: undefined as any,
      },
      rules: {
        roleIds: [{ required: true, message: '请选择一个角色!' }],
      },
    });

    watch(
      () => props.open,
      (v) => {
        state.show = v;
        if (state.show) {
          state.formState.username = props.user?.username || '';
          getRoles();
        }
      }
    );

    const handleSubmit = async (e: any) => {
      e.preventDefault();
      const params = Object.assign({}, props.user, state.formState);
      params.roleIds = state.formState.roleIds;
      state.loading = true;
      request({
        url: UserManageApi.addOrUpdateUser.url,
        method: UserManageApi.addOrUpdateUser.method,
        data: params,
      }).then((res: any) => {
        state.loading = false;
        if (res.code == 0) {
          message.success(res.msg);
          emit('closeUpdateUserRoleDialog', {
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

    const handleCancel = () => {
      emit('closeUpdateUserRoleDialog', { refresh: false });
    };

    onMounted(() => {
      getRoles();
    });

    return {
      ...toRefs(state),
      formRef,
      handleSubmit,
      getRoles,
      handleCancel,
    };
  },
});
</script>

<style scoped></style>

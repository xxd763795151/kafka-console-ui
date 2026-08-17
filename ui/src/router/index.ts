import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router';
import Home from '../views/Home.vue';
import Store from '@/store';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/acl-page',
    name: 'Acl',
    component: () => import(/* webpackChunkName: "acl" */ '../views/acl/Acl.vue'),
  },
  {
    path: '/topic-page',
    name: 'Topic',
    component: () => import(/* webpackChunkName: "topic" */ '../views/topic/Topic.vue'),
  },
  {
    path: '/group-page',
    name: 'Group',
    component: () => import(/* webpackChunkName: "group" */ '../views/group/Group.vue'),
  },
  {
    path: '/op-page',
    name: 'Operation',
    component: () => import(/* webpackChunkName: "op" */ '../views/op/Operation.vue'),
  },
  {
    path: '/cluster-page',
    name: 'Cluster',
    component: () => import(/* webpackChunkName: "cluster" */ '../views/cluster/Cluster.vue'),
  },
  {
    path: '/message-page',
    name: 'Message',
    component: () => import(/* webpackChunkName: "message" */ '../views/message/Message.vue'),
  },
  {
    path: '/client-quota-page',
    name: 'ClientQuota',
    component: () =>
      import(/* webpackChunkName: "quota" */ '../views/quota/ClientQuota.vue'),
  },
  {
    path: '/user-page',
    name: 'UserManage',
    component: () =>
      import(/* webpackChunkName: "user" */ '../views/user/UserManage.vue'),
  },
  {
    path: '/login-page',
    name: 'Login',
    component: () => import(/* webpackChunkName: "login" */ '../views/login/Login.vue'),
  },
];

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes,
});

router.beforeEach((to, _from, next) => {
  const enableAuth = Store.state.auth.enable;
  if (!enableAuth) {
    next();
  } else {
    if (to.path === '/login-page') {
      next();
    } else {
      const token = localStorage.getItem('access_token');
      if (token === null || token === '') {
        next('/login-page');
      } else {
        next();
      }
    }
  }
});

export default router;

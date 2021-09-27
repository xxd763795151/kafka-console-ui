import Vue from "vue";
import VueRouter from "vue-router";
import Home from "../views/Home.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "Home",
    component: Home,
  },
  {
    path: "/acl-page",
    name: "Acl",
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () =>
      import(/* webpackChunkName: "acl" */ "../views/acl/Acl.vue"),
  },
  {
    path: "/topic-page",
    name: "Topic",
    component: () =>
      import(/* webpackChunkName: "topic" */ "../views/topic/Topic.vue"),
  },
  {
    path: "/group-page",
    name: "Group",
    component: () =>
      import(/* webpackChunkName: "group" */ "../views/group/Group.vue"),
  },
  {
    path: "/op-page",
    name: "Operation",
    component: () =>
      import(/* webpackChunkName: "op" */ "../views/op/Operation.vue"),
  },
];

const router = new VueRouter({
  mode: "history",
  // mode: "hash",
  base: process.env.BASE_URL,
  routes,
});

export default router;

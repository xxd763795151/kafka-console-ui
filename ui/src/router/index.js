import Vue from "vue";
import VueRouter from "vue-router";
import Home from "../views/Home.vue";
import Store from "@/store";

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
  {
    path: "/cluster-page",
    name: "Cluster",
    component: () =>
      import(/* webpackChunkName: "cluster" */ "../views/cluster/Cluster.vue"),
  },
  {
    path: "/message-page",
    name: "Message",
    component: () =>
      import(/* webpackChunkName: "cluster" */ "../views/message/Message.vue"),
  },
  {
    path: "/client-quota-page",
    name: "ClientQuota",
    component: () =>
      import(
        /* webpackChunkName: "cluster" */ "../views/quota/ClientQuota.vue"
      ),
  },
  {
    path: "/user-page",
    name: "UserManage",
    component: () =>
      import(/* webpackChunkName: "cluster" */ "../views/user/UserManage.vue"),
  },
  {
    path: "/login-page",
    name: "Login",
    component: () =>
      import(/* webpackChunkName: "cluster" */ "../views/login/Login.vue"),
  },
];

const router = new VueRouter({
  // mode: "history",
  mode: "hash",
  base: process.env.BASE_URL,
  routes,
});

router.beforeEach((to, from, next) => {
  const enableAuth = Store.state.auth.enable;
  if (!enableAuth) {
    next();
  } else {
    if (to.path === "/login-page") {
      next();
    } else {
      let token = localStorage.getItem("access_token");
      if (token === null || token === "") {
        next("/login-page");
      } else {
        next();
      }
    }
  }
});

let originPush = VueRouter.prototype.push;
let originReplace = VueRouter.prototype.replace;
VueRouter.prototype.push = function (location, resolve, reject) {
  if (resolve && reject) {
    originPush.call(this, location, resolve, reject);
  } else {
    originPush.call(
      this,
      location,
      () => {},
      () => {}
    );
  }
};
VueRouter.prototype.replace = function (location, resolve, reject) {
  if (resolve && reject) {
    originReplace.call(this, location, resolve, reject);
  } else {
    originReplace.call(
      this,
      location,
      () => {},
      () => {}
    );
  }
};

export default router;

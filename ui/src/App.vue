<template>
  <div id="app">
    <div id="nav">
      <router-link to="/" class="pad-l-r">主页</router-link>
      |<router-link to="/acl" class="pad-l-r">Acl</router-link>
      <!--      |<router-link-->
      <!--        to="/topic"-->
      <!--        class="pad-l-r"-->
      <!--        >主题</router-link-->
      <!--      >-->
    </div>
    <router-view class="content" />
  </div>
</template>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#app {
  width: 100%;
  height: 100%;
}

#nav {
  background-color: #9fe0e0;
  font-size: large;
  padding-top: 1%;
  padding-bottom: 1%;
  margin-bottom: 1%;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #61c126;
}

.pad-l-r {
  padding-left: 10px;
  padding-right: 10px;
}

.content {
  padding-left: 2%;
  padding-right: 2%;
  height: 90%;
  width: 100%;
}
</style>

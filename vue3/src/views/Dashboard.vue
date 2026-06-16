<template>
  <div class="dashboard">
    <div class="hero">
      <pre class="ascii">  ██████╗ ███████╗
 ██╔════╝ ██╔════╝
 ██║      ███████╗
 ██║      ╚════██║
 ╚██████╗ ███████║
  ╚═════╝ ╚══════╝</pre>
      <div class="hero-sub">购机，本该如此</div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 120px;
}

.hero {
  text-align: center;
}

.ascii {
  font-family: var(--font-mono);
  font-size: 18px;
  line-height: 1.25;
  margin: 0 0 24px;
  letter-spacing: 0;
  background: linear-gradient(180deg, #a0a0a0 0%, #5a5a5a 40%, #2a2a2a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-sub {
  font-size: 20px;
  font-weight: 500;
  letter-spacing: 2px;
  background: linear-gradient(180deg, #a0a0a0 0%, #5a5a5a 40%, #2a2a2a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>

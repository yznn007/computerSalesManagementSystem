<template>
  <div class="dashboard">
    <div class="hero">
      <pre class="ascii">  ██████╗ ███████╗
 ██╔════╝ ██╔════╝
 ██║      ███████╗
 ██║      ╚════██║
 ╚██████╗ ███████║
  ╚═════╝ ╚══════╝</pre>
      <div class="hero-sub">电脑销售系统</div>
      <div class="hero-desc">MySQL InnoDB · 悲观锁 · Spring Boot 4.0 · Vue 3</div>
      <div class="hero-cmd">$ npm run dev</div>
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
  color: #e0e0e0;
  letter-spacing: 2px;
  margin-bottom: 8px;
}

.hero-desc {
  font-family: var(--font-mono);
  font-size: 12px;
  color: #5a5a5a;
  margin-bottom: 16px;
}

.hero-cmd {
  font-family: var(--font-mono);
  font-size: 13px;
  color: #3a3a3a;
}
</style>

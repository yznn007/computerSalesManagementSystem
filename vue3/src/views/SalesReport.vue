<template>
  <div class="page">
    <div class="page-header">
      <span class="page-title">统计概览</span>
    </div>

    <div v-loading="loading" class="stats-grid">
      <template v-if="data">
        <!-- 订单状态分布 — 环形图 -->
        <div class="card">
          <div class="card-title">订单状态分布</div>
          <div class="card-chart">
            <svg class="donut" viewBox="0 0 200 200">
              <circle class="donut-bg" cx="100" cy="100" r="72" />
              <template v-for="(s, i) in donutSegments" :key="i">
                <circle
                  class="donut-seg"
                  cx="100" cy="100" r="72"
                  :stroke="s.color"
                  :stroke-dasharray="`${s.len} ${circumference - s.len}`"
                  :stroke-dashoffset="-s.offset"
                />
              </template>
              <text class="donut-num" x="100" y="96" text-anchor="middle">{{ data.orders.total }}</text>
              <text class="donut-label" x="100" y="114" text-anchor="middle">总订单</text>
            </svg>
            <div class="legend">
              <div v-for="s in data.orders.by_status" :key="s.status" class="legend-row">
                <span class="legend-dot" :style="{ background: statusColor(s.status) }"></span>
                <span class="legend-text">{{ s.status }}</span>
                <span class="legend-count mono">{{ s.count }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 商品分类分布 — 水平条形图 -->
        <div class="card">
          <div class="card-title">商品分类</div>
          <div class="card-chart bars">
            <div class="bar-row" v-for="c in data.products.by_category" :key="c.category">
              <span class="bar-label">{{ c.category }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: barWidth(c.count, maxProductCount) + '%' }"></div>
              </div>
              <span class="bar-count mono">{{ c.count }}</span>
            </div>
            <div class="bar-divider"></div>
            <div class="bar-row summary">
              <span class="bar-label">低库存预警 ≤5</span>
              <span class="bar-count mono" :class="{ alert: data.products.low_stock > 0 }">{{ data.products.low_stock }}</span>
            </div>
          </div>
        </div>

        <!-- 销售额对比 — 垂直柱状图 -->
        <div class="card">
          <div class="card-title">销售金额</div>
          <div class="card-chart">
            <svg class="vbars" viewBox="0 0 240 180">
              <line class="vbars-axis" x1="30" y1="150" x2="220" y2="150" />
              <template v-for="(b, i) in salesBars" :key="i">
                <rect
                  class="vbar-rect"
                  :x="b.x" :y="b.y" :width="b.w" :height="b.h"
                  :fill="b.color"
                />
                <text class="vbar-val" :x="b.x + b.w / 2" :y="b.y - 6" text-anchor="middle">{{ b.short }}</text>
                <text class="vbar-label" :x="b.x + b.w / 2" y="166" text-anchor="middle">{{ b.label }}</text>
              </template>
            </svg>
          </div>
        </div>

        <!-- 客户统计 — 进度环 -->
        <div class="card">
          <div class="card-title">客户</div>
          <div class="card-chart">
            <div class="gauge-block">
              <svg class="gauge" viewBox="0 0 200 120">
                <path class="gauge-bg" d="M20 100 A 80 80 0 0 1 180 100" />
                <path
                  class="gauge-fill"
                  d="M20 100 A 80 80 0 0 1 180 100"
                  :stroke-dasharray="`${gaugeLen} ${arcLength - gaugeLen}`"
                />
              </svg>
              <div class="gauge-center">
                <span class="gauge-pct mono">{{ activePct }}%</span>
                <span class="gauge-sub">活跃率</span>
              </div>
            </div>
            <div class="metric-list">
              <div class="metric"><span class="m-label">总客户</span><span class="m-value mono">{{ data.customers.total }}</span></div>
              <div class="metric"><span class="m-label">活跃客户</span><span class="m-value mono ok">{{ data.customers.active }}</span></div>
              <div class="metric"><span class="m-label">人均订单</span><span class="m-value mono">{{ data.customers.avg_orders }}</span></div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStatsOverview } from '../api'

const data = ref(null)
const loading = ref(false)

const RADIUS = 72
const circumference = 2 * Math.PI * RADIUS
const ARC_R = 80
const arcLength = Math.PI * ARC_R

const statusColorMap = {
  '待付款': '#7a7a7a',
  '已付款': '#e0e0e0',
  '已发货': '#b0b0b0',
  '已取消': '#4a4a4a',
  '已退货': '#5a5a5a',
}

const statusColor = (s) => statusColorMap[s] || '#5a5a5a'

const donutSegments = computed(() => {
  if (!data.value?.orders?.by_status) return []
  const total = data.value.orders.total || 1
  let offset = 0
  return data.value.orders.by_status.map(s => {
    const ratio = s.count / total
    const len = ratio * circumference
    const seg = { color: statusColor(s.status), len, offset }
    offset += len
    return seg
  })
})

const maxProductCount = computed(() => {
  if (!data.value?.products?.by_category) return 1
  return Math.max(...data.value.products.by_category.map(c => c.count), 1)
})

const barWidth = (count, max) => Math.round((count / max) * 100)

const salesBars = computed(() => {
  if (!data.value?.sales) return []
  const sales = data.value.sales
  const paid = Number(sales.paid) || 0
  const pending = Number(sales.pending) || 0
  const vals = [
    { label: '已付款', raw: paid, color: '#e0e0e0' },
    { label: '待付款', raw: pending, color: '#7a7a7a' },
    { label: '合计', raw: paid + pending, color: '#b0b0b0' },
  ]
  const max = Math.max(...vals.map(v => v.raw), 1)
  const gap = 26
  const bw = 40
  const startX = 38
  const baseY = 150
  const chartH = 120
  return vals.map((v, i) => {
    const h = Math.round((v.raw / max) * chartH)
    return {
      x: startX + i * (bw + gap),
      y: baseY - h,
      w: bw,
      h,
      color: v.color,
      label: v.label,
      short: v.raw >= 10000 ? (v.raw / 10000).toFixed(1) + 'w' : v.raw.toFixed(0),
    }
  })
})

const activePct = computed(() => {
  if (!data.value?.customers) return 0
  const total = data.value.customers.total || 1
  return Math.round((data.value.customers.active / total) * 100)
})

const gaugeLen = computed(() => (activePct.value / 100) * arcLength)

onMounted(async () => {
  loading.value = true
  try { data.value = (await getStatsOverview()).data } catch {} finally { loading.value = false }
})
</script>

<style scoped>
.page { padding-top: 24px; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 13px; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-tertiary); }

.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }

.card { border: 1px solid var(--border-dim); border-radius: 6px; padding: 20px 24px; background: var(--bg-elevated); }
.card-title { font-size: 12px; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-tertiary); margin-bottom: 20px; }
.card-chart { display: flex; align-items: center; gap: 24px; min-height: 180px; }

/* --- 环形图 --- */
.donut { width: 180px; height: 180px; flex-shrink: 0; }
.donut-bg { fill: none; stroke: var(--border-dim); stroke-width: 16; }
.donut-seg { fill: none; stroke-width: 16; stroke-linecap: butt; transition: stroke-width 0.15s; }
.donut-seg:hover { stroke-width: 20; }
.donut-num { font-family: var(--font-mono); font-size: 28px; fill: var(--text-primary); font-weight: 600; }
.donut-label { font-family: var(--font-sans); font-size: 11px; fill: var(--text-tertiary); }

.legend { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.legend-row { display: flex; align-items: center; gap: 8px; }
.legend-dot { width: 8px; height: 8px; border-radius: 2px; flex-shrink: 0; }
.legend-text { font-size: 13px; color: var(--text-secondary); }
.legend-count { font-size: 13px; font-weight: 500; color: var(--text-primary); margin-left: auto; }

/* --- 水平条形图 --- */
.bars { flex-direction: column; gap: 14px; width: 100%; }
.bar-row { display: flex; align-items: center; gap: 10px; width: 100%; }
.bar-label { font-size: 13px; color: var(--text-secondary); width: 80px; flex-shrink: 0; }
.bar-track { flex: 1; height: 6px; background: var(--border-dim); border-radius: 3px; overflow: hidden; }
.bar-fill { height: 100%; background: var(--text-secondary); border-radius: 3px; transition: width 0.4s ease; }
.bar-count { font-size: 13px; font-weight: 500; color: var(--text-primary); width: 28px; text-align: right; flex-shrink: 0; }
.bar-divider { height: 1px; background: var(--border-dim); margin: 4px 0; }
.bar-row.summary .bar-label { color: var(--text-tertiary); }
.bar-count.alert { color: #e0e0e0; font-weight: 600; }

/* --- 垂直柱状图 --- */
.vbars { width: 100%; max-width: 280px; height: 180px; margin: 0 auto; }
.vbars-axis { stroke: var(--border-dim); stroke-width: 1; }
.vbar-rect { rx: 2; transition: opacity 0.15s; }
.vbar-rect:hover { opacity: 0.7; }
.vbar-val { font-family: var(--font-mono); font-size: 11px; fill: var(--text-secondary); }
.vbar-label { font-family: var(--font-sans); font-size: 11px; fill: var(--text-tertiary); }

/* --- 进度环/仪表盘 --- */
.gauge-block { position: relative; width: 200px; height: 120px; flex-shrink: 0; }
.gauge { width: 200px; height: 120px; }
.gauge-bg { fill: none; stroke: var(--border-dim); stroke-width: 10; stroke-linecap: round; }
.gauge-fill { fill: none; stroke: #e0e0e0; stroke-width: 10; stroke-linecap: round; transition: stroke-dasharray 0.5s ease; }
.gauge-center { position: absolute; bottom: 4px; left: 50%; transform: translateX(-50%); text-align: center; }
.gauge-pct { font-size: 22px; font-weight: 600; color: var(--text-primary); display: block; }
.gauge-sub { font-size: 11px; color: var(--text-tertiary); }

.metric-list { flex: 1; display: flex; flex-direction: column; gap: 12px; }
.metric { display: flex; align-items: baseline; justify-content: space-between; }
.m-label { font-size: 13px; color: var(--text-tertiary); }
.m-value { font-size: 16px; font-weight: 500; color: var(--text-primary); }
.m-value.ok { color: #e0e0e0; }

.mono { font-family: var(--font-mono); }
</style>

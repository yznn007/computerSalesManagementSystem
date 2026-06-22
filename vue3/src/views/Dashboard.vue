<template>
	<div class="dashboard">
		<div class="hero">
			<pre class="ascii">
  ██████╗ ███████╗
 ██╔════╝ ██╔════╝
 ██║  ███████╗
 ██║  ╚════██║
 ╚██████╗ ███████║
  ╚═════╝ ╚══════╝</pre
			>
			<div class="hero-sub">One click, one PC</div>
			<div class="search-wrapper">
				<div class="search-box">
					<svg
						class="search-icon"
						xmlns="http://www.w3.org/2000/svg"
						width="24"
						height="24"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2"
						stroke-linecap="round"
						stroke-linejoin="round"
					>
						<circle
							cx="11"
							cy="11"
							r="8"
						/>
						<path d="m21 21-4.3-4.3" />
					</svg>
					<input
						v-model="keyword"
						class="search-input"
						placeholder="Search products..."
						@keydown.enter="search"
					/>
				</div>
			</div>
		</div>

		<div class="product-section">
			<div class="page-header">
				<div class="category-tabs">
					<button
						v-for="cat in cats"
						:key="cat"
						class="tab"
						:class="{ active: activeCategory === cat }"
						@click="selectCategory(cat)"
					>
						{{ cat || "全部" }}
					</button>
				</div>
				<div class="sort-btns">
					<button
						v-for="f in sortFields"
						:key="f.key"
						class="sort-btn"
						:class="{ active: sortField === f.key }"
						@click="toggleSort(f.key)"
					>
						{{ f.label
						}}<span
							v-if="sortField === f.key"
							class="sort-arrow"
							>{{ sortDir === "desc" ? "↓" : "↑" }}</span
						>
					</button>
				</div>
				<div
					v-if="keyword"
					class="search-notice"
				>
					搜索「{{ keyword }}」
				</div>
			</div>

			<div
				v-if="activeCategory && subCats[activeCategory]?.length"
				class="sub-tabs"
			>
				<button
					v-for="sub in subCats[activeCategory]"
					:key="sub"
					class="sub-tab"
					:class="{ active: activeSub === sub }"
					@click="selectSub(activeCategory, sub)"
				>
					{{ sub }}
				</button>
			</div>

			<div
				v-if="activeCategory === 'DIY配件' && activeSub && subCats3[activeSub]?.length"
				class="sub-tabs third-tabs"
			>
				<button
					v-for="third in subCats3[activeSub]"
					:key="third"
					class="sub-tab third-tab"
					:class="{ active: activeThird === third }"
					@click="selectThird(third)"
				>
					{{ third }}
				</button>
			</div>

			<div class="table-head">
				<span class="th th-name">商品</span>
				<span class="th th-tag">分类</span>
				<span class="th th-stock">库存</span>
				<span class="th th-price">售价</span>
				<span class="th th-act"></span>
			</div>

			<div v-loading="loading">
				<div
					v-for="p in displayedProducts"
					:key="p.product_id"
					class="row"
					@click="showDetail(p)"
				>
					<span class="row-name">{{ p.brand }} {{ p.model }}</span>
					<span class="row-tag">{{ p.category }}</span>
					<span class="row-stock mono-dim">{{ p.stock }}</span>
					<span class="row-price mono">¥{{ p.price }}</span>
					<button
						class="add-btn"
						:disabled="p.stock <= 0"
						@click.stop="addToCart(p)"
					>
						+
					</button>
				</div>
				<div
					v-if="!loading && displayedProducts.length === 0"
					class="empty"
				>
					暂无商品
				</div>
			</div>

			<el-dialog
				v-model="detailVisible"
				:title="detailProduct?.model"
				width="420px"
			>
				<template v-if="detailProduct">
					<div class="detail-row">
						<span>品牌</span><span>{{ detailProduct.brand }}</span>
					</div>
					<div class="detail-row">
						<span>售价</span
						><span class="mono">¥{{ detailProduct.price }}</span>
					</div>
					<div class="detail-row">
						<span>库存</span
						><span class="mono">{{ detailProduct.stock }}</span>
					</div>
					<div v-if="detailDetail">
						<div
							v-if="detailDetail.screen_size !== undefined"
							class="detail-row"
						>
							<span>屏幕</span
							><span>{{ detailDetail.screen_size }}</span>
						</div>
						<div
							v-if="detailDetail.cpu_model"
							class="detail-row"
						>
							<span>处理器</span
							><span>{{ detailDetail.cpu_model }}</span>
						</div>
						<div
							v-if="detailDetail.gpu_model"
							class="detail-row"
						>
							<span>显卡</span
							><span>{{ detailDetail.gpu_model }}</span>
						</div>
						<div
							v-if="detailDetail.weight"
							class="detail-row"
						>
							<span>重量</span
							><span>{{ detailDetail.weight }}</span>
						</div>
						<div
							v-if="detailDetail.form_factor"
							class="detail-row"
						>
							<span>机箱</span
							><span>{{ detailDetail.form_factor }}</span>
						</div>
						<div
							v-if="detailDetail.cpu_desc"
							class="detail-row"
						>
							<span>处理器</span
							><span>{{ detailDetail.cpu_desc }}</span>
						</div>
						<div
							v-if="detailDetail.gpu_desc"
							class="detail-row"
						>
							<span>显卡</span
							><span>{{ detailDetail.gpu_desc }}</span>
						</div>
						<div
							v-if="detailDetail.ram_desc"
							class="detail-row"
						>
							<span>内存</span
							><span>{{ detailDetail.ram_desc }}</span>
						</div>
						<div
							v-if="detailDetail.storage_desc"
							class="detail-row"
						>
							<span>存储</span
							><span>{{ detailDetail.storage_desc }}</span>
						</div>
						<div
							v-if="detailDetail.part_type"
							class="detail-row"
						>
							<span>配件</span
							><span
								>{{ detailDetail.part_type }} /
								{{ detailDetail.specification }}</span
							>
						</div>
					</div>
					<div
						v-if="detailDetail?.composition?.length"
						style="
							margin-top: 12px;
							border-top: 1px solid #333;
							padding-top: 12px;
						"
					>
						<div
							style="
								font-size: 12px;
								color: #999;
								margin-bottom: 8px;
							"
						>
							组装配置
						</div>
						<div
							v-for="c in detailDetail.composition"
							:key="c.product_id"
							class="detail-row"
						>
							<span
								>{{ c.part_type }} · {{ c.brand }}
								{{ c.model }}</span
							>
							<span class="mono">×{{ c.quantity }}</span>
						</div>
					</div>
				</template>
				<template #footer>
					<el-button
						v-if="detailProduct"
						@click="detailVisible = false"
						size="small"
						>关闭</el-button
					>
					<el-button
						v-if="detailProduct"
						type="primary"
						size="small"
						:disabled="detailProduct.stock <= 0"
						@click="
							addToCart(detailProduct);
							detailVisible = false;
						"
						>加入购物车</el-button
					>
				</template>
			</el-dialog>
		</div>

		<button
			v-if="cartItems.length"
			class="cart-fab"
			@click="cartVisible = true"
		>
			<svg
				width="20"
				height="20"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2"
			>
				<circle
					cx="9"
					cy="21"
					r="1"
				/>
				<circle
					cx="20"
					cy="21"
					r="1"
				/>
				<path
					d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"
				/>
			</svg>
			<span class="cart-badge">{{ totalCount }}</span>
		</button>

		<el-drawer
			v-model="cartVisible"
			title="购物车"
			size="360px"
			direction="rtl"
		>
			<div
				v-if="isStaff"
				class="cart-block"
			>
				<div class="cart-label">客户</div>
				<el-select
					v-model="selectedCustomer"
					placeholder="请选择客户"
					filterable
					value-key="customer_id"
					class="dark-select"
				>
					<el-option
						v-for="c in customers"
						:key="c.customer_id"
						:label="`${c.customer_name} — ${c.phone}`"
						:value="c"
					/>
				</el-select>
			</div>
			<div class="cart-body">
				<div
					v-if="cartItems.length === 0"
					class="cart-empty"
				>
					购物车为空
				</div>
				<template v-else>
					<div class="cart-list">
						<div
							v-for="(item, index) in cartItems"
							:key="item.product_id"
							class="cart-row"
						>
							<div class="cart-row-top">
								<span class="cart-row-name"
									>{{ item.brand }} {{ item.model }}</span
								>
								<span class="cart-row-price mono"
									>¥{{
										(item.price * item.quantity).toFixed(2)
									}}</span
								>
							</div>
							<div class="cart-row-ctl">
								<el-input-number
									v-model="item.quantity"
									:min="1"
									:max="item.stock"
									size="small"
									controls-position="right"
									class="qty"
								/>
								<button
									class="rm"
									@click="removeFromCart(index)"
								>
									×
								</button>
							</div>
						</div>
					</div>
					<div class="cart-footer">
						<div class="cart-total">
							<span class="total-label"
								>共 {{ totalCount }} 件</span
							>
							<span class="total-price mono"
								>¥{{ totalPrice.toFixed(2) }}</span
							>
						</div>
						<button
							class="submit"
							:disabled="
								(isStaff && !selectedCustomer) || submitting
							"
							@click="submitOrder"
						>
							{{ submitting ? "..." : "提交订单" }}
						</button>
					</div>
				</template>
			</div>
		</el-drawer>
	</div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {
	getProducts,
	getProductDetail,
	getCustomers,
	createOrder,
} from "../api";

const isStaff = computed(() => localStorage.getItem("role") === "staff");
const keyword = ref("");
const cats = ['', '笔记本', '台式机整机', 'DIY配件'];
const subCats = {
	笔记本: ['Lenovo', 'Apple', 'ASUS', 'Dell', 'HP', 'HUAWEI', 'MSI', 'Xiaomi', 'Honor', 'Razer'],
	台式机整机: ['Lenovo', 'HP', 'ASUS', 'Dell', 'MSI'],
	DIY配件: ['CPU', '显卡', '主板', '内存', '硬盘', '电源', '机箱', '散热器'],
};
const subCats3 = {
	CPU: ['AMD', 'Intel'],
	显卡: ['NVIDIA', 'AMD', 'ASUS'],
	主板: ['ASUS', 'MSI', 'Gigabyte', 'ASRock'],
	内存: ['Corsair', 'G.SKILL', 'Kingston', 'Crucial'],
	硬盘: ['Samsung', 'WD', 'Crucial', 'Seagate', 'Kingston'],
	电源: ['Corsair', 'Seasonic', 'Super Flower', 'FSP', 'Great Wall', 'Segotep', 'Antec'],
	机箱: ['Lian Li', 'NZXT', 'Phanteks', 'CoolerMaster', 'Fractal Design', 'be quiet!'],
	散热器: ['Noctua', 'Thermalright', 'Valkyrie', 'DeepCool', 'CoolerMaster', 'EK'],
};
const activeCategory = ref("");
const activeSub = ref("");
const activeThird = ref("");
const products = ref([]);
const loading = ref(false);
const detailVisible = ref(false);
const detailProduct = ref(null);
const detailDetail = ref(null);

const sortFields = [
	{ key: "name", label: "名称" },
	{ key: "price", label: "价格" },
	{ key: "stock", label: "库存" },
];
const sortField = ref(null);
const sortDir = ref("desc");

const toggleSort = (field) => {
	if (sortField.value === field) {
		if (sortDir.value === "desc") sortDir.value = "asc";
		else {
			sortField.value = null;
			sortDir.value = "desc";
		}
	} else {
		sortField.value = field;
		sortDir.value = "desc";
	}
};

const filteredProducts = computed(() => {
	let result = products.value;
	if (keyword.value) {
		const kw = keyword.value.toLowerCase();
		result = result.filter(
			(p) =>
				p.brand.toLowerCase().includes(kw) ||
				p.model.toLowerCase().includes(kw),
		);
	}
	if (activeSub.value) {
		if (activeCategory.value === "DIY配件") {
			result = result.filter((p) => p.part_type === activeSub.value);
		} else {
			const sub = activeSub.value.toLowerCase();
			result = result.filter(
				(p) => p.brand && p.brand.toLowerCase().includes(sub),
			);
		}
	}
	if (activeThird.value && activeCategory.value === "DIY配件") {
		result = result.filter((p) => p.brand === activeThird.value);
	}
	return result;
});

const displayedProducts = computed(() => {
	const arr = [...filteredProducts.value];
	if (sortField.value === "name")
		arr.sort((a, b) =>
			(a.brand + a.model).localeCompare(b.brand + b.model, "zh"),
		);
	else if (sortField.value === "price") arr.sort((a, b) => a.price - b.price);
	else if (sortField.value === "stock") arr.sort((a, b) => a.stock - b.stock);
	if (sortField.value && sortDir.value === "desc") arr.reverse();
	return arr;
});

const search = () => {};

const selectCategory = (cat) => {
	if (activeCategory.value === cat) {
		activeCategory.value = "";
		activeSub.value = "";
		activeThird.value = "";
	} else {
		activeCategory.value = cat;
		activeSub.value = "";
		activeThird.value = "";
	}
	fetchProducts();
};

const selectSub = (cat, sub) => {
	activeCategory.value = cat;
	activeSub.value = activeSub.value === sub ? "" : sub;
	activeThird.value = "";
};

const selectThird = (third) => {
	activeThird.value = activeThird.value === third ? "" : third;
};

const fetchProducts = async () => {
	loading.value = true;
	try {
		products.value =
			(await getProducts(activeCategory.value || undefined)).data || [];
	} catch {
	} finally {
		loading.value = false;
	}
};

const showDetail = async (p) => {
	detailProduct.value = p;
	detailVisible.value = true;
	try {
		detailDetail.value = (await getProductDetail(p.product_id)).data;
	} catch {
		detailDetail.value = null;
	}
};

const customers = ref([]);
const selectedCustomer = ref(null);
const cartItems = ref([]);
const cartVisible = ref(false);
const submitting = ref(false);

const totalCount = computed(() =>
	cartItems.value.reduce((s, i) => s + i.quantity, 0),
);
const totalPrice = computed(() =>
	cartItems.value.reduce((s, i) => s + i.price * i.quantity, 0),
);

const fetchCustomers = async () => {
	if (!isStaff.value) return;
	try {
		customers.value = (await getCustomers()).data || [];
	} catch {}
};

const addToCart = (p) => {
	if (p.stock <= 0) return ElMessage.warning("库存不足");
	const ex = cartItems.value.find((i) => i.product_id === p.product_id);
	if (ex) {
		ex.quantity < p.stock ? ex.quantity++ : ElMessage.warning("库存不足");
	} else {
		cartItems.value.push({ ...p, quantity: 1 });
		ElMessage.success({ message: "已加入购物车", duration: 1000 });
	}
};

const removeFromCart = (i) => cartItems.value.splice(i, 1);

const submitOrder = async () => {
	if (isStaff.value && !selectedCustomer.value)
		return ElMessage.warning("请选择客户");
	submitting.value = true;
	try {
		const payload = {
			items: cartItems.value.map((i) => ({
				product_id: i.product_id,
				quantity: i.quantity,
			})),
		};
		if (isStaff.value)
			payload.customer_id = selectedCustomer.value.customer_id;
		const res = await createOrder(payload);
		if (res.data?.status === 0) {
			ElMessage.success(`下单成功 ${res.data.order_no}`);
			cartItems.value = [];
			cartVisible.value = false;
			selectedCustomer.value = null;
			fetchProducts();
		} else ElMessage.error("下单失败");
	} catch {
	} finally {
		submitting.value = false;
	}
};

onMounted(() => {
	fetchProducts();
	fetchCustomers();
});
</script>

<style scoped>
.dashboard {
	display: flex;
	flex-direction: column;
	padding-top: 24px;
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
	background: linear-gradient(180deg, #d0d0d0 0%, #8a8a8a 40%, #5a5a5a 100%);
	-webkit-background-clip: text;
	-webkit-text-fill-color: transparent;
	background-clip: text;
}
.hero-sub {
	font-size: 20px;
	font-weight: 500;
	color: #6a6a6a;
	letter-spacing: 2px;
}
.search-wrapper {
	margin-top: 40px;
	display: flex;
	justify-content: center;
}
.search-box {
	position: relative;
	width: 100%;
	max-width: 390px;
}
.search-icon {
	position: absolute;
	left: 0;
	top: 50%;
	transform: translateY(-50%);
	width: 16px;
	height: 16px;
	color: #4a4a4a;
	pointer-events: none;
}
.search-input {
	width: 100%;
	padding: 12px 0 12px 28px;
	background: transparent;
	border: none;
	border-bottom: 1px solid #333;
	color: #d0d0d0;
	font-family: var(--font-mono);
	font-size: 14px;
	outline: none;
	caret-color: #d0d0d0;
	transition: border-color 0.2s;
}
.search-input::placeholder {
	color: #4a4a4a;
}
.search-input::selection {
	background: rgba(255, 255, 255, 0.2);
}
.search-input:focus {
	border-color: #555;
}
.product-section {
	width: 100%;
	margin-top: 60px;
}
.page-header {
	display: flex;
	align-items: center;
	gap: 16px;
	margin-bottom: 12px;
}
.category-tabs {
	display: flex;
	gap: 2px;
}
.tab {
	font-size: 12px;
	padding: 3px 8px;
	border: none;
	background: none;
	color: var(--text-secondary);
	cursor: pointer;
	font-family: var(--font-sans);
	transition: color 0.15s;
}
.tab:hover {
	color: var(--text-primary);
}
.tab.active {
	color: #e0e0e0;
}
.sort-btns {
	display: flex;
	gap: 2px;
	margin-left: auto;
}
.sort-btn {
	font-size: 12px;
	padding: 3px 8px;
	border: none;
	background: none;
	color: var(--text-tertiary);
	cursor: pointer;
	font-family: var(--font-sans);
	border-radius: 3px;
	transition: color 0.15s;
	display: flex;
	align-items: center;
	gap: 2px;
}
.sort-btn:hover {
	color: var(--text-secondary);
}
.sort-btn.active {
	color: #e0e0e0;
}
.sort-arrow {
	font-family: var(--font-mono);
	font-size: 11px;
}
.sub-tabs {
	display: flex;
	gap: 2px;
	margin-bottom: 24px;
	padding-left: 2px;
}
.third-tabs {
	margin-bottom: 16px;
	padding-left: 0;
}
.third-tab {
	opacity: 0.7;
}
.third-tab.active {
	opacity: 1;
}
.sub-tab {
	font-size: 11px;
	padding: 2px 8px;
	border: none;
	background: none;
	color: var(--text-tertiary);
	cursor: pointer;
	font-family: var(--font-sans);
	transition: color 0.15s;
}
.sub-tab:hover {
	color: var(--text-secondary);
}
.sub-tab.active {
	color: #e0e0e0;
}
.search-notice {
	font-size: 12px;
	color: var(--text-secondary);
}
.table-head {
	display: grid;
	grid-template-columns: 1fr 80px 60px 100px 40px;
	align-items: center;
	padding: 8px 8px;
	border-bottom: 1px solid #1a1a1a;
}
.th {
	font-size: 12px;
	font-weight: 500;
	text-transform: uppercase;
	letter-spacing: 0.5px;
	color: var(--text-tertiary);
}
.th-tag {
	text-align: center;
}
.th-stock {
	text-align: right;
	padding-right: 16px;
}
.th-price {
	text-align: right;
}
.row {
	display: grid;
	grid-template-columns: 1fr 80px 60px 100px 40px;
	align-items: center;
	padding: 12px 8px;
	cursor: pointer;
	transition: background 0.1s;
	border-bottom: 1px solid #1a1a1a;
}
.row:hover {
	background: var(--bg-hover);
}
.row-name {
	font-size: 13px;
	color: var(--text-primary);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.row-tag {
	font-size: 12px;
	color: var(--text-tertiary);
	text-align: center;
}
.row-stock {
	font-size: 13px;
	color: var(--text-tertiary);
	text-align: right;
	padding-right: 16px;
}
.row-price {
	font-size: 13px;
	font-weight: 500;
	color: var(--text-primary);
	text-align: right;
}
.add-btn {
	width: 26px;
	height: 26px;
	border: none;
	background: none;
	color: var(--text-tertiary);
	cursor: pointer;
	border-radius: 3px;
	font-size: 16px;
	line-height: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: color 0.15s;
}
.add-btn:hover:not(:disabled) {
	color: #e0e0e0;
}
.add-btn:disabled {
	opacity: 0.3;
	cursor: not-allowed;
}
.mono {
	font-family: var(--font-mono);
}
.mono-dim {
	font-family: var(--font-mono);
	color: var(--text-tertiary);
}
.empty {
	text-align: center;
	color: var(--text-tertiary);
	padding: 80px 0;
	font-size: 13px;
}
.detail-row {
	display: flex;
	justify-content: space-between;
	padding: 6px 0;
	font-size: 13px;
}
.detail-row span:first-child {
	color: var(--text-secondary);
}
.cart-fab {
	position: fixed;
	right: 32px;
	bottom: 32px;
	width: 52px;
	height: 52px;
	border-radius: 50%;
	border: 1px solid #444;
	background: #1a1a1a;
	color: #e0e0e0;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.5);
	transition: all 0.15s;
	z-index: 100;
}
.cart-fab:hover {
	background: #252525;
	border-color: #666;
	transform: scale(1.05);
}
.cart-badge {
	position: absolute;
	top: -4px;
	right: -4px;
	min-width: 20px;
	height: 20px;
	padding: 0 5px;
	border-radius: 10px;
	background: #e0e0e0;
	color: #0a0a0a;
	font-size: 11px;
	font-family: var(--font-mono);
	font-weight: 600;
	display: flex;
	align-items: center;
	justify-content: center;
}
.cart-block {
	margin-bottom: 20px;
	padding-bottom: 16px;
	border-bottom: 1px solid #333;
}
.cart-label {
	font-size: 12px;
	color: var(--text-tertiary);
	margin-bottom: 8px;
}
.cart-empty {
	text-align: center;
	color: var(--text-tertiary);
	padding: 60px 0;
	font-size: 13px;
}
.cart-list {
	display: flex;
	flex-direction: column;
	gap: 4px;
}
.cart-row {
	padding: 12px 0;
	border-bottom: 1px solid #2a2a2a;
}
.cart-row:last-child {
	border-bottom: none;
}
.cart-row-top {
	display: flex;
	justify-content: space-between;
	align-items: baseline;
	margin-bottom: 8px;
}
.cart-row-name {
	font-size: 13px;
	color: var(--text-primary);
}
.cart-row-price {
	font-size: 14px;
	font-weight: 500;
	color: var(--text-primary);
}
.cart-row-ctl {
	display: flex;
	align-items: center;
	gap: 8px;
}
.qty {
	width: 120px;
}
.rm {
	background: none;
	border: none;
	font-size: 18px;
	color: var(--text-tertiary);
	cursor: pointer;
	margin-left: auto;
}
.rm:hover {
	color: #f56c6c;
}
.cart-footer {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	padding: 16px 20px;
	border-top: 1px solid #333;
	background: #131313;
}
.cart-total {
	display: flex;
	justify-content: space-between;
	align-items: baseline;
	margin-bottom: 12px;
}
.total-label {
	font-size: 13px;
	color: var(--text-secondary);
}
.total-price {
	font-size: 20px;
	font-weight: 600;
	color: var(--text-primary);
	letter-spacing: -0.5px;
}
.submit {
	width: 100%;
	padding: 10px 0;
	background: #e0e0e0;
	border: none;
	color: #0a0a0a;
	font-size: 14px;
	font-weight: 600;
	cursor: pointer;
	font-family: var(--font-sans);
	transition: opacity 0.15s;
}
.submit:hover:not(:disabled) {
	opacity: 0.9;
}
.submit:disabled {
	opacity: 0.3;
	cursor: default;
}
</style>

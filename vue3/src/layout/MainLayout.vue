<template>
	<div class="shell">
		<header class="topbar">
			<router-link
				to="/dashboard"
				class="logo-link"
			>
				<pre class="logo">
        ██████╗ ███████╗
       ██╔════╝ ██╔════╝
         ██║  ███████╗
         ██║  ╚════██║
       ╚██████╗ ███████║
        ╚═════╝ ╚══════╝
        </pre
				>
			</router-link>
			<nav class="nav">
				<router-link
					to="/dashboard"
					class="nav-link"
					:class="{ active: $route.path === '/dashboard' }"
					>主页</router-link
				>
				<router-link
					to="/orders"
					class="nav-link"
					:class="{ active: $route.path === '/orders' }"
					>订单</router-link
				>
				<router-link
					v-if="role === 'staff'"
					to="/customers"
					class="nav-link"
					:class="{ active: $route.path === '/customers' }"
					>客户</router-link
				>
<router-link
				v-if="role === 'staff'"
				to="/products"
				class="nav-link"
				:class="{ active: $route.path === '/products' }"
				>商品</router-link
			>
			<router-link
				v-if="role === 'staff'"
				to="/stats"
				class="nav-link"
				:class="{ active: $route.path === '/stats' }"
				>统计</router-link
			>
			<el-dropdown
					trigger="click"
					popper-class="nav-drop"
				>
					<span class="nav-user">{{ name }} ▾</span>
					<template #dropdown>
						<el-dropdown-menu>
							<el-dropdown-item @click="openSettings"
								>设置</el-dropdown-item
							>
							<el-dropdown-item
								divided
								@click="logout"
								>登出</el-dropdown-item
							>
						</el-dropdown-menu>
					</template>
				</el-dropdown>
			</nav>
		</header>

		<main class="main">
			<router-view />
		</main>

		<el-dialog
			v-model="settingsVisible"
			title="账户设置"
			width="440px"
		>
			<el-tabs v-model="settingsTab">
				<el-tab-pane
					label="基本信息"
					name="profile"
				>
					<el-form
						:model="profileForm"
						label-width="72px"
						v-if="settingsTab === 'profile'"
					>
						<template v-if="role === 'staff'">
							<el-form-item label="用户名"
								><el-input v-model="profileForm.username"
							/></el-form-item>
							<el-form-item label="姓名"
								><el-input v-model="profileForm.staff_name"
							/></el-form-item>
						</template>
						<template v-else>
							<el-form-item label="姓名"
								><el-input v-model="profileForm.customer_name"
							/></el-form-item>
							<el-form-item label="手机号"
								><el-input
									v-model="profileForm.phone"
									maxlength="11"
							/></el-form-item>
							<el-form-item label="收货地址"
								><el-input v-model="profileForm.address"
							/></el-form-item>
						</template>
						<el-form-item
							><el-button
								type="primary"
								:loading="profileSaving"
								@click="saveProfile"
								size="small"
								>保存</el-button
							></el-form-item
						>
					</el-form>
				</el-tab-pane>
				<el-tab-pane
					label="修改密码"
					name="pwd"
				>
					<el-form
						:model="pwdForm"
						label-width="72px"
						v-if="settingsTab === 'pwd'"
					>
						<el-form-item label="原密码"
							><el-input
								v-model="pwdForm.old_password"
								type="password"
								show-password
						/></el-form-item>
						<el-form-item label="新密码"
							><el-input
								v-model="pwdForm.new_password"
								type="password"
								show-password
						/></el-form-item>
						<el-form-item
							><el-button
								type="primary"
								:loading="pwdSaving"
								@click="savePassword"
								size="small"
								>修改密码</el-button
							></el-form-item
						>
					</el-form>
				</el-tab-pane>
			</el-tabs>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { me, updateProfile, changePassword } from "../api";

const router = useRouter();
const role = computed(() => localStorage.getItem("role") || "");
const name = computed(() => localStorage.getItem("name") || "");

const logout = () => {
	localStorage.removeItem("token");
	localStorage.removeItem("role");
	localStorage.removeItem("name");
	localStorage.removeItem("uid");
	router.push("/login");
};

const settingsVisible = ref(false);
const settingsTab = ref("profile");
const profileForm = ref({
	customer_name: "",
	phone: "",
	address: "",
	staff_name: "",
	username: "",
});
const profileSaving = ref(false);
const pwdForm = ref({ old_password: "", new_password: "" });
const pwdSaving = ref(false);

const openSettings = async () => {
	settingsTab.value = "profile";
	try {
		const { data } = await me();
		profileForm.value = data || {};
	} catch {}
	pwdForm.value = { old_password: "", new_password: "" };
	settingsVisible.value = true;
};

const saveProfile = async () => {
	profileSaving.value = true;
	try {
		const payload = {};
		if (role.value === "staff") {
			if (profileForm.value.username)
				payload.username = profileForm.value.username;
			if (profileForm.value.staff_name)
				payload.staff_name = profileForm.value.staff_name;
		} else {
			if (profileForm.value.customer_name)
				payload.customer_name = profileForm.value.customer_name;
			if (profileForm.value.phone)
				payload.phone = profileForm.value.phone;
			if (profileForm.value.address)
				payload.address = profileForm.value.address;
		}
		await updateProfile(payload);
		// 更新 localStorage 中的 name
		const newName =
			role.value === "staff" ? payload.staff_name : payload.customer_name;
		if (newName) localStorage.setItem("name", newName);
		ElMessage.success("已保存");
	} catch {
	} finally {
		profileSaving.value = false;
	}
};

const savePassword = async () => {
	if (!pwdForm.value.old_password || !pwdForm.value.new_password)
		return ElMessage.warning("请填写完整");
	pwdSaving.value = true;
	try {
		await changePassword({
			old_password: pwdForm.value.old_password,
			new_password: pwdForm.value.new_password,
		});
		ElMessage.success("密码已修改");
		pwdForm.value = { old_password: "", new_password: "" };
	} catch {
	} finally {
		pwdSaving.value = false;
	}
};
</script>

<style scoped>
.shell {
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

.topbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 48px;
	padding: 0 24px;
	border-bottom: 1px solid #262626;
	flex-shrink: 0;
}

.logo-link {
	text-decoration: none;
	cursor: pointer;
}

.logo {
	font-family: var(--font-mono);
	font-size: 18px;
	line-height: 1.25;
	margin: 0;
	margin-left: -16px;
	letter-spacing: 0;
	background: linear-gradient(180deg, #d0d0d0 0%, #8a8a8a 40%, #5a5a5a 100%);
	-webkit-background-clip: text;
	-webkit-text-fill-color: transparent;
	background-clip: text;
	display: inline-block;
	transform: scale(0.22);
	transform-origin: left center;
	flex-shrink: 0;
}

.nav {
	display: flex;
	align-items: center;
	gap: 0;
	flex-shrink: 0;
}
.nav-link {
	font-size: 13px;
	font-weight: 400;
	padding: 4px 8px;
	color: #5a5a5a;
	text-decoration: none;
	transition: color 0.15s;
	cursor: pointer;
}
.nav-link:hover {
	color: #e0e0e0;
}
.nav-link.active {
	color: #e0e0e0;
}
.nav-user {
	font-size: 13px;
	font-weight: 400;
	color: #5a5a5a;
	padding: 4px 8px;
	margin-left: 8px;
	cursor: pointer;
	transition: color 0.15s;
}
.nav-user:hover {
	color: #e0e0e0;
}
.mono-dim {
	color: var(--text-secondary);
}

.main {
	flex: 1;
	max-width: 1280px;
	width: 100%;
	margin: 0 auto;
	padding: 32px 24px 64px;
}
</style>

<style>
.nav .el-dropdown:focus {
	outline: none;
}
.el-dropdown__popper {
	outline: none !important;
}
.nav-drop {
	border: 1px solid #1a1a1a !important;
	background: #111 !important;
	border-radius: 6px !important;
}
.nav-drop .el-dropdown-menu__item {
	color: #8a8a8a !important;
	font-size: 13px;
}
.nav-drop .el-dropdown-menu__item:hover,
.nav-drop .el-dropdown-menu__item:focus {
	color: #e0e0e0 !important;
	background: #161616 !important;
}
</style>

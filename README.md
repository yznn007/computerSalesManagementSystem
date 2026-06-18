# 电脑销售管理系统

基于 Spring Boot + Vue 3 的电脑销售管理平台，支持笔记本、台式机整机、DIY 配件的销售与库存管理。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Axios |
| 后端 | Java 21 + Spring Boot 4.0.7 + MyBatis |
| 鉴权 | JWT Bearer token（双角色：customer / staff） |
| 数据库 | MySQL 8.x (InnoDB) |

## 项目结构

```
├── SpringBoot/          # 后端
├── vue3/                # 前端
├── sql/                 # 数据库脚本
├── docs/                # 技术文档
├── er-diagram.drawio    # E-R 图（draw.io）
├── er-diagram.png       # E-R 图预览
├── AGENTS.md            # 开发指南（OpenCode）
└── README.md
```

## 快速开始

```bash
# 数据库初始化
mysql -u root -proot --default-character-set=utf8mb4
source sql/01_schema.sql
source sql/02_indexes.sql
source sql/03_procedures.sql
source sql/04_test_data.sql
source sql/05_status_procedures.sql   # 订单状态机（付款/发货/取消/退货）

# 后端启动
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml spring-boot:run

# 前端启动
cd vue3
npm install
npm run dev
```

> `--default-character-set=utf8mb4` 必须指定，否则中文数据会乱码。

## 分支策略

| 分支 | 说明 |
|------|------|
| `main` | 稳定发布版 |
| `dev` | 开发主分支 |
| `feat/*` | 功能特性分支 |

## 数据库设计

9 张表，符合 3NF，关键设计：

- **双角色账号**：`Customer`（手机号登录，自助下单）与 `Staff`（销售员，管理商品/客户/订单），密码均存 BCrypt 哈希
- **垂直拆分**：Product 与 Laptop_Detail / Desktop_Detail 1:1 拆分
- **悲观锁**：下单时 `SELECT ... FOR UPDATE` 防止超卖
- **存储过程**：`sp_create_order` 支持 JSON 多商品下单；`sp_update_order_status` 实现订单状态机（付款/发货/取消/退货，取消与退货回补库存）
- **审计字段**：所有表含 `created_at` / `updated_at`

详见 [docs/database-technical-document.md](docs/database-technical-document.md)

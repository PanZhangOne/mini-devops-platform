# mini-devops-platform

一个基于 Spring Cloud 的迷你 DevOps 平台，采用多模块后端 + 前端分离架构。

## 项目结构

- `devops-common`：公共模块
- `devops-gateway`：网关服务
- `devops-auth-service`：认证服务
- `devops-work-service`：工作台/工作项服务
- `devops-code-service`：代码相关服务
- `devops-release-service`：发布服务
- `devops-pipeline-service`：流水线服务
- `devops-runner-service`：任务执行服务
- `devops-frontend`：前端应用（Vite + React + TypeScript）
- `scripts/`：镜像构建、推送、容器启动脚本

## 技术栈

- Java 21
- Spring Boot 3.5.x
- Spring Cloud 2025.x
- Spring Cloud Alibaba 2025.x
- MyBatis-Plus
- PostgreSQL
- Flyway
- React 19 + Vite + TypeScript

## 环境要求

- JDK 21
- Maven 3.9+
- Node.js 20+
- pnpm 9+
- Docker / Docker Compose

## 本地开发

### 1. 启动 PostgreSQL

在项目根目录执行：

```bash
docker compose up -d postgres
```

默认配置：

- Host: `127.0.0.1`
- Port: `5432`
- User: `postgresql`
- Password: `postgresql`
- Database: `mini_devops_work`

### 2. 构建后端

在项目根目录执行：

```bash
mvn clean package
```

如需跳过测试：

```bash
mvn clean package -DskipTests
```

### 3. 启动前端

```bash
cd devops-frontend
pnpm install
pnpm dev
```

## Docker 脚本

项目提供了常用脚本（位于 `scripts/`）：

### 构建镜像

```bash
bash scripts/build-images.sh 1.0.0
```

### 推送镜像

```bash
bash scripts/push-images.sh 1.0.0
```

可通过环境变量覆盖默认镜像仓库：

- `REGISTRY`（默认 `192.168.199.107:8812`）
- `PROJECT`（默认 `mini-devops`）

示例：

```bash
REGISTRY=your.registry:5000 PROJECT=mini-devops bash scripts/push-images.sh 1.0.0
```

### 运行容器

```bash
bash scripts/run-containers.sh 1.0.0
```

可通过环境变量覆盖：

- `NACOS_SERVER_ADDR`（默认 `192.168.199.107:8848`）
- `SPRING_PROFILES_ACTIVE`（默认 `dev`）

示例：

```bash
NACOS_SERVER_ADDR=127.0.0.1:8848 SPRING_PROFILES_ACTIVE=dev bash scripts/run-containers.sh 1.0.0
```

## 常见问题

1. Java 版本不匹配

请确认本地 `java -version` 为 21。

2. 前端依赖安装慢

可配置国内镜像源后执行 `pnpm install`。

3. Docker 运行失败

请确认 Docker 已启动，并且本机端口（80、9000、9001、9010、9020、9030）未被占用。

## 说明

- 根目录 `Dockerfile` 用于构建各后端服务镜像（通过 `JAR_FILE` 构建参数指定具体模块产物）。
- `devops-frontend/Dockerfile` 用于前端镜像构建。

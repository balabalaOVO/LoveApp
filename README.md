# Yu AI Agent

全栈 AI 智能体平台，包含**恋爱大师**情感咨询助手和 **YuManus 超级智能体**两大 AI 应用。支持 ReAct 自主推理与工具调用、RAG 知识检索增强、多轮对话记忆、用户认证、云端文件存储等功能。

## 功能特性

### 🤖 恋爱大师（Love App）
- 角色扮演恋爱心理咨询师"军师"，提供情感建议
- RAG 增强：基于向量知识库检索恋爱相关文档（单身篇/恋爱篇/已婚篇）
- SSE 流式对话，支持多轮会话记忆
- 多模态输出：生成图片、PDF 报告等

### 🧠 YuManus 超级智能体
- ReAct（Reasoning + Acting）循环：自主思考 → 调用工具 → 观察结果 → 继续推理
- 最多 10 步自动执行，前端实时展示每步的思考与执行结果
- 9 大工具能力，覆盖搜索、文件、终端、邮件等场景

### 🔧 工具系统
| 工具 | 功能 |
|---|---|
| 网页搜索 | 百度搜索（SearchAPI） |
| 网页抓取 | Jsoup 抓取网页内容 |
| 图片搜索 | Pexels API 搜索图片 |
| PDF 生成 | iText 生成图文 PDF，上传至腾讯云 COS |
| 资源下载 | 下载网络资源至 COS |
| 终端操作 | 执行 Shell / CMD 命令 |
| 文件操作 | 本地文件读写 |
| 邮件发送 | QQ SMTP 发送邮件 |
| 日期时间 | 获取当前日期时间 |

### 🔐 用户认证
- JWT 令牌认证，BCrypt 密码加密
- 注册 / 登录接口，前端路由守卫

### 💬 对话管理
- MySQL 持久化存储对话历史，支持查看和删除
- 会话列表侧边栏，可切换历史对话

### 📦 云端存储
- 腾讯云 COS 对象存储，按用户和会话组织文件
- 私有文件签名 URL 下载

### 🔌 MCP 协议
- 集成 MCP Client，支持 Stdio / SSE 传输
- 内置图片搜索 MCP Server（`yu-image-search-mcp-server`）

## 技术栈

### 后端
| 技术 | 版本 / 说明 |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.5 |
| Spring AI | 1.0.0 |
| LangChain4j | 1.0.0-beta3 |
| MyBatis-Plus | 3.5.7 |
| MySQL | 主数据库（用户、会话、消息） |
| PostgreSQL + pgvector | 向量数据库（RAG 检索） |
| DashScope | 阿里云模型服务（DeepSeek / Qwen） |
| JWT | jjwt 0.12.6 |
| 腾讯云 COS | 对象存储 |
| Knife4j | 4.4.0（API 文档） |

### 前端
| 技术 | 说明 |
|---|---|
| Vue 3 | Composition API |
| Vue Router 4 | 路由管理 |
| Vite 7 | 构建工具 |
| Axios | HTTP 请求 + SSE 流式 |
| Nginx | 生产环境反向代理 |

### 部署
- Docker 多阶段构建（后端 Maven 构建 + 前端 Node 构建 → Nginx）
- Nginx 反向代理，配置 SSE 长连接支持

## 项目结构

```
yu-ai-agent/
├── src/main/java/com/yupi/yuaiagent/
│   ├── Agent/              # Agent V1（完整功能版）
│   ├── Agent2/             # Agent V2（简化版）
│   ├── App/                # AI 应用（LoveApp 等）
│   ├── Advisor/            # Spring AI Advisors（日志、RAG 等）
│   ├── chatMemory/         # 对话记忆（文件 / MySQL）
│   ├── config/             # 配置类（CORS、JWT、COS）
│   ├── controller/         # REST 控制器
│   ├── entity/             # 实体类
│   ├── mapper/             # MyBatis-Plus Mapper
│   ├── model/              # DTO
│   ├── rag/                # RAG 向量存储配置
│   ├── Service/            # 业务服务
│   ├── tools/              # 工具定义
│   └── resources/
│       ├── application.yml           # 默认配置（prod）
│       ├── application-local.yml     # 本地开发
│       └── application-prod.yml      # 生产环境
├── yu-ai-frontend/         # Vue 3 前端
│   └── src/
│       ├── api/            # API 封装
│       ├── components/     # 组件（ChatWindow、Sidebar）
│       ├── router/         # 路由
│       ├── store/          # 状态管理
│       └── views/          # 页面
├── yu-image-search-mcp-server/  # 图片搜索 MCP Server
├── docs/                   # 项目文档
└── Dockerfile              # 后端 Docker 构建
```

## 快速开始

### 环境要求
- JDK 21+
- Maven 3.9+
- Node.js 20+
- MySQL 8.0+
- PostgreSQL + pgvector 扩展（可选，用于向量检索）

### 后端启动

1. **克隆项目**
```bash
git clone <repo-url>
cd yu-ai-agent
```

2. **配置数据库**

创建 MySQL 数据库并执行初始化 SQL（或在首次启动时自动建表）。修改 `application-local.yml` 中的数据库连接信息。

3. **配置 API Key**

创建 `src/main/resources/application-local.yml`，填入必要配置：
```yaml
spring:
  ai:
    dashscope:
      api-key: your-dashscope-api-key
  datasource:
    url: jdbc:mysql://localhost:3306/yu_ai_agent
    username: root
    password: your-password
pexels:
  api-key: your-pexels-api-key
searchapi:
  api-key: your-searchapi-key
```

> 在 `application-local.yml` 中配置的密钥不会被 Git 追踪（已在 `.gitignore` 中排除）。

4. **启动后端**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

后端默认运行在 `http://localhost:8123`，API 文档地址 `http://localhost:8123/doc.html`。

### 前端启动

```bash
cd yu-ai-frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，自动代理 API 请求到后端 `8123` 端口。

### Docker 部署

```bash
# 后端
docker build -t yu-ai-agent .
docker run -p 8123:8123 -e DASHSCOPE_API_KEY=xxx yu-ai-agent

# 前端
cd yu-ai-frontend
docker build -t yu-ai-frontend .
docker run -p 80:80 yu-ai-frontend
```

## API 概览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/ai/love_app/chat/sse` | 恋爱大师流式对话 |
| GET | `/api/ai/manus/chat` | 智能体流式对话 |
| GET | `/api/ai/conversations` | 获取会话列表 |
| DELETE | `/api/ai/conversations/{chatKey}` | 删除会话 |
| GET | `/api/ai/file/download` | 下载 COS 文件 |

## 相关文档

- [Agent 框架教程](docs/Agent目录教程.md)
- [对话历史持久化](docs/conversation-history-persistence.md)
- [COS 文件上传下载](docs/cos-file-upload-download.md)
- [Swagger 接口调试](<docs/Swagger 接口调试方法>)

## License

MIT

# AI_QA_System

一个基于 Spring Boot 3 的 AI 知识问答后端项目，围绕 `Qwen 接入结构 + RAG 检索优化 + Agent 编排` 设计，适合继续扩展成企业知识库问答、内部文档助手或业务 Copilot 服务。

项目当前默认以本地可演示链路为主，优先验证查询重写、知识切分、混合检索、Agent 路由、会话记忆与可观测性，而不是依赖真实线上模型。

## 功能概览

- Qwen 兼容 OpenAI 风格接口接入结构
- 查询重写，提升用户问题表达质量
- 语义分块，增强知识切片质量
- 混合检索：关键词检索 + 向量检索
- RRF 融合排序，提升复杂问题召回稳定性
- Agent 路由与 Tool Calling
- 会话记忆与最近轨迹记录
- 知识命中率与检索指标可观测性
- 内置简单网页前端，可直接导入知识和提问
- 默认 `mock-enabled: true`，便于本地直接演示整条问答链路

## 项目亮点

- 问答入口按问题长度、关键词和 `forceRetrieval` 参数在 `clarify`、`direct_qa`、`retrieval_qa` 之间路由。
- RAG 链路包含查询重写、文本分块、关键词检索、向量检索、RRF 融合与证据回传。
- Agent 工具当前包含知识检索、会话记忆、澄清提问，便于后续继续扩展更多业务工具。
- 可观测性服务会记录问题总量、检索问答量、知识命中率、平均召回块数、路由分布与工具调用次数。
- 系统自带前端页面，能直接演示知识导入、提问、证据查看、轨迹刷新与指标概览。

## 技术栈

- Java 17+
- Spring Boot 3.3.4
- Spring Web
- Spring WebFlux `WebClient`
- Spring Validation
- Spring Actuator
- Spring Cache
- Caffeine

## 项目结构

```text
src/main/java/com/aiqa/system
├─ agent/                 Agent Tool 抽象
├─ config/                Qwen / RAG / Bean 配置
├─ controller/            知识导入、问答、可观测性接口
├─ dto/                   请求与响应对象
├─ model/                 文档块、轨迹、检索命中、会话消息
├─ repository/            内存知识库仓储
└─ service/               路由、检索、记忆、观测、问答编排

src/main/resources
├─ application.yml        项目配置
└─ static/                内置前端页面
```

## 核心流程

1. 通过 `/api/knowledge/ingest` 导入知识文档。
2. 文档进入语义分块服务，生成多个知识块。
3. 每个知识块生成向量并存入内存仓储。
4. 用户通过 `/api/qa/ask` 提问。
5. 系统先完成路由判断、查询重写、会话记忆读取与工具规划。
6. 当路由命中 `retrieval_qa` 时，执行混合检索并拼装证据。
7. 最终调用 Qwen 兼容接口或 mock 回答逻辑返回结果。
8. 同步记录最近轨迹和观测指标，供页面或接口查看。

## 快速开始

### 1. 进入项目目录

```powershell
cd C:\Users\爆米花\IdeaProjects\untitled\AI_QA_System
```

### 2. 配置 Java

```powershell
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

### 3. 打包项目

```powershell
cd C:\Users\爆米花\IdeaProjects\untitled\AI_QA_System
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd' package
```

### 4. 启动项目

当前推荐直接使用 `java -jar` 启动：

```powershell
cd C:\Users\爆米花\IdeaProjects\untitled\AI_QA_System
java -jar .\target\ai-qa-system-1.0.0-SNAPSHOT.jar
```

启动后访问：

- 前端页面：`http://localhost:8090`
- 健康检查：`http://localhost:8090/actuator/health`

## 页面说明

首页 `http://localhost:8090` 支持：

- 导入知识文档
- 发起问答
- 查看工具调用结果
- 查看检索证据
- 查看最近一次 Agent 路由
- 查看知识命中率与检索指标
- 填充演示知识并快速体验默认链路

## 常用接口

### 知识库接口

- `POST /api/knowledge/ingest`

### 问答接口

- `POST /api/qa/ask`
- `GET /api/qa/trace`

### 可观测性接口

- `GET /api/observability/metrics`
- `GET /api/observability/traces`

## 调用示例

### 导入知识

```powershell
Invoke-RestMethod -Method Post http://localhost:8090/api/knowledge/ingest `
  -ContentType 'application/json' `
  -Body '{"title":"RAG介绍","content":"RAG 通过检索外部知识增强大模型回答质量。查询重写、语义分块、混合检索和重排是常见优化手段。","metadata":{"domain":"ai"}}'
```

### 发起提问

```powershell
Invoke-RestMethod -Method Post http://localhost:8090/api/qa/ask `
  -ContentType 'application/json' `
  -Body '{"question":"为什么 RAG 需要查询重写？","sessionId":"demo-session","forceRetrieval":true}'
```

### 查看最近一次轨迹

```powershell
Invoke-RestMethod -Method Get http://localhost:8090/api/qa/trace
```

### 查看观测指标

```powershell
Invoke-RestMethod -Method Get http://localhost:8090/api/observability/metrics
```

## 配置说明

配置文件位置：

- `src/main/resources/application.yml`

当前默认配置重点包括：

- `server.port`
- `ai.qwen.base-url`
- `ai.qwen.api-key`
- `ai.qwen.chat-model`
- `ai.qwen.embedding-model`
- `ai.qwen.timeout-seconds`
- `ai.qwen.mock-enabled`
- `ai.rag.rewrite-enabled`
- `ai.rag.chunk-size`
- `ai.rag.chunk-overlap`
- `ai.rag.keyword-top-k`
- `ai.rag.vector-top-k`
- `ai.rag.fused-top-k`

说明：

- 当前默认是 `mock-enabled: true`
- 这意味着系统会优先演示 RAG、Agent、记忆和可观测性链路
- 当前向量能力使用项目内的轻量嵌入实现，方便本地无依赖演示
- 如果后续要切到真实 Qwen，只需要补充可用模型地址、API Key，并改为 `mock-enabled: false`

## 当前实现说明

- 知识库默认是内存实现，重启后数据不会持久化。
- 会话记忆默认保留最近 8 条消息轨迹。
- 最近观测轨迹默认保留 10 条。
- Mock 模式下，回答内容由演示逻辑返回，但检索、路由、工具执行和轨迹统计仍会完整工作。

## 测试

```powershell
cd C:\Users\爆米花\IdeaProjects\untitled\AI_QA_System
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd' test
```

## 常见问题

### `spring-boot:run` 无法正常启动

当前环境下 `spring-boot:run` 可能存在主类加载问题，推荐直接使用：

```powershell
cd C:\Users\爆米花\IdeaProjects\untitled\AI_QA_System
java -jar .\target\ai-qa-system-1.0.0-SNAPSHOT.jar
```

### 为什么系统没有直接调用真实大模型

当前默认使用 mock 模式，优先验证：

- 查询重写
- 语义分块
- 混合检索
- Agent Tool Calling
- 会话记忆
- 可观测性指标

如果需要切换到真实 Qwen，只需修改 `application.yml` 中的模型地址、Key 和 `mock-enabled` 配置。

### 为什么重启后知识数据没了

当前仓储实现是 `InMemoryKnowledgeRepository`，适合演示，不做持久化存储。后续如果要接企业知识库，可以替换为数据库、向量库或对象存储方案。

# EasyChat - 企业级即时通讯系统

## 📖 项目简介

EasyChat是一个基于微服务架构的企业级即时通讯系统，采用Spring Cloud Alibaba技术栈构建，提供完整的实时聊天、用户管理、群组管理等功能。系统采用模块化设计，支持高并发、高可用的实时通信场景。

## 🏗️ 系统架构

### 技术栈
- **后端框架**: Spring Boot 2.7.12 + Spring Cloud 2021.0.3
- **服务治理**: Spring Cloud Alibaba 2021.0.4.0 + Nacos
- **RPC框架**: Apache Dubbo 3.3.0
- **数据库**: MySQL 8.0 + MyBatis-Plus 3.4.3
- **缓存**: Redis + Redisson 3.13.6
- **消息队列**: Apache Kafka
- **实时通信**: Netty 4.1.42 + WebSocket
- **网关**: Spring Cloud Gateway
- **开发语言**: Java 11

### 微服务模块

| 模块 | 端口 | Dubbo端口 | 功能描述 |
|------|------|-----------|----------|
| **gateway** | 8080 | - | API网关，统一入口，认证鉴权 |
| **user-service** | 8081 | 20885 | 用户服务，注册登录，用户信息管理 |
| **contact-service** | 8082 | 20883 | 联系人服务，好友关系，群组成员管理 |
| **group-service** | 8083 | 20882 | 群组服务，群组创建维护，群聊管理 |
| **chat-service** | 8087 | 20884 | 聊天服务，实时消息处理，会话管理 |
| **admin-service** | 8084 | - | 管理服务，系统监控，后台管理 |
| **common** | - | - | 公共模块，工具类，DTO定义 |

## 📁 项目结构

```
EasyChat/
├── gateway/                 # API网关服务
│   ├── src/main/java/com/easychat/gateway/
│   │   ├── filter/AuthGlobalFilter.java     # 全局认证过滤器
│   │   ├── route/DynamicRouteLoader.java    # 动态路由加载器
│   │   └── config/AuthProperties.java       # 认证配置
├── user-service/           # 用户服务
├── contact-service/        # 联系人服务  
├── group-service/         # 群组服务
├── chat-service/          # 聊天服务
├── admin-service/         # 管理服务
├── common/                # 公共模块
├── sqls/                  # 数据库脚本
├── file/                  # 文件存储目录
├── logs/                  # 日志目录
└── pom.xml               # Maven父项目配置
```

## 🚀 快速开始

### 环境要求
- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Kafka 2.8+
- Nacos 2.0+

### 数据库初始化
执行 `sqls/` 目录下的SQL脚本创建数据库和表结构：

```sql
-- 创建数据库
CREATE DATABASE user CHARACTER SET utf8mb4;
CREATE DATABASE contact CHARACTER SET utf8mb4;
CREATE DATABASE `group` CHARACTER SET utf8mb4;
CREATE DATABASE chat CHARACTER SET utf8mb4;
CREATE DATABASE admin CHARACTER SET utf8mb4;

-- 执行对应SQL文件初始化表结构
-- user_info.sql, contact_user_info.sql, group_info.sql, chat_session.sql等
```

### 服务配置
1. **Nacos配置中心**: 172.23.80.100:8848
2. **Redis配置**: 172.23.80.100:6379
3. **MySQL配置**: 172.23.80.100:3306
4. **Kafka配置**: 172.23.80.100:9094

### 启动顺序

```bash
# 1. 编译项目
mvn clean install

# 2. 启动基础设施服务
# Redis, MySQL, Kafka, Nacos 等基础设施服务
# 注意：确保所有服务启动前，相关配置文件中的数据库连接信息、Redis地址、Nacos地址等与实际环境一致。
# 另外，确保Nacos服务启动后，注册中心中包含所有必要的配置项。动态路由配置在gateway模块中。
# 注意：如果使用了自定义的Nacos配置分组，确保在gateway模块的application.yml中指定了正确的group。


# 3. 启动微服务（按依赖顺序）
# 启动 user-service
mvn spring-boot:run -pl user-service

# 启动 contact-service  
mvn spring-boot:run -pl contact-service

# 启动 group-service
mvn spring-boot:run -pl group-service

# 启动 chat-service
mvn spring-boot:run -pl chat-service

# 启动 gateway
mvn spring-boot:run -pl gateway

# 启动 admin-service
mvn spring-boot:run -pl admin-service
```

## 🔧 核心功能

### 用户管理 (user-service)
- 用户注册、登录、身份验证
- 用户信息维护（昵称、头像、状态）
- Token认证机制
- 管理员权限管理
- 用户状态同步

### 实时通信 (chat-service)
- WebSocket长连接管理
- 一对一聊天、群组聊天
- 消息发送、接收、存储
- 在线状态管理
- 心跳检测机制
- Netty高性能网络通信

### 联系人管理 (contact-service)
- 好友关系管理
- 好友申请、审批流程
- 联系人分组管理
- 黑名单功能
- 好友推荐

### 群组管理 (group-service)
- 群组创建、维护、解散
- 群成员管理（添加、移除、权限设置）
- 群公告、群文件管理
- 群聊会话管理
- 群权限控制

### API网关 (gateway)
- 统一API入口
- JWT Token认证
- 动态路由配置
- 请求限流
- 跨域处理

## 🔒 安全特性

### 认证鉴权
- JWT Token认证机制
- API网关统一鉴权
- 路径权限控制（includePaths/excludePaths/adminPaths）
- 管理员权限分级

### 数据安全
- 密码加密存储（MD5 + Salt）
- 敏感信息脱敏
- SQL注入防护

### 通信安全
- WebSocket安全握手
- 消息加密传输
- 连接超时控制

## 📊 性能优化

### 高可用设计
- 微服务架构，服务独立部署
- 负载均衡，支持水平扩展
- 服务熔断、降级机制
- 数据库读写分离

### 性能优化
- Redis缓存热点数据
- 数据库连接池优化
- 消息异步处理（Kafka）
- Netty高性能网络通信
- 连接池管理

## 🛠️ 开发指南

### 代码规范
- 统一使用Lombok简化代码
- MyBatis-Plus代码生成器
- 统一异常处理机制
- 标准化API响应格式

### 接口文档
各服务提供RESTful API接口，可通过Swagger UI访问：
- http://localhost:8080/swagger-ui.html (网关聚合)

### 数据库设计
系统采用分库设计，每个微服务拥有独立的数据库：
- `user`数据库：用户信息表
- `contact`数据库：联系人关系表
- `group`数据库：群组信息表
- `chat`数据库：聊天记录表

## 🔄 核心业务流程

### 用户注册流程
1. 客户端发送注册请求到gateway
2. gateway路由到user-service
3. user-service验证数据并创建用户
4. 返回注册结果

### 消息发送流程
1. 客户端通过WebSocket连接chat-service
2. 发送消息到chat-service
3. chat-service处理消息并存储
4. 通过Kafka异步处理消息分发
5. 推送到目标用户

### 好友添加流程
1. 用户A发送好友申请到contact-service
2. contact-service创建申请记录
3. 用户B收到申请通知
4. 用户B审批申请
5. 建立好友关系

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 邮箱: [项目维护者邮箱]
- 问题反馈: [GitHub Issues]

---

**EasyChat文档** 

https://pppr8ikl5f.feishu.cn/wiki/ZGpZwdgAFiDxAvk928Lct7D6ntG
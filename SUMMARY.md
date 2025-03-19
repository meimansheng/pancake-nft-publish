# PancakeSwap NFT 发布系统

## 项目概述
这是一个为PancakeSwap平台设计的NFT收藏集发布和管理系统。该系统允许用户创建、管理和发布NFT收藏集到PancakeSwap平台。

## 核心功能

### 1. NFT收藏集管理
- 创建新的NFT收藏集
- 查询收藏集信息
- 更新收藏集信息
- 删除收藏集
- 特殊的Bunny NFT收藏集管理

### 2. 安全特性
- 基于令牌的访问控制（x-secure-token）
- 请求频率限制（Rate Limiting）
  - 每分钟最多20个请求
  - 使用Bucket4j实现令牌桶算法

### 3. 异常处理
- 统一的全局异常处理机制
- 支持多种异常类型：
  - 参数验证异常
  - 业务逻辑异常
  - 资源未找到异常
  - 系统异常

## 技术架构

### 1. 后端框架
- Spring Boot
- RESTful API设计
- 分层架构：
  - Controller层：处理HTTP请求
  - Service层：业务逻辑处理
  - Model层：数据模型

### 2. 主要组件
- CollectionController：处理收藏集相关的API请求
- ControllerAdvisor：全局异常处理器
- NFTService：NFT相关业务逻辑处理
- BunnyNFTService：特殊的Bunny NFT处理服务

### 3. 安全机制
- 请求认证：通过secure-token进行API访问控制
- 频率限制：使用令牌桶算法防止API滥用
- 参数验证：使用@Valid注解进行请求参数验证

## API接口

### 1. 收藏集管理接口
POSTgit /api/v1/collections          # 创建新的NFT收藏集
POST /api/v1/bunny/collections/{address}  # 添加Bunny NFT收藏集
DELETE /api/v1/collections/{id}   # 删除指定收藏集

### 2. 请求/响应格式
- 请求头要求：
  - x-secure-token：访问令牌
- 响应格式：
  - 成功：返回200状态码
  - 错误：返回对应的错误状态码和消息

## 部署注意事项
1. 需要配置secure.token环境变量
2. 注意调整请求频率限制的参数
3. 确保相关的NFT服务配置正确

## 最佳实践
1. 所有API请求都需要包含有效的secure-token
2. 注意处理异步操作的异常情况
3. 遵循API请求频率限制
4. 确保数据验证的完整性

## 未来优化方向
1. 添加更详细的API文档
2. 增强监控和日志功能
3. 优化异步处理机制
4. 增加更多的单元测试覆盖
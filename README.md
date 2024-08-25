## 模板特点
### 主流框架 & 特性
- Spring Boot 2.7.x（贼新）
- Spring MVC
- MyBatis + MyBatis Plus 数据访问（开启分页）
- Spring Boot 调试工具和项目处理器
- Spring AOP 切面编程
- Spring Scheduler 定时任务
- Spring 事务注解

### 数据存储

- MySQL 数据库
- Redis 内存数据库
### 工具类
- Easy Excel 表格处理
- Hutool 工具库
- Apache Commons Lang3 工具类
- Lombok 注解

### 业务特性
- 业务代码生成器（支持自动生成 Service、Controller、数据模型代码）
- Spring Session Redis 分布式登录
- 全局请求响应拦截器（记录日志）
- 全局异常处理器
- 自定义错误码
- 封装通用响应类
- 自定义权限注解 + 全局校验
- 全局跨域处理
- 长整数丢失精度解决
- 多环境配置

## 业务功能
- 用户登录、注册、注销
- 支持excel文件上传
- 支持图表自动生成和图表分页查看和搜索
- 支持图表异步生成和图表删除
### 架构设计
- 合理分层
### Redis 分布式登录
1）修改 `application.yml` 的 Redis 配置为你自己的：
```yml
spring:
  redis:
    database: 1
    host: localhost
    port: 6379
    timeout: 5000
    password: 123456
```
2）修改 `application.yml` 中的 session 存储方式：
```yml
spring:
  session:
    store-type: redis
```
3）移除 `MainApplication` 类开头 `@SpringBootApplication` 注解内的 exclude 参数：
修改前：
```java
//@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
```
修改后：
```java
//@SpringBootApplication
```

启动时先执行bimq包中的BiInitMain类


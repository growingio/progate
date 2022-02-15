# Progate

Progate 是一款面向 Pipeline 设计的高拓展网络 API 网关，它能方便地将 gRPC, Dubbo 等接口转换成 GraphQL 与 RESTful 接口。

## 状态

早期的 MVP 阶段，应用于 Growing CDP 产品。

## 功能

- 插件化设计
- 支持 GraphQL
- 支持 OpenAPI v3

## 模块
| 名称        | 功能           |
|:----------|--------------|
| api       | 接口定义         |
| utilities | 工具类          |
| compiler  | 编译器          |
| graphql   | GraphQL 入口实现 |
| grpc      | gRPC 出口实现    |
| core      | 核心（负责组织各个模块） |
| bootstrap | 程序启动器        |

## 上手

1. 将 `conf/gateway.example.yaml` 复制成 `conf/gateway.yaml`。你可以根据自己的需求调整配置文件中的参数。
2. 进入目录 `examples/grpc-example` 运行 gRPC 上游服务。
3. 进入目录 `bootstrap` 运行服务。
4. 访问 GraphQL 或 RESTful 接口。

## 感谢
[Beyond](https://zh.wikipedia.org/wiki/Beyond)

# zthc-script

一个基于 Clojure 的中文脚本语言解析器和执行环境。

## 项目概述

zthc-script 是一个用 Clojure 编写的脚本语言项目，支持中文关键字的编程语法解析。它提供了：

- **脚本语言解析器**: 支持中文函数定义、变量声明等语法
- **TCP 服务器**: 用于处理客户端连接和消息传输
- **nREPL 服务**: 提供远程代码执行能力
- **多协议支持**: 支持 MQTT 和其他网络协议

## 功能特性

### 脚本语言特性
- 中文关键字支持 (函数、变量、常量等)
- 函数定义和调用语法
- 变量声明和赋值
- 参数传递和返回值处理
- 字符串、数字等基本数据类型

### 网络服务特性
- 高性能 TCP 服务器 (基于 Aleph)
- 异步消息处理
- 客户端连接管理
- 日志记录和错误处理

### 开发工具特性
- nREPL 远程交互式开发环境
- 扩展的日志系统
- 实用的宏和工具函数

## 快速开始

### 系统要求

- Java 8 或更高版本
- Leiningen (推荐) 或 Boot

### 安装和运行

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd zthc-script
   ```

2. **安装依赖**
   ```bash
   lein deps
   ```

3. **运行项目**
   ```bash
   lein run
   ```

4. **构建 JAR**
   ```bash
   lein uberjar
   ```

### 使用示例

#### 启动 TCP 服务器
```clojure
(require '[net.zthc.script.tcp-server :as tcp])

;; 启动默认端口 (127.0.0.1:7888)
(def server (tcp/start-server 7888))

;; 启动指定主机和端口
(def server (tcp/start-server 9000 "0.0.0.0"))
```

#### 解析脚本语法
```clojure
(require '[net.zthc.script.parser :as parser])

;; 解析函数调用
(parser/parse-function "@调试输出(asdxx, 123, @调试输出_(321, @到整数(\"777\")));")

;; 解析变量定义
(parser/parse-def-var "变量 a: 199;")

;; 解析函数定义
(parser/parse-def-function "函数 add(a, b){ @返回(a+b) }")
```

#### 使用工具函数
```clojure
(require '[net.zthc.script.util :as util])

;; 日志输出
(util/log :info "Server started on port %d" 7888)

;; 字符串处理
(util/big-first "hello") ; => "Hello"
```

## 脚本语言语法

### 变量声明
```
变量 变量名: 值;
常量 常量名: 值;
```

### 函数定义
```
函数 函数名(参数1, 参数2) {
    函数体
}
```

### 函数调用
```
@函数名(参数1, 参数2);
```

### 支持的数据类型
- 整数: `123`
- 浮点数: `123.45`
- 字符串: `"hello"`, `'hello'`, `「hello」`, `\hello\`

## 项目结构

```
src/net/zthc/script/
├── core.clj           # 主入口
├── parser.clj         # 主解析器
├── parser/
│   ├── atom.clj       # 原子解析
│   ├── string.clj     # 字符串解析
│   ├── number.clj     # 数字解析
│   ├── symbol.clj     # 符号解析
│   └── util.clj       # 解析工具
├── tcp_server.clj    # TCP 服务器
└── util.clj           # 工具函数
```

## 依赖项

- Clojure 1.12.2
- Manifold 0.4.3 (异步流处理)
- Aleph (TCP 服务器)
- Instaparse 1.5.0 (语法解析)
- MQTT Client 1.16
- nREPL 0.2.13 (远程 REPL)

## 开发指南

### 运行测试
```bash
lein test
```

### 检查依赖更新
```bash
lein ancient
```

### 生成依赖图
```bash
lein vizdeps
```

## 技术栈

- **核心语言**: Clojure
- **解析器**: Instaparse (PEG 语法)
- **异步处理**: Manifold
- **网络通信**: Aleph TCP
- **消息队列**: MQTT
- **开发工具**: Leiningen

## 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。

## 贡献指南

欢迎提交 Issue 和 Pull Request。请确保：

1. 代码符合项目的风格规范
2. 添加必要的测试用例
3. 更新相关文档

## 支持

如有问题或建议，请通过以下方式联系：

- 提交 GitHub Issue
- 发送邮件至项目维护者

# zthc-script

一个基于 Clojure 的中文脚本语言解析器和执行环境，支持 SCI (Small Clojure Interpreter) 沙箱执行。

## 项目概述

zthc-script 是一个支持中文关键字的编程语言解析器和执行引擎，提供了完整的语法解析、代码执行、TCP 服务器和 nREPL 远程开发环境。

## 核心功能

### 🚀 中文脚本语言解析与执行
- **函数定义**: `函数 add(a, b) { @返回(a+b) }`
- **变量声明**: `变量 name: "张三";`
- **函数调用**: `@调试输出("hello", 123);`
- **常量定义**: `常量 PI: 3.14;`
- **代码执行**: 基于 SCI 的安全沙箱执行环境

### 🔧 内置函数库
- **输出函数**: `调试输出`, `打印`, `输出`
- **类型转换**: `到整数`, `到字符串`, `到浮点数`
- **数学运算**: `加`, `减`, `乘`, `除`
- **比较操作**: `等于?`, `大于?`, `小于?`, `大于等于?`, `小于等于?`
- **字符串函数**: `长度`, `连接`, `包含?`, `为空?`, `不为空?`

### 🌐 网络服务
- **TCP 服务器** (端口 7888) - 处理客户端连接和脚本执行
- **nREPL 服务器** (端口 7889) - 远程交互式开发环境
- **异步消息处理** - 基于 Aleph 和 Manifold
- **沙箱执行** - 安全的代码执行环境

### 📝 支持的数据类型
- **字符串**: `"text"`, `'text'`, `「text」`, `\text\`
- **数字**: 整数 `123`, 浮点数 `3.14`, 科学计数法 `1.23e4`
- **布尔值**: `true/false`, `真/假`
- **标识符**: 支持中英文混合命名

## 快速开始

### 系统要求
- Java 8 或更高版本
- Leiningen

### 安装和运行

```bash
# 克隆项目
git clone <repository-url>
cd zthc-script

# 安装依赖
lein deps

# 运行测试
lein test

# 启动服务
lein run

# 构建 JAR
lein uberjar
```

### 演示执行引擎

```bash
# 运行演示脚本
lein run -m clojure.main demo.clj
```

## 使用示例

### 脚本执行

```clojure
(require '[net.zthc.script.parser :as parser])
(require '[net.zthc.script.executor :as executor])

;; 执行简单脚本
(executor/execute-script "变量 x: 100; @调试输出(x);" parser/parse)
;; => {:success true, :last-result "100", ...}

;; 执行数学运算
(executor/execute-script "变量 a: 10; 变量 b: 20; @调试输出(@加(a, b));" parser/parse)
;; => {:success true, :last-result "30", ...}

;; 类型转换
(executor/execute-script "变量 str: \"123\"; @调试输出(@到整数(str));" parser/parse)
;; => {:success true, :last-result "123", ...}
```

### 沙箱执行

```clojure
;; 安全的沙箱执行
(let [ast (parser/parse "@调试输出(\"安全执行\");")
      code-forms (net.zthc.script.transformer/ast-to-clojure ast)
      allowed-funcs ['调试输出 '打印 '到字符串]]
  (executor/execute-in-sandbox code-forms allowed-funcs))
;; => {:success true, :sandbox true, :last-result "安全执行"}
```

### 基本解析

```clojure
;; 解析函数调用
(parser/parse-function-call "@调试输出(\"hello\", 123);")
;; => {:type :function-call, :name "调试输出", :args [...]}

;; 解析变量定义
(parser/parse-def-var "变量 age: 25;")
;; => {:type :var-def, :name "age", :value {...}}

;; 解析函数定义
(parser/parse-def-function "函数 greet(name) { @调试输出(\"你好\", name); }")
;; => {:type :function-def, :name "greet", :params ["name"], :body [...]}
```

### TCP 服务器

```clojure
(require '[net.zthc.script.tcp-server :as tcp])

;; 启动服务器
(def server (tcp/start-server 7888))

;; 获取服务器状态
(tcp/get-server-stats)

;; 停止服务器
(tcp/stop-server server)
```

### 支持的命令

TCP 服务器支持以下命令：
- `ping` - 心跳检测
- `parse:代码` - 解析中文脚本代码
- `exec:代码` - 执行中文脚本代码
- `sandbox:代码` - 在沙箱中执行代码
- `list-clients` - 列出连接的客户端
- `broadcast:消息` - 广播消息给所有客户端

## 项目结构

```
src/net/zthc/script/
├── core.clj              # 主入口和服务启动
├── parser.clj            # 主解析器
├── executor.clj          # SCI 执行引擎
├── transformer.clj       # AST 到 Clojure 代码转换器
├── builtins.clj          # 内置函数库
├── parser/
│   ├── atom.clj          # 原子表达式解析
│   ├── string.clj        # 字符串解析
│   ├── number.clj        # 数字解析
│   ├── symbol.clj        # 符号和关键字解析
│   └── util.clj          # 解析工具函数
├── tcp_server.clj        # TCP 服务器实现
└── util.clj              # 通用工具函数

test/net/zthc/script/     # 测试套件
├── executor_test.clj     # 执行引擎测试
├── transformer_test.clj  # 转换器测试
├── builtins_test.clj     # 内置函数测试
├── parser_test.clj
├── parser/
│   ├── atom_test.clj
│   ├── string_test.clj
│   ├── number_test.clj
│   └── symbol_test.clj
└── util_test.clj
```

## 中文语法规范

### 关键字
- `函数` - 函数定义
- `变量` - 变量声明
- `常量` - 常量定义
- `真/假` - 布尔值
- `返回` - 函数返回

### 内置函数

#### 输出函数
- `调试输出` - 带日志的调试输出
- `打印` - 标准输出（带换行）
- `输出` - 标准输出（不换行）

#### 类型转换
- `到整数` - 转换为整数
- `到字符串` - 转换为字符串
- `到浮点数` - 转换为浮点数

#### 数学运算
- `加` - 加法运算（支持多个参数）
- `减` - 减法运算
- `乘` - 乘法运算（支持多个参数）
- `除` - 除法运算

#### 比较操作
- `等于?` - 相等比较
- `大于?` - 大于比较
- `小于?` - 小于比较
- `大于等于?` - 大于等于比较
- `小于等于?` - 小于等于比较

#### 字符串函数
- `长度` - 获取字符串或集合长度
- `连接` - 连接字符串或集合
- `包含?` - 检查是否包含
- `为空?` - 检查是否为空
- `不为空?` - 检查是否不为空

### 语法格式

**变量声明**:
```
变量 变量名: 值;
常量 常量名: 值;
```

**函数调用**:
```
@函数名(参数1, 参数2);
```

**复杂脚本示例**:
```
变量 名字: "小明";
变量 年龄: 25;
变量 成年: @大于等于?(年龄, 18);

@调试输出("姓名:", 名字);
@调试输出("年龄:", 年龄);
@调试输出("是否成年:", 成年);

变量 问候语: @连接("你好, ", 名字, "!");
@调试输出(问候语);
```

## 技术栈

- **核心语言**: Clojure 1.12.2
- **执行引擎**: SCI (Small Clojure Interpreter) 0.10.49
- **网络通信**: Aleph 0.9.2 (TCP 服务器)
- **异步处理**: Manifold 0.4.3
- **数据处理**: clojure.data.json 2.5.1
- **开发工具**: nREPL 0.2.13
- **解析器**: 自定义解析器
- **构建工具**: Leiningen

## 执行特性

### 安全沙箱
- 基于 SCI 的安全执行环境
- 可配置的函数白名单
- 禁止危险操作（如 System/exit, 文件操作等）
- 隔离的命名空间

### 性能优化
- AST 缓存机制
- 代码优化和清理
- 惰性求值支持
- 内存使用监控

### 错误处理
- 详细的错误信息
- 执行栈跟踪
- 语法错误定位
- 运行时异常捕获

## 测试

项目包含完整的测试套件：

```bash
# 运行所有测试
lein test

# 运行特定测试
lein test net.zthc.script.executor-test
lein test net.zthc.script.transformer-test
```

**测试覆盖情况**:
- 58 个测试用例
- 332 个断言
- 覆盖所有核心模块（解析、执行、转换、内置函数）
- 0 失败，0 错误

## 开发指南

### 添加新的内置函数

1. 在 `builtins.clj` 中定义函数
2. 添加到 `builtin-functions` 映射表
3. 编写相应的测试用例
4. 更新文档

### 扩展语法支持

1. 在相应的解析器模块中添加解析逻辑
2. 在 `transformer.clj` 中添加 AST 转换逻辑
3. 在 `parser.clj` 中集成新的解析器
4. 编写测试用例

### 网络协议扩展

1. 在 `tcp_server.clj` 中添加新的命令处理
2. 实现相应的消息格式
3. 更新客户端交互逻辑
4. 添加安全检查

## 示例脚本

### 数学计算器
```
变量 a: 10;
变量 b: 5;

@调试输出("加法:", @加(a, b));
@调试输出("减法:", @减(a, b));
@调试输出("乘法:", @乘(a, b));
@调试输出("除法:", @除(a, b));
```

### 字符串处理
```
变量 文本: "Hello, 世界!";
变量 长度值: @长度(文本);

@调试输出("原文:", 文本);
@调试输出("长度:", 长度值);
@调试输出("是否包含'世界':", @包含?(文本, "世界"));
```

### 类型转换
```
变量 数字字符串: "123.45";
变量 整数值: @到整数(数字字符串);
变量 浮点值: @到浮点数(数字字符串);

@调试输出("原始:", 数字字符串);
@调试输出("整数:", 整数值);
@调试输出("浮点:", 浮点值);
```

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 项目
2. 创建功能分支
3. 编写测试
4. 提交代码
5. 创建 Pull Request

## 联系方式

如有问题或建议，请通过 GitHub Issues 联系。

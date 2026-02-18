# auto-complete-log

一个 JetBrains IDE 插件，用于自动补全日志占位符。

## 功能介绍

### 功能一：日志占位符同步（快捷键）

当用户编辑类似 `log.info("method=> var1={}", var1);` 这样的日志代码时：
- 删除字符串中的 `var1` 占位符时，同时删除后面的参数 `var1`
- 添加变量 `var2` 到参数列表时，同时在前面的字符串追加 `var2={}`

**快捷键：`CTRL+;`**

### 功能二：日志变量自动补全

在日志字符串中输入变量名前缀时，自动补全上下文可用的变量：
- 支持的日志方法：`debug`, `info`, `warn`, `error`, `trace`, `fatal`
- 补全范围：局部变量、方法参数、类成员变量、静态变量、继承的成员等
- 选中变量后自动添加到参数列表

**示例**：
```java
public void process(String userId, Order order) {
    String status = "pending";
    // 输入 log.info("user={}") 
    // 在 ={} 前输入变量名前缀时，会弹出补全提示
    // 选择 userId 后自动变为：log.info("userId={}", userId)
}
```

## 项目结构

```
intellij-auto-complete-log/
├── build.gradle.kts          # Gradle 构建配置
├── settings.gradle.kts       # Gradle 设置
├── gradle/                   # Gradle Wrapper
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew                   # Gradle Wrapper 脚本 (Unix)
├── gradlew.bat               # Gradle Wrapper 脚本 (Windows)
├── src/
│   └── main/
│       ├── java/
│       │   └── io/github/pdkst/autocompletelog/
│       │       ├── AutoCompleteAction.java       # 核心动作类，实现日志占位符同步
│       │       ├── completion/
│       │       │   ├── LogVariableCompletionContributor.java  # 补全贡献者入口
│       │       │   ├── LogVariableCompletionProvider.java     # 补全提供者
│       │       │   └── LogParameterInsertHandler.java        # 参数插入处理器
│       │       ├── util/
│       │       │   └── VariableCollector.java    # 变量收集工具类
│       │       └── Sample.java                   # 示例代码
│       └── resources/
│           ├── META-INF/
│           │   └── plugin.xml   # 插件配置文件
│           └── logback.xml      # 日志配置
└── README.md
```

## 技术栈

- **Java 11**
- **IntelliJ Platform Plugin SDK** (version: 2022.1.4)
- **Gradle** + `org.jetbrains.intellij` 插件

## 依赖

- `org.apache.commons:commons-lang3:3.13.0`
- `ch.qos.logback:logback-classic:1.5.0`
- `org.projectlombok:lombok:1.18.30` (compile only)

## 兼容性

- **since-build**: 221
- **until-build**: 241.*
- 支持 IDE: IntelliJ IDEA (IC)

## 使用方法

1. 在日志方法调用处（如 `log.info()`, `log.debug()`, `log.warn()`, `log.error()` 等）
2. 添加变量参数
3. 按下快捷键 `CTRL+;`
4. 插件会自动在日志字符串中补全对应的 `{}` 占位符

## 版本历史

### v1.3.0
- 版本升级

### v1.1.2
- 兼容 IntelliJ 2024.1 版本

### v1.1.1-SNAPSHOT
- 兼容 IntelliJ 2023.3 版本

### v1.1.0-SNAPSHOT
- 修复部分情况下无法自动补全的问题

## 作者

pdk studio (pdkstudio@163.com)

## 许可证

详见 [GitHub](https://github.com/pdkst)

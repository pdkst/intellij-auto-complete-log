# auto-complete-log

一个 JetBrains IDE 插件，用于自动补全日志占位符。

## 功能介绍

当用户编辑类似 `log.info("method=> var1={}", var1);` 这样的日志代码时：
- 删除字符串中的 `var1` 占位符时，同时删除后面的参数 `var1`
- 添加变量 `var2` 到参数列表时，同时在前面的字符串追加 `var2={}`

**快捷键：`CTRL+;`**

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
│       │       ├── AutoCompleteAction.java  # 核心动作类，实现日志自动补全逻辑
│       │       └── Sample.java              # 示例代码
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

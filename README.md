# Navicat 密码解密工具

简体中文 | [English](README_en.md)

一个基于 Java 的 Navicat 数据库连接密码加密解密工具。

## 功能特性

- 解密 Navicat 存储的密码
- 将密码加密为 Navicat 格式
- 支持 Windows 和 macOS 平台
- 兼容多个 Navicat 版本

## 使用方法

```java
NavicatPasswordUtil util = new NavicatPasswordUtil();

// 解密密码
String decryptedPassword = util.decrypt("加密的密码字符串");

// 加密密码
String encryptedPassword = util.encrypt("要加密的密码");
```

## 命令行使用

```bash
# 解密密码
java -jar navicat-password-util.jar -d "加密的密码字符串"

# 加密密码
java -jar navicat-password-util.jar -e "要加密的密码"
```

## 运行环境要求

- Java 8 或更高版本
- Bouncy Castle 加密库

## 编译方法

1. 克隆项目到本地
2. 使用 Maven 或其他构建工具进行编译
```bash
mvn clean package
```

## 注意事项

- 本工具仅供学习和研究使用
- 请勿用于非法用途
- 在使用前请确保您具有相应的权限

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进这个项目。

## 许可证

本项目采用 MIT 许可证 - 详情请查看 LICENSE 文件。
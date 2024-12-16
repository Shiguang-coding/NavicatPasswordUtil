# Navicat Password Decryption Tool

[简体中文](README.md) | English

A Java-based utility for encrypting and decrypting Navicat database connection passwords.

## Features

- Decrypt Navicat stored passwords
- Encrypt passwords for Navicat format
- Support for both Windows and macOS platforms
- Compatible with various Navicat versions

## Usage

```java
NavicatPasswordUtil util = new NavicatPasswordUtil();

// Decrypt password
String decryptedPassword = util.decrypt("encrypted_password_string");

// Encrypt password
String encryptedPassword = util.encrypt("your_password");
```

## Command Line Usage

```bash
# Decrypt password
java -jar navicat-password-util.jar -d "encrypted_password_string"

# Encrypt password
java -jar navicat-password-util.jar -e "your_password"
```

## Requirements

- Java 8 or higher
- Bouncy Castle Crypto library

## Build

1. Clone the repository
2. Build with Maven or other build tools
```bash
mvn clean package
```

## Note

- This tool is for educational purposes only
- Do not use for illegal purposes
- Ensure you have proper permissions before use

## Contributing

Issues and Pull Requests are welcome to help improve this project.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
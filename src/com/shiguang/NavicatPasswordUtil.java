package com.shiguang;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * 用于处理 Navicat 数据库连接密码加密和解密的工具类
 * @author <a href="https://github.com/shiguang-coding">時光</a>
 * @github <a href="https://github.com/Shiguang-coding/NavicatPasswordUtil">NavicatPasswordUtil</a>
 * @blog <a href="https://blog.shiguangdev.cn/2022/01/22/26622ef15f70">如何快速找回Navicat本地数据库密码？</a>
 */
public class NavicatPasswordUtil {

    public static void main(String[] args) throws Exception {
        // 创建 NavicatPasswordUtil 实例
        NavicatPasswordUtil passwordUtil = new NavicatPasswordUtil();

        // 待解密的密码字符串
        String encryptedPassword = "6EE58FD042645AF6E22B8E376B8EA727";

        // 解密 Navicat 12 及以后的版本
        String decryptedPassword = passwordUtil.decryptPassword(encryptedPassword, NavicatVersion.VERSION_12);

        // 正则替换控制符（如响铃、退格等）
        decryptedPassword = decryptedPassword.replaceAll("\\p{Cntrl}", "");

        // 输出解密后的明文 结果为 shiguang
        System.out.println("解密后的密码: " + decryptedPassword);
    }

    // AES 加密密钥
    private static final String AES_KEY = "libcckeylibcckey";
    // AES 加密向量
    private static final String AES_IV = "libcciv libcciv ";
    // Blowfish 加密密钥
    private static final String BLOWFISH_KEY = "3DC5CA39";
    // Blowfish 加密向量
    private static final String BLOWFISH_IV = "d9c7c3c8870d64bd";

    /**
     * 加密密码
     *
     * @param plaintextPassword 明文密码
     * @param navicatVersion    加密版本（NavicatVersion.VERSION_11 或 NavicatVersion.VERSION_12）
     * @return 加密后的密文密码
     * @throws Exception 加密过程中可能抛出的异常
     */
    public String encryptPassword(String plaintextPassword, NavicatVersion navicatVersion) throws Exception {
        switch (navicatVersion) {
            case VERSION_11:
                return encryptBlowfish(plaintextPassword);
            case VERSION_12:
                return encryptAES(plaintextPassword);
            default:
                throw new IllegalArgumentException("不支持的 Navicat 版本");
        }
    }

    /**
     * 解密密码
     *
     * @param encryptedPassword 密文密码
     * @param navicatVersion    解密版本（NavicatVersion.VERSION_11 或 NavicatVersion.VERSION_12）
     * @return 解密后的明文密码
     * @throws Exception 解密过程中可能抛出的异常
     */
    public String decryptPassword(String encryptedPassword, NavicatVersion navicatVersion) throws Exception {
        switch (navicatVersion) {
            case VERSION_11:
                return decryptBlowfish(encryptedPassword);
            case VERSION_12:
                return decryptAES(encryptedPassword);
            default:
                throw new IllegalArgumentException("不支持的 Navicat 版本");
        }
    }

    /**
     * 使用 Blowfish 加密密码（适用于 Navicat 11 及以前的版本）
     *
     * @param plaintextPassword 明文密码
     * @return 加密后的密文密码
     * @throws Exception 加密过程中可能抛出的异常
     */
    private String encryptBlowfish(String plaintextPassword) throws Exception {
        byte[] iv = hexStringToByteArray(BLOWFISH_IV);
        byte[] key = hashToBytes(BLOWFISH_KEY);

        int round = plaintextPassword.length() / 8;
        int leftLength = plaintextPassword.length() % 8;
        StringBuilder encryptedResult = new StringBuilder();
        byte[] currentVector = iv.clone();

        Cipher cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        for (int i = 0; i < round; i++) {
            byte[] block = xorBytes(plaintextPassword.substring(i * 8, (i + 1) * 8).getBytes(), currentVector);
            byte[] encryptedBlock = cipher.doFinal(block);
            currentVector = xorBytes(currentVector, encryptedBlock);
            encryptedResult.append(bytesToHex(encryptedBlock));
        }

        if (leftLength > 0) {
            currentVector = cipher.doFinal(currentVector);
            byte[] block = xorBytes(plaintextPassword.substring(round * 8).getBytes(), currentVector);
            encryptedResult.append(bytesToHex(block));
        }

        return encryptedResult.toString().toUpperCase();
    }

    /**
     * 使用 AES 加密密码（适用于 Navicat 12 及以后的版本）
     *
     * @param plaintextPassword 明文密码
     * @return 加密后的密文密码
     * @throws Exception 加密过程中可能抛出的异常
     */
    private String encryptAES(String plaintextPassword) throws Exception {
        byte[] iv = AES_IV.getBytes();
        byte[] key = AES_KEY.getBytes();

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] encryptedResult = cipher.doFinal(plaintextPassword.getBytes());
        return bytesToHex(encryptedResult).toUpperCase();
    }

    /**
     * 使用 Blowfish 解密密码（适用于 Navicat 11 及以前的版本）
     *
     * @param encryptedPassword 密文密码
     * @return 解密后的明文密码
     * @throws Exception 解密过程中可能抛出的异常
     */
    private String decryptBlowfish(String encryptedPassword) throws Exception {
        byte[] iv = hexStringToByteArray(BLOWFISH_IV);
        byte[] key = hashToBytes(BLOWFISH_KEY);
        byte[] encryptedBytes = hexStringToByteArray(encryptedPassword.toLowerCase());

        int round = encryptedBytes.length / 8;
        int leftLength = encryptedBytes.length % 8;
        StringBuilder decryptedResult = new StringBuilder();
        byte[] currentVector = iv.clone();

        Cipher cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        for (int i = 0; i < round; i++) {
            byte[] encryptedBlock = Arrays.copyOfRange(encryptedBytes, i * 8, (i + 1) * 8);
            byte[] decryptedBlock = xorBytes(cipher.doFinal(encryptedBlock), currentVector);
            currentVector = xorBytes(currentVector, encryptedBlock);
            decryptedResult.append(new String(decryptedBlock));
        }

        if (leftLength > 0) {
            currentVector = cipher.doFinal(currentVector);
            byte[] block = Arrays.copyOfRange(encryptedBytes, round * 8, round * 8 + leftLength);
            decryptedResult.append(new String(xorBytes(block, currentVector), StandardCharsets.UTF_8));
        }

        return decryptedResult.toString();
    }

    /**
     * 使用 AES 解密密码（适用于 Navicat 12 及以后的版本）
     *
     * @param encryptedPassword 密文密码
     * @return 解密后的明文密码
     * @throws Exception 解密过程中可能抛出的异常
     */
    private String decryptAES(String encryptedPassword) throws Exception {
        byte[] iv = AES_IV.getBytes();
        byte[] key = AES_KEY.getBytes();
        byte[] encryptedBytes = hexStringToByteArray(encryptedPassword.toLowerCase());

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] decryptedResult = cipher.doFinal(encryptedBytes);
        return new String(decryptedResult);
    }

    /**
     * 对两个字节数组进行异或操作
     *
     * @param bytes1 第一个字节数组
     * @param bytes2 第二个字节数组
     * @return 异或结果字节数组
     */
    private static byte[] xorBytes(byte[] bytes1, byte[] bytes2) {
        byte[] result = new byte[bytes1.length];
        for (int i = 0; i < bytes1.length; i++) {
            result[i] = (byte) (bytes1[i] ^ bytes2[i]);
        }
        return result;
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hexString 十六进制字符串
     * @return 字节数组
     */
    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 将字符串哈希为字节数组
     *
     * @param inputString 输入字符串
     * @return 哈希后的字节数组
     * @throws Exception 哈希过程中可能抛出的异常
     */
    private static byte[] hashToBytes(String inputString) throws Exception {
        return MessageDigest.getInstance("SHA-1").digest(inputString.getBytes());
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param byteArray 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] byteArray) {
        StringBuilder result = new StringBuilder();
        for (byte b : byteArray) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}

/**
 * Navicat 版本枚举
 */
enum NavicatVersion {
    VERSION_11,
    VERSION_12
}
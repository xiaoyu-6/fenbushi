package com.nonRepudiation;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
//实现AES-256加密和解密
public class AES256Util{
    // 定义常量，表示密钥算法和加密算法
    public static final String KEY_ALGORITHM = "AES";
    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";

    // 生成随机密钥的方法
    public static byte[] initkey() throws Exception {
        // 在Security中添加BouncyCastleProvider作为加密算法提供者
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // 实例化密钥生成器，并初始化为256位
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM, "BC");
        kg.init(256);
        // 生成密钥对象
        SecretKey secretKey = kg.generateKey();
        // 获取密钥的字节数组形式
        return secretKey.getEncoded();
    }

    // 生成一个预定的根密钥的方法
    public static byte[] initRootKey() throws Exception {
        return new byte[]{0x08, 0x08, 0x04, 0x0b, 0x02, 0x0f, 0x0b, 0x0c,
                0x01, 0x03, 0x09, 0x07, 0x0c, 0x03, 0x07, 0x0a, 0x04, 0x0f,
                0x06, 0x0f, 0x0e, 0x09, 0x05, 0x01, 0x0a, 0x0a, 0x01, 0x09,
                0x06, 0x07, 0x09, 0x0d};
    }

    // 将字节数组形式的密钥转换为Key对象的方法
    public static Key toKey(byte[] key) throws Exception {
        // 使用密钥工厂生成SecretKey对象
        SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
        return secretKey;
    }

    // 加密方法
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 将密钥字节数组转换为Key对象
        Key k = toKey(key);
        // 在Security中添加BouncyCastleProvider作为加密算法提供者
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // 实例化Cipher对象，选择加密算法和填充方式
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, "BC");
        // 初始化Cipher对象为加密模式，传入密钥
        cipher.init(Cipher.ENCRYPT_MODE, k);
        // 执行加密操作
        return cipher.doFinal(data);
    }

    // 解密方法
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 将密钥字节数组转换为Key对象
        Key k = toKey(key);
        // 在Security中添加BouncyCastleProvider作为加密算法提供者
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // 实例化Cipher对象，选择解密算法和填充方式
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, "BC");
        // 初始化Cipher对象为解密模式，传入密钥
        cipher.init(Cipher.DECRYPT_MODE, k);
        // 执行解密操作
        return cipher.doFinal(data);
    }

    // 主方法，测试AES加密和解密过程
    public static void main(String[] args) throws Exception {
        String str = "芸sweet";
        // 打印原文
        System.out.println("原文：" + str);
        // 生成随机密钥
        byte[] key = AES256Util.initkey();
        // 打印密钥
        System.out.print("密钥：");
        for (int i = 0; i < key.length; i++)
            System.out.printf("%x", key[i]);
        // 加密
        byte[] data = AES256Util.encrypt(str.getBytes(), key);
        // 打印密文
        System.out.print("加密后：");
        for (int i = 0; i < data.length; i++)
            System.out.printf("%x", data[i]);

        // 解密密文
        data = AES256Util.decrypt(data, key);

        // 打印原文
        System.out.println("解密后：" + new String(data));
    }

}
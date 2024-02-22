package com.nonRepudiation;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Util {

    /**
     * 使用 SHA-256 算法计算字符串的摘要
     *
     * @param str 要计算摘要的字符串
     * @return SHA-256 摘要的十六进制字符串表示
     */
    public static String getSHA256StrJava(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";

        try {
            // 获取 SHA-256 摘要算法的实例
            messageDigest = MessageDigest.getInstance("SHA-256");
            // 将字符串编码为字节数组，并更新摘要
            messageDigest.update(str.getBytes("UTF-8"));
            // 获取摘要的十六进制表示
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodeStr;
    }

    /**
     * 将字节数组转换为十六进制字符串表示
     *
     * @param bytes 要转换的字节数组
     * @return 字节数组的十六进制字符串表示
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;

        // 遍历字节数组，将每个字节转换为两位的十六进制字符串
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                // 对于长度为1的字符串，在其前面补0
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }

        return stringBuffer.toString();
    }
}

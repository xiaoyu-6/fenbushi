package com.nonRepudiation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//创建固定大小文件
public class CreateFixLengthFile {

    public static void main(String[] args) throws IOException {
        CreateFixLengthFile file = new CreateFixLengthFile();
        file.createFixLengthFile(new File("file6"), 1024 * 1024 * 100); // 创建一个大小为100MB的文件
    }

    /**
     * 创建固定大小的文件
     *
     * @param file   要创建的文件
     * @param length 文件的长度，以字节为单位
     * @throws IOException
     */
    public void createFixLengthFile(File file, long length) throws IOException {
        FileOutputStream fos = null;
        FileChannel output = null;
        try {
            // 创建FileOutputStream以向文件写入数据
            fos = new FileOutputStream(file);
            // 获取FileChannel，可以通过它进行文件IO操作
            output = fos.getChannel();
            // 创建一个ByteBuffer，用于写入文件
            ByteBuffer buffer = ByteBuffer.allocate(1);
            // 将ByteBuffer写入文件，写入长度为length-1的空字节，相当于在文件中创建了一个指定大小的空洞
            output.write(buffer, length - 1);
        } finally {
            try {
                // 关闭FileChannel和FileOutputStream
                if (output != null) {
                    output.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

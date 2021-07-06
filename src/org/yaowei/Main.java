package org.yaowei;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yaowei
 * @version 2.1
 */
public class Main {
    public static void main(String[] args) {
        System.err.println("\n解密 qmcflac 文件，程序版本：2.0\n" +
                "使用方法：将 jar 与 qmcflac 文件放在同一个文件夹内执行 jar 即可\n" +
                "需要 Java10 及以上，推荐 Java11\n" +
                "警告：本程序仅用于交流学习，请勿用作非法途径。");
        System.out.println("程序执行中，请稍等。");
        final Main main = new Main();
        // 获取当前路径 URL 对象
        final URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        // 获取当前路径父文件
        final File parentFile = new File(URLDecoder.decode(location.getPath(), StandardCharsets.UTF_8)).getParentFile();

        List<File> qmcFileList = null;
        try {
            // 获取所有 qmcflac File
            qmcFileList = main.getQmcFiles(parentFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int passCount = 0;
        final ArrayList<String> errorFiles = new ArrayList<>();
        // 解密所有 qmcflac File并输出至磁盘
        for (File file : qmcFileList) {
            final boolean decodeStatus = main.qmcflacDecode(file);
            // 解密失败记录文件路径
            if (decodeStatus) {
                passCount++;
            } else {
                errorFiles.add(file.getPath());
            }
        }

        System.out.println("执行完成，共检测到" + qmcFileList.size() +
                "个 qmcflac 文件，其中解密成功" + passCount +
                "个。");
        if (errorFiles.size() > 0) {
            System.err.println(errorFiles.size() + "个文件未成功解密：");
            for (String errorFile : errorFiles) {
                System.out.println(errorFile);
            }
        }
        System.out.println("");
    }

    /**
     * 获取指定 File 对象内所有后缀为 .qmcflac 的 File 对象集合，包括子文件夹。
     *
     * @param dir 指定 File 对象
     * @return 返回后缀为 .qmcflac 的 File 对象集合
     * @throws FileNotFoundException 如果指定 File 不存在、不是文件夹或文件夹内无 File 对象，则抛出异常
     */
    public List<File> getQmcFiles(File dir) throws FileNotFoundException {
        if (!dir.exists() || dir.isFile()) {
            throw new FileNotFoundException("路径不存在或不是文件夹");
        }
        final File[] files = dir.listFiles();
        if (files == null) {
            throw new FileNotFoundException("路径内没有文件或文件夹");
        }
        final ArrayList<File> result = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".qmcflac")) {
                result.add(file);
            } else if (file.isDirectory()) {
                final List<File> qmcFile = getQmcFiles(file);
                result.addAll(qmcFile);
            }
        }
        return result;
    }

    /**
     * 解密 qmcflac 文件并输出至磁盘
     *
     * @param qmcflacFile qmcflac File
     * @return 返回是否成功解密并输出至磁盘。
     */
    public boolean qmcflacDecode(File qmcflacFile) {
        try (final BufferedInputStream input = new BufferedInputStream(new FileInputStream(qmcflacFile));
             final BufferedOutputStream output = new BufferedOutputStream(
                     new FileOutputStream(qmcflacFile.getName().replaceAll(".qmcflac$", ".flac")))
        ) {
            byte[] bytes = new byte[input.available()];
            input.read(bytes);
            final Decode decode = new Decode();
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (decode.nextMask() ^ bytes[i]);
            }
            output.write(bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
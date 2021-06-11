package com.yaowei;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author yaowei
 * @date 2021-06-07 21:58
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("用于解析QQ音乐 qmcflac 文件。将此 jar 文件移动至有 qmcflac 文件的文件夹内执行即可。");
        URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println(location.getPath());
        System.out.println("开始执行");
        File jarFile = new File(location.getPath());
        int fileCount = 0;
        int passCount = 0;
        ArrayList<String> errorFiles = new ArrayList<>();
        File thisDir = jarFile.getParentFile();
        if (thisDir == null || !thisDir.exists() || thisDir.isFile()) {
            System.err.println("thisDir == null");
            return;
        }
        File[] files = thisDir.listFiles();
        if (files == null || files.length == 0) {
            System.err.println("The dir " + thisDir.getName() + " don't have file.");
            return;
        }
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        byte[] bytes;
        for (File file : files) {
            if (file.isDirectory() || file.length() <= 100000 || !file.getName().matches(".*.qmcflac")) {
                continue;
            }
            fileCount++;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                Decode decode = new Decode();
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = (byte) (decode.nextMask() ^ bytes[i]);
                }
                outputStream = new BufferedOutputStream(new FileOutputStream(
                        file.getName().replaceAll(".qmcflac$", ".flac")));
                outputStream.write(bytes);
                passCount++;
                System.out.println("解析成功：" + file.getName());
            } catch (IOException e) {
                errorFiles.add(file.getName());
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("执行完毕");
        System.out.println("共有" + fileCount + "个 qmcflac 文件，成功解析" + passCount + "个文件。");
        if (errorFiles.size() > 0) {
            System.err.println("以下文件出现错误：");
            for (String errorFile : errorFiles) {
                System.out.println(errorFile);
            }
        }
    }
}
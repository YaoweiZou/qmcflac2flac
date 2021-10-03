package org.yaowei;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yaowei
 * @version 3.0
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("qmcflac to flac");
        System.out.println("version 3.0");
        System.out.println("使用方法：将 jar 与 qmcflac 文件移至同一文件夹内执行 jar 即可，需要 Java 1.8 及以上");
        System.out.println("\n正在执行，稍等······\n");
        // 获取当前目录
        String userDir = System.getProperty("user.dir");
        final File thisDir = new File(userDir);
        List<File> qmcFiles = null;
        try {
            qmcFiles = getQmcFiles(thisDir);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert qmcFiles != null;
        final int qmcSize = qmcFiles.size();
        final ArrayList<String> failList = new ArrayList<>();
        for (File qmcFile : qmcFiles) {
            final boolean result = qmcflacDecode(qmcFile);
            if (!result) {
                failList.add(qmcFile.getAbsolutePath());
            }
        }
        System.out.println("程序执行结束，共" + qmcSize + "个 qmc 文件，成功" + (qmcSize - failList.size()) + "个");
        if (failList.size() > 0) {
            System.err.println("失败：");
            for (String failFile : failList) {
                System.err.println(failFile);
            }
        }
    }

    /**
     * 获取指定文件夹内所有后缀为 .qmcflac 的 File 对象集合
     *
     * @param dir 指定文件夹
     * @return 包含后缀为 qmcflac 文件的 list
     * @throws FileNotFoundException 指定文件夹内无文件或无 qmcflac 文件时抛出异常
     */
    public static List<File> getQmcFiles(File dir) throws FileNotFoundException {
        final File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            throw new FileNotFoundException("文件夹为空");
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
        if (result.size() == 0) {
            throw new FileNotFoundException("文件夹内找不到 qmcflac 文件");
        }
        return result;
    }

    /**
     * 解密 qmcflac 文件并写入磁盘
     *
     * @param qmcFile qmcflac File
     * @return 返回是否成功解密并写入磁盘。
     */
    public static boolean qmcflacDecode(File qmcFile) {
        try (final BufferedInputStream input = new BufferedInputStream(new FileInputStream(qmcFile));
             final BufferedOutputStream output = new BufferedOutputStream(
                     new FileOutputStream(qmcFile.getName().replaceAll(".qmcflac$", ".flac")))
        ) {
            byte[] bytes = new byte[input.available()];
            input.read(bytes);
            final Decode decode = new Decode();
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (decode.nextMask() ^ bytes[i]);
            }
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
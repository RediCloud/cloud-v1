package net.suqatri.cloud.commons.test;

import jodd.io.ZipUtil;
import net.suqatri.cloud.commons.file.FileUtils;
import net.suqatri.cloud.commons.file.ZipUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class Main {

    public static void main(String[] args) throws IOException {
        File file1 = new File("folder1");
        File file2 = new File("folder2");

        if(!file1.exists()) return;

        File zip = new File("test1.zip");
        File newZip = new File("test2.zip");

        //ZIP
        ZipUtils.zipDirFiles(file1, FileUtils.getAllFilesAndDirs(file1), zip);

        long time = System.currentTimeMillis();
        System.out.println("Starting coping file1 to file2");
        System.out.println("File1: " + file1.getAbsolutePath());
        System.out.println("File2: " + file2.getAbsolutePath());

        byte[] bytes = FileUtils.fileToBytes(zip.getAbsolutePath());
        System.out.println("bytes: " + bytes.length);

        List<byte[]> list = FileUtils.splitByteArray(bytes, 104857600 / 1000);

        System.out.println(list.size() + "mbs");

        byte[] newBytes = FileUtils.mergeByteArrays(list);
        System.out.println("newBytes: " + newBytes.length);

        FileUtils.bytesToFile(newBytes, new File("test2.zip").getAbsolutePath());

        //UNZIP
        ZipUtils.unzipDir(newZip, file2.getPath());

        System.out.println((System.currentTimeMillis() - time) + "ms");
    }

}

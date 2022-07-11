package net.suqatri.cloud.commons.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static void zipFiles(File zipFile, List<File> files, String subtractRelativePath) throws IOException{
        byte[] buffer = new byte[1024];
        FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        for (File scrFile : files) {
            FileInputStream fileInputStream = new FileInputStream(scrFile);
            zipOutputStream.putNextEntry(new ZipEntry(scrFile.getParent().replaceFirst(subtractRelativePath, "")));
            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, length);
            }
            zipOutputStream.closeEntry();
            fileInputStream.close();
        }
    }

    public static void zipDir(File fileToZip, File zipFile) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(zipFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipFile(null, fileToZip, fileToZip.getName(), zipOutputStream);
        zipOutputStream.close();
        outputStream.close();
    }

    public static void zipDirFiles(File fileToZip, List<File> files, File zipFile) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(zipFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipFile(files, fileToZip, fileToZip.getName(), zipOutputStream);
        zipOutputStream.close();
        outputStream.close();
    }

    private static void zipFile(List<File> files, File fileToZip, String fileName, ZipOutputStream zipOutputStream) throws IOException {
        if(fileToZip.isHidden()) return;

        if(fileToZip.isDirectory()){
            File[] children = fileToZip.listFiles();
            ZipEntry zipEntry = new ZipEntry(fileName + "/");
            zipOutputStream.putNextEntry(zipEntry);
            for (File childFile : children) {
                if(childFile.isDirectory() || files == null || files.contains(childFile)){
                    zipFile(files, childFile, fileName + "/" + childFile.getName(), zipOutputStream);
                }
            }
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while((length = fileInputStream.read(bytes)) >= 0){
            zipOutputStream.write(bytes, 0, length);
        }
        fileInputStream.close();
    }

    public static void unzipDir(File zipFile, String dirToUnzip) throws IOException{
        byte[] buffer = new byte[1024];
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while(zipEntry != null){
            String fileName = zipEntry.getName();
            File newFile = new File(dirToUnzip + File.separator + fileName);
            if(zipEntry.isDirectory()){
                newFile.mkdirs();
            }else{
                newFile.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                int length;
                while((length = zipInputStream.read(buffer)) >= 0){
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.close();
            }
            zipInputStream.closeEntry();
            zipEntry = zipInputStream.getNextEntry();
        }
    }

}

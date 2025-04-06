package fun.moystudio.openlink.logic;

import fun.moystudio.openlink.frpc.OldFrpc;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Extract {
    public static void ExtractBySuffix(File file, String suffix,File extractDir) throws Exception {//仅解压10MB以内的文件
        if(!file.exists()) {
            throw new RuntimeException("[OpenLink] The file to extract does not exist!");
        }
        FileInputStream fileInputStream=new FileInputStream(file);
        if(!extractDir.exists()){
            extractDir.mkdir();
        }
        if(suffix.equals(".zip")){
            Thread thread=new Thread(()->{
                try{
                    ZipInputStream zipInputStream=new ZipInputStream(fileInputStream);
                    ZipEntry zipEntry=zipInputStream.getNextEntry();
                    while (zipEntry!=null){
                        String path = extractDir.getAbsolutePath()+File.separator+zipEntry.getName();
                        if(zipEntry.isDirectory()){
                            File dir=new File(path);
                            dir.mkdir();
                        } else {
                            FileOutputStream fileOutputStream=new FileOutputStream(path);
                            byte[] buffer=new byte[OldFrpc.MAX_BUFFER_SIZE];
                            int length;
                            while((length=zipInputStream.read(buffer))>0){
                                fileOutputStream.write(buffer,0,length);
                            }
                            fileOutputStream.close();
                        }
                        zipEntry=zipInputStream.getNextEntry();
                    }
                    zipInputStream.closeEntry();
                    zipInputStream.close();
                    fileInputStream.close();
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            },"Extract thread");
            thread.start();
            thread.join();
            return;
        } else if (suffix.equals(".tar.gz")) {
            Thread thread = new Thread(()->{
                try{
                    GZIPInputStream gzipInputStream=new GZIPInputStream(fileInputStream);
                    TarArchiveInputStream tarInputStream=new TarArchiveInputStream(gzipInputStream);
                    TarArchiveEntry tarEntry=tarInputStream.getNextTarEntry();
                    while (tarEntry!=null){
                        String path = extractDir.getAbsolutePath()+File.separator+tarEntry.getName();
                        if(tarEntry.isDirectory()) {
                            File dir=new File(path);
                            dir.mkdir();
                        } else {
                            FileOutputStream fileOutputStream=new FileOutputStream(path);
                            byte[] buffer=new byte[OldFrpc.MAX_BUFFER_SIZE];
                            int length;
                            while((length=tarInputStream.read(buffer))>0){
                                fileOutputStream.write(buffer,0,length);
                            }
                            fileOutputStream.close();
                        }
                        tarEntry=tarInputStream.getNextTarEntry();
                    }
                    tarInputStream.close();
                    gzipInputStream.close();
                    fileInputStream.close();
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },"Extract thread");
            thread.start();
            thread.join();
            return;
        }
        throw new Exception("[OpenLink] The suffix of the file to extract is unsupported!");
    }
    public static void ExtractBySuffix(File file, String suffix) throws Exception {
        ExtractBySuffix(file,suffix,file.getParentFile());
    }
}

package nd.esp.service.lifecycle.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.*;

public class ZipUtils {

    private final static Logger LOG = LoggerFactory.getLogger(ZipUtils.class);

    private static final int BUFFER = 1024*10;

    /**
     * 将指定目录压缩到和该目录同名的zip文件，自定义压缩路径
     * @param sourceFilePath  目标文件路径
     * @param zipFilePath     指定zip文件路径
     * @return
     */
    public static boolean zip(String sourceFilePath,String zipFilePath) throws Exception{
        boolean result=false;
        File source=new File(sourceFilePath);
        if(!source.exists()){

            LOG.info(sourceFilePath+" doesn't exist.");

            return result;
        }
        if(!source.isDirectory()){

            LOG.info(sourceFilePath+" is not a directory.");

            return result;
        }
        File zipFile=new File(zipFilePath+"/"+source.getName()+".zip");

        if(!zipFile.getParentFile().exists()){
            if(!zipFile.getParentFile().mkdirs()){

                LOG.info("cann't create file "+zipFile.getName());

                return result;
            }
        }

        LOG.info("creating zip file...");
        try (   
            FileOutputStream dest = new FileOutputStream(zipFile);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            ZipOutputStream out=new ZipOutputStream(new BufferedOutputStream(checksum))
        ){
            out.setMethod(ZipOutputStream.DEFLATED);
            compress(source,out,source.getName());
            result=true;
        } catch (FileNotFoundException e) {

            LOG.warn("Zipping: File not found:", e);

            throw e;
        }

        LOG.info("zip done.");
        
        return result;
    }
    private static void compress(File file,ZipOutputStream out,String mainFileName) throws Exception{
        if(file.isFile()){
            try (
                FileInputStream fi = new FileInputStream(file);
                BufferedInputStream origin=new BufferedInputStream(fi, BUFFER)
            ) {
                int index=file.getAbsolutePath().indexOf(mainFileName)+mainFileName.length()+1;
                String entryName=file.getAbsolutePath().substring(index).replace('\\', '/');
                ZipEntry entry = new ZipEntry(entryName);
                out.putNextEntry(entry);
                byte[] data = new byte[BUFFER];
                int count;
                while((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            } catch (FileNotFoundException e) {

                LOG.warn("Zipping: File not found:", e);

                throw e;
            } catch (IOException e) {

                LOG.error("Zipping: IOException:",e);

                throw e;
            }
        }else if (file.isDirectory()){
            File[] fs=file.listFiles();
            if(fs!=null&&fs.length>0){
                for(File f:fs){
                    compress(f,out,mainFileName);
                }
            } else {
                int index=file.getAbsolutePath().indexOf(mainFileName)+mainFileName.length()+1;
                String entryName=file.getAbsolutePath().substring(index).replace('\\', '/');
                ZipEntry entry = new ZipEntry(entryName+"/");
                out.putNextEntry(entry);
                out.closeEntry();
            }
        }
    }

    /**
     * 将zip文件解压到指定的目录，该zip文件必须是使用该类的zip方法压缩的文件
     * @param zipFile
     * @param destPath
     * @return
     */
    public static boolean unzip(File zipFile,String destPath) throws Exception{
        boolean result=false;
        if(!zipFile.exists()){
            LOG.info(zipFile.getName()+" doesn't exist.");
            return result;
        }
        File target=new File(destPath);
        if(!target.exists()){
            if(!target.mkdirs()){
                LOG.info("cann't create file "+target.getName());
                return result;
            }
        }

        LOG.info("start unzip file ...");
        try (
            FileInputStream fis= new FileInputStream(zipFile);
            CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
        ) {
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                if(entry.isDirectory()){
                    continue;
                }
                int count;
                byte data[] = new byte[BUFFER];
                String entryName=entry.getName();
                String newEntryName=destPath+"/"+entryName;
                File temp=new File(newEntryName).getParentFile();
                if(!temp.exists()){
                    if(!temp.mkdirs()){
                        throw new RuntimeException("create file "+temp.getName() +" fail");
                    }
                }
                FileOutputStream fos = new FileOutputStream(newEntryName);
                BufferedOutputStream dest = new BufferedOutputStream(fos,BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            result=true;
        } catch (FileNotFoundException e) {
            LOG.warn("Zipping: File not found:", e);
            LOG.info("unzip fail.");
            throw e;
        } catch (IOException e) {
            LOG.warn("Zipping: IOException:", e);
            LOG.info("unzip fail.");
            throw e;
        }
        
        LOG.info("unzip done.");

        return result;
    }
    
//    public static void main(String[] args) throws IOException {
//        File zipFile=new File("D:/desk/temp2.zip");
//        //File zipFile=new File("D:/desk/6ff13df4-886a-4672-a64a-63cd9bf63770.zip");
//        ZipUtils.unzip(zipFile,"d:/desk/temp") ;
//        ZipUtils.zip("d:/desk/temp", "d:/desk");
//    }
}





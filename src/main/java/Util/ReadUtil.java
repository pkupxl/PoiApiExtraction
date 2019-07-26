package Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class ReadUtil {
    public static String getContent(File file){
        try{
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] input = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(input);
            bufferedInputStream.close();
            return new String(input);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

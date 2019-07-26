package Util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PomUtil {
    public static File getPomFile(String path){
        File curFile=new File(path);
        if(!curFile.isDirectory()){
            if(curFile.getName().equals("pom.xml")){
                return curFile;
            }
        }else{
            File[]innerFiles=curFile.listFiles();
            for(File f:innerFiles){
                File pom=getPomFile(f.getAbsolutePath());
                if(pom!=null){
                    return pom;
                }
            }
        }
        return null;
    }

    public static String getPoiVersion(File pom){
        if(pom==null)return null;
        SAXReader reader = new SAXReader();
        try{
            Document document = reader.read(pom);
            Element rootElement = document.getRootElement();
            List<Element> Dependencies = new ArrayList();
            parseDependency(rootElement, Dependencies);
            for(Element element:Dependencies){
                boolean isPOI=false;
                String PoiVersion=null;
                for (Iterator<?> iterator = element.elementIterator(); iterator.hasNext();) {
                    Element e = (Element)iterator.next();
                    if (e.getName().equals("groupId")&&e.getText().equals("org.apache.poi")) {
                        isPOI=true;
                    }
                    if(e.getName().equals("version")){
                        PoiVersion=e.getText();
                    }
                }
                if(isPOI){
                    if(PoiVersion==null)continue;
                    if(!PoiVersion.startsWith("$")){
                        return PoiVersion;
                    }else{
                        int len=PoiVersion.trim().length();
                        String tag=PoiVersion.trim().substring(2,len-1);
                        String exactVersion=getTagValue(tag,rootElement);
                        return exactVersion;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static void parseDependency(Element rootElement, List<Element> Dependencies){
        for (Iterator<?> iterator = rootElement.elementIterator(); iterator.hasNext();) {
            Element element = (Element)iterator.next();
            if (element.getName().equals("dependency")) {
                Dependencies.add(element);
            } else if(element.hasContent()){
                parseDependency(element,Dependencies);
            }
        }
    }

    public static String getTagValue(String tag, Element rootElement){
        for (Iterator<?> iterator = rootElement.elementIterator(); iterator.hasNext();) {
            Element element = (Element)iterator.next();
            if (element.getName().equals(tag)){
                return element.getText();
            }
            if(element.hasContent()){
                String value=getTagValue(tag,element);
                if(value!=null)return value;
            }
        }
        return null;
    }
}

package Util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.*;

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

    public static void main(String args[]){
        int cnt=0;
        String dataPath="D:\\Data";
        File Dir=new File(dataPath);
        File[] Projects = Dir.listFiles();

        List<String>VS=new ArrayList<String>();
        Map<String ,Integer>m=new HashMap<String ,Integer>();
        for(File f:Projects){
            File pom=getPomFile(f.getAbsolutePath());
            String v=getPoiVersion(pom);
            cnt++;
     //       System.out.println(cnt+": "+ v);
            if(!VS.contains(v)){
                VS.add(v);
            }
            if(m.containsKey(v)){
                int n=m.get(v);
                m.put(v,n+1);
            }else{
                m.put(v,1);
            }
        }

        VS.sort(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        int sum=0;
        for(String s:VS){
            System.out.println(s+" : "+m.get(s));
            sum+=m.get(s);
        }
        System.out.println(sum);

    }
}

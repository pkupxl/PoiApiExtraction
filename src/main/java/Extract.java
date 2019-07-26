import Entity.ApiVersionInvolveInfo;
import Entity.Invocation;
import Entity.PoiApiInfo;
import Util.PomUtil;
import Util.ReadUtil;
import Visitor.PoiImportVisitor;
import Visitor.PoiMethodInvocationVisitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import java.io.File;
import java.util.*;

public class Extract {
    public static List<String> getImportsFromFile(File file){
        String Content= ReadUtil.getContent(file);
        ASTParser astParser = ASTParser.newParser(AST.JLS10);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setSource(Content.toCharArray());
        CompilationUnit unit = (CompilationUnit) (astParser.createAST(null));
        PoiImportVisitor importVisitor = new PoiImportVisitor();
        unit.accept(importVisitor);
        return importVisitor.getPoiImports();
    }

    public static List<Invocation>getInvocations(File file){
        String Content= ReadUtil.getContent(file);
        ASTParser astParser = ASTParser.newParser(AST.JLS10);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        astParser.setBindingsRecovery(true);
        astParser.setEnvironment(null, null, null, true);
        astParser.setUnitName("any_name");
        astParser.setSource(Content.toCharArray());
        CompilationUnit unit = (CompilationUnit) (astParser.createAST(null));
        List<String>poiImports=getImportsFromFile(file);
        PoiMethodInvocationVisitor invocationVisitor = new PoiMethodInvocationVisitor();
        unit.accept(invocationVisitor);
        List<Invocation>invocations= invocationVisitor.getInvocations();
        List<Invocation>result=new ArrayList<Invocation>();
        for(int i=0;i<invocations.size();++i){
            String expressionType=invocations.get(i).expressionType;
            String apiName=invocations.get(i).apiName;
            String statement=invocations.get(i).statement;
            for(int j=0;j<poiImports.size();++j){
                if(poiImports.get(j).endsWith(expressionType)){
                    result.add(new Invocation(apiName,statement,poiImports.get(j)));
                    break;
                }
            }
        }
        return result;
    }

    public static void getPoiApiInfo(File file,List<PoiApiInfo>poiApiInfos){
        if(!file.isDirectory()){
            if(file.getName().endsWith(".java")){
                List<Invocation>invocations=getInvocations(file);
                for(int i=0;i<invocations.size();++i){
                    String apiName=invocations.get(i).apiName;
                    String relatedImport=invocations.get(i).expressionType;
                    String statement=invocations.get(i).statement;
                    List<String>useExample=new ArrayList<String>();
                    useExample.add(statement);
                    boolean alreadyExist=false;

                    for(int j=0;j<poiApiInfos.size();++j){
                        PoiApiInfo curApi=poiApiInfos.get(j);
                        if(curApi.getName().equals(apiName) && curApi.getPackagePath().equals(relatedImport)){
                            alreadyExist=true;
                            if(curApi.getUseExample().containsKey(file.getAbsolutePath())){
                                curApi.getUseExample().get(file.getAbsolutePath()).add(statement);
                            }else{
                                curApi.getUseExample().put(file.getAbsolutePath(),useExample);
                            }
                            break;
                        }
                    }

                    if(alreadyExist)continue;
                    else{
                        Map<String, List<String>>useExamples=new HashMap();
                        useExamples.put(file.getAbsolutePath(),useExample);
                        poiApiInfos.add(new PoiApiInfo(apiName,relatedImport,useExamples));
                    }
                }
            }
        }else{
            File[] fs = file.listFiles();
            for(File f:fs){
                getPoiApiInfo(f,poiApiInfos);
            }
        }
    }

    public static void main(String args[]){
        String dataPath="E:\\data";
        File Dir=new File(dataPath);
        File[] Projects = Dir.listFiles();
        int ProjectCnt=0;

        List<ApiVersionInvolveInfo>APIS=new ArrayList<ApiVersionInvolveInfo>();

        for(File project:Projects){
            File pom=PomUtil.getPomFile(project.getAbsolutePath());
            if(pom==null)continue;
            String poiVersionUsed=PomUtil.getPoiVersion(pom);
            if(poiVersionUsed==null)continue;
            List<PoiApiInfo>poiApiInfos=new ArrayList<PoiApiInfo>();
            getPoiApiInfo(project,poiApiInfos);

            for(int i=0;i<poiApiInfos.size();++i){
                String apiName=poiApiInfos.get(i).getName();
                String packagePath=poiApiInfos.get(i).getPackagePath();

                boolean alreadyExist=false;
                for(int j=0;j<APIS.size();++j){
                    if(APIS.get(j).getApiName().equals(apiName) && APIS.get(j).getPackagePath().equals(packagePath)){
                        alreadyExist=true;
                        if(APIS.get(j).getVersions().containsKey(poiVersionUsed)){
                            if(!APIS.get(j).getVersions().get(poiVersionUsed).contains(project.getName())){
                                APIS.get(j).getVersions().get(poiVersionUsed).add(project.getName());
                            }
                        }else{
                            List<String>p=new ArrayList<String>();
                            p.add(project.getName());
                            APIS.get(j).getVersions().put(poiVersionUsed,p);
                        }
                    }
                }

                if(alreadyExist)continue;
                else{
                    List<String>p=new ArrayList<String>();
                    p.add(project.getName());
                    Map<String,List<String>>versions=new HashMap<String, List<String>>();
                    versions.put(poiVersionUsed,p);
                    APIS.add(new ApiVersionInvolveInfo(apiName,packagePath,versions));
                }
            }

           /* ProjectCnt++;
            System.out.println(ProjectCnt+": "+project.getName()+"  poiVersion:"+poiVersionUsed);
            for(int i=0;i<poiApiInfos.size();++i){
                PoiApiInfo api=poiApiInfos.get(i);
                System.out.println("  "+api.getPackagePath()+" "+api.getName());
              *//*  for(String s:api.getUseExample().keySet()){
                    System.out.println("    "+s+":");
                    for(String e:api.getUseExample().get(s)){
                        System.out.println(e);
                    }
                }*//*
            }
            System.out.println("--------------------------------------------");*/
        }


        APIS.sort(new Comparator<ApiVersionInvolveInfo>() {
            public int compare(ApiVersionInvolveInfo o1, ApiVersionInvolveInfo o2) {
                return o2.getVersions().keySet().size()-o1.getVersions().keySet().size();
            }
        });
       /* for(int i=0;i<APIS.size();++i){
            System.out.println((i+1)+":"+APIS.get(i).getPackagePath()+" "+ APIS.get(i).getApiName()+
                    " 涉及"+APIS.get(i).getVersions().size()+"个版本");
            for(String s:APIS.get(i).getVersions().keySet()){
                System.out.print(s+"  ");
                List<String>ps=APIS.get(i).getVersions().get(s);
                for(String p:ps){
                    System.out.print(p+" ");
                }
                System.out.println();
            }
            System.out.println();
            System.out.println();
        }*/

       Map<Integer,List<String>>Cnt=new HashMap<Integer, List<String>>();
       for(int i=0;i<APIS.size();++i){
           int num=APIS.get(i).getVersions().size();
           if(Cnt.containsKey(num)){
               Cnt.get(num).add(APIS.get(i).getPackagePath()+" "+APIS.get(i).getApiName());
           }else{
               List<String>apinames=new ArrayList<String>();
               apinames.add(APIS.get(i).getPackagePath()+" "+APIS.get(i).getApiName());
               Cnt.put(num,apinames);
           }
       }

       for(int i:Cnt.keySet()){
           System.out.println("在"+i+"个版本中出现的API有"+Cnt.get(i).size()+"个:");
           for(String s:Cnt.get(i)){
               System.out.println("    "+s);
           }
            System.out.println();
       }
    }
}

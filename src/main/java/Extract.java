import Entity.ApiVersionInvolveInfo;
import Entity.Invocation;
import Entity.PoiApiInfo;
import Git.GitManagement;
import Util.PomUtil;
import Util.ReadUtil;
import Visitor.PoiImportVisitor;
import Visitor.PoiMethodInvocationVisitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.*;

import static Git.GitManagement.getAllCommitWithTag;

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
            if(expressionType==null)continue;
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
        String dataPath="D:\\Data";
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
            GitManagement G=new GitManagement("D:\\项目源代码\\poi\\.git");
            Map<String, RevCommit> Commits=getAllCommitWithTag();

            for(int i=0;i<poiApiInfos.size();++i){
                String apiName=poiApiInfos.get(i).getName();
                String packagePath=poiApiInfos.get(i).getPackagePath();
                System.out.print(packagePath+" "+apiName);
                Map<String,List<String>>r=G.getAPIHistory(Commits,packagePath,apiName);
                System.out.println("    版本数目"+r.size());
                for(String v:r.keySet()){
                    System.out.println("版本:"+v);
                    List<String>ms=r.get(v);
                    for(String m:ms){
                        System.out.println(m);
                    }
                }
            }
        }
    }





}

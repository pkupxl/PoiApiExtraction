package Git;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitManagement {
    private Repository repository;
    public GitManagement(String path){
        try{
            repository = new FileRepository(path);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Map<String,RevCommit>getAllCommitWithTag(){
        try{
            Repository repository = new FileRepository("D:\\项目源代码\\poi\\.git");
            Git git = new Git(repository);
            List<Ref> call = git.tagList().call();
            Map<String,RevCommit>result=new HashMap<String, RevCommit>();
            for (Ref ref : call) {
                if(ref.getName().startsWith("refs/tags/REL")){
                    RevWalk walk = new RevWalk(repository);
                    RevCommit commit = walk.parseCommit(ref.getObjectId());
                    result.put(ref.getName().substring(14).replace('_','.'),commit);
                }
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    class methodFindVisitor extends ASTVisitor {
        public String methodName;
        public List<String>Candidate;

        public methodFindVisitor(String methodName){
            this.methodName=methodName;
            this.Candidate=new ArrayList<String>();
        }
        public boolean visit(MethodDeclaration node){
            if(node.getName().toString().equals(methodName)){
                if(node.getBody()!=null)
                    Candidate.add(node.getBody().toString());
            }
            return false;
        }

        public List<String>getCandidate(){
            return Candidate;
        }
    }


    public  List<String> findMethod(String fileContent,String methodName){
        ASTParser parser = ASTParser.newParser(AST.JLS10);
        parser.setSource(fileContent.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        methodFindVisitor visitor=new methodFindVisitor(methodName);
        CompilationUnit unit=(CompilationUnit)parser.createAST(null);
        unit.accept(visitor);
        return visitor.getCandidate();
    }

    public Map<String,List<String>> getAPIHistory(Map<String,RevCommit>result,String path, String name){
        try  {
            Map<String,List<String>>APIHistory=new HashMap<String, List<String>>();
            for(String version:result.keySet()){
                RevCommit commit=result.get(version);
                RevWalk walk = new RevWalk(repository);
                RevTree tree = walk.parseTree(commit.getTree().getId());
                TreeWalk treeWalk = new TreeWalk(repository);
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    String fileContent = new String(loader.getBytes());
                    String Path=treeWalk.getPathString();
                    Path=Path.replace('/','.');
                    if(Path.contains(path+".java")){
                         /*   System.out.print("版本:");
                            System.out.print(version+"   ");*/
                        //              System.out.println(Path);
                        //             System.out.println(fileContent);
                        List<String>ms=findMethod(fileContent,name);
                        if(ms.size()>0){
                            APIHistory.put(version,ms);
                        }

                    }

                }
            }
            return APIHistory;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}

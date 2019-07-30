package Visitor;

import Entity.Invocation;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class PoiMethodInvocationVisitor extends ASTVisitor {
    public List<Invocation>invocations;

    public PoiMethodInvocationVisitor() {
        this.invocations=new ArrayList<Invocation>();
    }

    public List<Invocation>getInvocations(){
        return this.invocations;
    }

    @Override
    public boolean visit(MethodInvocation node){
        String apiname=node.getName().toString();
        String statement=node.toString();
       /* System.out.println(statement);
        System.out.println(apiname);
        Expression exp=node.getExpression();
        if(exp!=null){
            ITypeBinding typeBinding = node.getExpression().resolveTypeBinding();
            if(typeBinding!=null){
                System.out.println(typeBinding.getName());
            }
            if(exp.getNodeType()== ASTNode.QUALIFIED_NAME){
                System.out.println("QUALIFY!!!!");
            }

            List<Expression>args=node.arguments();
            for(int i=0;i<args.size();++i){
                ITypeBinding binding=args.get(i).resolveTypeBinding();
                if(binding!=null){
                    System.out.println("args"+i+":"+binding.getQualifiedName());
                }
            }
        }

        System.out.println("----------------------------------------------");*/


        if(node.getExpression()==null)return true;
        ITypeBinding typeBinding = node.getExpression().resolveTypeBinding();
        if(typeBinding==null)return true;
        invocations.add(new Invocation(apiname,statement,typeBinding.getName()));
        return true;
    }


}

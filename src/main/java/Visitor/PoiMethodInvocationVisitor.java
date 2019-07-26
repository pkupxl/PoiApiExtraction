package Visitor;

import Entity.Invocation;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
        if(node.getExpression()==null)return true;
        ITypeBinding typeBinding = node.getExpression().resolveTypeBinding();
        if(typeBinding==null)return true;
        invocations.add(new Invocation(apiname,statement,typeBinding.getName()));
        return true;
    }
}

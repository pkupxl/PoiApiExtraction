package Visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import java.util.ArrayList;
import java.util.List;

public class PoiImportVisitor extends ASTVisitor {
    private List<String>poiImports;

    public PoiImportVisitor() {
        this.poiImports = new ArrayList<String>();
    }

    public List<String>getPoiImports(){
        return poiImports;
    }

    @Override
    public boolean visit(ImportDeclaration node){
        String importName=node.getName().toString();
        if(importName.startsWith("org.apache.poi")){
            poiImports.add(importName);
        }
        return true;
    }
}

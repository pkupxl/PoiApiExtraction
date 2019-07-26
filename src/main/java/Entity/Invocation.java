package Entity;

public class Invocation{
    public String apiName;
    public String statement;
    public String expressionType;
    public Invocation(String apiName,String statement,String expressionType){
        this.apiName=apiName;
        this.statement=statement;
        this.expressionType=expressionType;
    }
}
package Entity;

import java.util.List;
import java.util.Map;

public class PoiApiInfo {
    private String name;                //API名
    private String packagePath;         //API所在包名
    private Map<String, List<String>>useExample;       //API使用例子: 文件名,[语句1，语句2...]

    public PoiApiInfo(String name, String packagePath, Map<String, List<String>> useExample) {
        this.name = name;
        this.packagePath = packagePath;
        this.useExample = useExample;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public Map<String, List<String>> getUseExample() {
        return useExample;
    }

    public void setUseExample(Map<String, List<String>> useExample) {
        this.useExample = useExample;
    }

}

package Entity;

import java.util.List;
import java.util.Map;

public class ApiVersionInvolveInfo {
    public String apiName;
    public String packagePath;
    public Map<String, List<String>>versions;

    public ApiVersionInvolveInfo(String apiName, String packagePath, Map<String, List<String>> versions) {
        this.apiName = apiName;
        this.packagePath = packagePath;
        this.versions = versions;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public Map<String, List<String>> getVersions() {
        return versions;
    }

    public void setVersions(Map<String, List<String>> versions) {
        this.versions = versions;
    }
}

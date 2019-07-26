package Entity;

import java.util.List;

public class Project {
    private String name;
    private String path;
    private String poiVersion;
    private List<PoiApiInfo>PoiApis;

    public Project(String name, String path, String poiVersion, List<PoiApiInfo> poiApis) {
        this.name = name;
        this.path = path;
        this.poiVersion = poiVersion;
        PoiApis = poiApis;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPoiVersion() {
        return poiVersion;
    }

    public void setPoiVersion(String poiVersion) {
        this.poiVersion = poiVersion;
    }

    public List<PoiApiInfo> getPoiApis() {
        return PoiApis;
    }

    public void setPoiApis(List<PoiApiInfo> poiApis) {
        PoiApis = poiApis;
    }
}

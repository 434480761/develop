package nd.esp.service.lifecycle.services.updatamediatype.model;

import java.util.LinkedList;
import java.util.List;

public class UpdateMediatypeModel {
    private long resourceTotal;

    private long haveMdeiaType;

    private long nothaveFormat;

    private long needUpdate;

    private long saveScuccess;

    private List<String> ids = new LinkedList<String>();

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public long getResourceTotal() {
        return resourceTotal;
    }

    public void setResourceTotal(long resourceTotal) {
        this.resourceTotal = resourceTotal;
    }

    public long getHaveMdeiaType() {
        return haveMdeiaType;
    }

    public void setHaveMdeiaType(long haveMdeiaType) {
        this.haveMdeiaType = haveMdeiaType;
    }

    public long getNothaveFormat() {
        return nothaveFormat;
    }

    public void setNothaveFormat(long nothaveFormat) {
        this.nothaveFormat = nothaveFormat;
    }

    public long getNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(long needUpdate) {
        this.needUpdate = needUpdate;
    }

    public long getSaveScuccess() {
        return saveScuccess;
    }

    public void setSaveScuccess(long saveScuccess) {
        this.saveScuccess = saveScuccess;
    }

    @Override
    public String toString() {
        return "UpdateMediatypeModel [resourceTotal=" + resourceTotal + ", haveMdeiaType=" + haveMdeiaType
                + ", nothaveFormat=" + nothaveFormat + ", needUpdate=" + needUpdate + ", saveScuccess=" + saveScuccess
                + "]";
    }

}

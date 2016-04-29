package nd.esp.service.lifecycle.models.statisticals.v06;

public class ResourceStatisticalModel {
    private String keyTitle;

    private Double keyValue;

    private String dataFrom;

    public String getKeyTitle() {
        return keyTitle;
    }

    public void setKeyTitle(String keyTitle) {
        this.keyTitle = keyTitle;
    }

    public Double getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(Double keyValue) {
        this.keyValue = keyValue;
    }

    public String getDataFrom() {
        return dataFrom;
    }

    public void setDataFrom(String dataFrom) {
        this.dataFrom = dataFrom;
    }

    @Override
    public String toString() {
        return "StatisticalModel [keyTitle=" + keyTitle + ", keyValue=" + keyValue + ", dataFrom=" + dataFrom + "]";
    }
    
}

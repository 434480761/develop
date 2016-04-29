package nd.esp.service.lifecycle.vos.statistical.v06;


public class ResourceStatisticalViewModel {
    
//    @NotBlank(message="{statisticalViewModel.keyTitle.notBlank.validmsg}",groups={BasicInfoDefault.class, LessPropertiesDefault.class, UpdateKnowledgeDefault.class})
    private String keyTitle;

    private Double keyValue ;

//    @NotBlank(message="{statisticalViewModel.dataFrom.notBlank.validmsg}",groups={BasicInfoDefault.class, LessPropertiesDefault.class, UpdateKnowledgeDefault.class})
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
        return "StatisticalViewModel [keyTitle=" + keyTitle + ", keyValue=" + keyValue + ", dataFrom=" + dataFrom + "]";
    }

}

package nd.esp.service.lifecycle.utils.titan.script.model;

import nd.esp.service.lifecycle.utils.titan.script.annotation.*;

/**
 * Created by Administrator on 2016/9/14.
 */
@TitanEdge(label = "has_tech_info")
public class TitanTechInfoEdge extends TitanModel{

    @TitanEdgeTargetKey(target = "identifier")
    @TitanCompositeKey
    @TitanField(name = "identifier")
    protected String identifier;

    @TitanField(name = "description")
    protected String description;

    @TitanField(name = "ti_entry")
    private String entry;

    @TitanField(name = "ti_format")
    private String format;

    @TitanField(name = "ti_location")
    private String location;

    @TitanField(name = "ti_md5")
    private String md5;

    @TitanField(name = "ti_requirements")
    private String requirements;

    @TitanField(name = "ti_secure_key")
    private String secureKey;

    @TitanField(name = "ti_size")
    private Long size;

    @TitanField(name = "ti_title")
    protected String title;

    @TitanField(name = "ti_printable")
    private Boolean printable;

    @TitanEdgeResourceKey(source = "identifier")
    private String resource;

//    @TitanEdgeResourceKey(source = "primary_category")
    private String resType;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getSecureKey() {
        return secureKey;
    }

    public void setSecureKey(String secureKey) {
        this.secureKey = secureKey;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getPrintable() {
        return printable;
    }

    public void setPrintable(Boolean printable) {
        this.printable = printable;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }
}

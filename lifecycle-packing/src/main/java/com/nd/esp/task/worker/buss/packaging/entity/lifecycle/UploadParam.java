package com.nd.esp.task.worker.buss.packaging.entity.lifecycle;

/**
 * @title 上传接口参数bean
 * @desc
 * @atuh lwx
 * @createtime on 2015年7月6日 下午9:37:36
 */
public class UploadParam {

    private String resourceType;

    private String uuid;

    private String uid;

    private boolean renew;

    private String coverage;

    public static final String DEFAULT_UID = "777";

    public static final String DEFAULT_RESOURCE_TYPE = ResourcesType.COURSEWARES.getMessage();

    public static final String DEFAULT_COVERAGE = "";

    public UploadParam(String uuid) {
        this.uuid = uuid;
        this.resourceType = DEFAULT_RESOURCE_TYPE;
        this.coverage = DEFAULT_COVERAGE;
        this.renew = false;
        this.uid = DEFAULT_UID;

    }

    public UploadParam buildResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;

    }

    public UploadParam buildRenew(boolean renew) {
        this.renew = renew;
        return this;

    }

    public UploadParam buildCoverage(String coverage) {
        this.coverage = coverage;
        return this;

    }

    public UploadParam buildUid(String uid) {
        this.uid = uid;
        return this;

    }

    public String getResourceType() {
        return resourceType;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUid() {
        return uid;
    }

    public boolean isRenew() {
        return renew;
    }

    public String getCoverage() {
        return coverage;
    }

    public static void main(String[] args) {
        UploadParam param = new UploadParam("uuid");
        param.buildCoverage("coverage");
        param.buildResourceType("resourcetype");
        System.out.println(param.getCoverage());
        System.out.println(param.getResourceType());
        System.out.println(param.getUid());
        System.out.println(param.getUuid());
        System.out.println(param.isRenew());

    }

}

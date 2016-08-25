package nd.esp.service.lifecycle.support.busi.titan;

import java.util.List;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.repository.model.ResourceCategory;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.ResourceStatistical;
import nd.esp.service.lifecycle.repository.model.TechInfo;

public class CheckResourceModel {
    private Education education;
    private List<TechInfo> techInfos;
    private List<ResCoverage> resCoverages;
    private List<ResourceCategory> resourceCategories;
    private List<ResourceStatistical> resourceStatistic;
    private List<ResourceRelation> resourceRelations;
    
    private CheckResourceModel(Builder builder) {
        this.education = builder.education;
        this.techInfos = builder.techInfos;
        this.resCoverages = builder.resCoverages;
        this.resourceCategories = builder.resourceCategories;
        this.resourceStatistic = builder.resourceStatistic;
        this.resourceRelations = builder.resourceRelations;
    }

    public static class Builder {
        private Education education;
        private List<TechInfo> techInfos;
        private List<ResCoverage> resCoverages;
        private List<ResourceCategory> resourceCategories;
        private List<ResourceStatistical> resourceStatistic;
        private List<ResourceRelation> resourceRelations;
        
        public Builder(Education education){
            this.education = education;
        }
        
        public Builder techInfos(List<TechInfo> techInfos){
            this.techInfos = techInfos;
            return this;
        }

        public Builder resCoverages(List<ResCoverage> resCoverages){
            this.resCoverages = resCoverages;
            return this;
        }
        
        public Builder resourceCategories(List<ResourceCategory> resourceCategories){
            this.resourceCategories = resourceCategories;
            return this;
        }
        
        public Builder statistic(List<ResourceStatistical> statistic){
            this.resourceStatistic = statistic;
            return this;
        }
        
        public CheckResourceModel builder(){
            return new CheckResourceModel(this);
        }
    }

    public Education getEducation() {
        return education;
    }

    public void setEducation(Education education) {
        this.education = education;
    }

    public List<TechInfo> getTechInfos() {
        return techInfos;
    }

    public void setTechInfos(List<TechInfo> techInfos) {
        this.techInfos = techInfos;
    }

    public List<ResCoverage> getResCoverages() {
        return resCoverages;
    }

    public void setResCoverages(List<ResCoverage> resCoverages) {
        this.resCoverages = resCoverages;
    }

    public List<ResourceCategory> getResourceCategories() {
        return resourceCategories;
    }

    public void setResourceCategories(List<ResourceCategory> resourceCategories) {
        this.resourceCategories = resourceCategories;
    }

    public List<ResourceStatistical> getResourceStatistic() {
        return resourceStatistic;
    }

    public void setResourceStatistic(List<ResourceStatistical> resourceStatistic) {
        this.resourceStatistic = resourceStatistic;
    }

    public List<ResourceRelation> getResourceRelations() {
        return resourceRelations;
    }

    public void setResourceRelations(List<ResourceRelation> resourceRelations) {
        this.resourceRelations = resourceRelations;
    }

    @Override
    public String toString() {
        return "CheckResourceModel [education=" + education + ", techInfos=" + techInfos + ", resCoverages="
                + resCoverages + ", resourceCategories=" + resourceCategories + ", resourceStatistic="
                + resourceStatistic + ", resourceRelations=" + resourceRelations + "]";
    }
    
}


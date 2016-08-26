package nd.esp.service.lifecycle.vos.chapters.v06;

import org.springframework.http.HttpStatus;

import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;

/**
 * 06章节的常量
 * <p>Create Time: 2015年8月4日           </p>
 * @author xiezy
 */
public class ChapterConstant {
    /**
     * 方向
     */
    public final static String DIR_PRE = "pre";
    public final static String DIR_NEXT = "next";
    
    /**
     * 章节的root左值的起始值
     */
    public final static int CHAPTER_LEFT_INITIAL_VALUE = 1;
    
    /**
     * sql运算符
     */
    public final static String GREATER_THAN = ">";
    public final static String GREATER_THAN_OR_EQUAL = ">=";
    
    /**
     * 获取子章节时的模式
     */
    public final static String PATTERN_ALL = "All";
    public final static String PATTERN_CHILDREN = "Children";
    
    /**
     * 判断是否是合法的direction	
     * <p>Create Time: 2015年8月4日   </p>
     * <p>Create author: xiezy   </p>
     * @param dir
     * @return
     */
    public static boolean isDirection(String dir){
        if(StringUtils.isEmpty(dir)){
            return false;
        }
        
        if(dir.equals(ChapterConstant.DIR_PRE) || dir.equals(ChapterConstant.DIR_NEXT)){
            return true;
        }
        
        return false;
    }
    
    public static void validChapterSupportResourceTypes(String resourceType){
        if(!ChapterSupportResourceTypes.isChapterSupportResourceTypes(resourceType)){
            throw new LifeCircleException(HttpStatus.NOT_FOUND,"LC/CHAPTER_NOT_SUPPORT","tree model for chapters not support resourceType: "+resourceType);
        }
    }
    
    public enum ChapterSupportResourceTypes{
        teachingmaterials,guidancebooks,userspace,metacurriculums;
        public static boolean isChapterSupportResourceTypes(String resourceType) {

            if (StringUtils.hasText(resourceType)) {
                for(ChapterSupportResourceTypes type:ChapterSupportResourceTypes.values()){
                    if(type.toString().equals(resourceType)){
                        return true;
                    }
                }
            }
            return false;

        }
    }
}

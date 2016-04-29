package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.solr.client.solrj.beans.Field;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 类描述:bean 创建人: 创建时间:2015-05-13 18:52:3
 * 
 * @version
 */

@Entity
@Table(name = "chapters")
//@EntityListeners(value={ChapterPersistListener.class})
@NamedQueries({
    @NamedQuery(name = "getLastChapterOnSameLevel", query = "SELECT c FROM Chapter c WHERE c.primaryCategory='chapters' AND c.teachingMaterial=:tmid And c.parent=:pid ORDER BY c.left DESC"),
    @NamedQuery(name = "queryChapterListWithoutParent", query = "SELECT c FROM Chapter c WHERE c.primaryCategory='chapters' AND c.teachingMaterial=:tmid ORDER BY c.left"),
    @NamedQuery(name = "queryChapterListWithParent", query = "SELECT c FROM Chapter c WHERE c.primaryCategory='chapters' AND c.teachingMaterial=:tmid And c.parent=:pid ORDER BY c.left"),
    @NamedQuery(name = "queryChapterListWithoutParentWithEnableTrue", query = "SELECT c FROM Chapter c WHERE c.enable=1 AND c.primaryCategory='chapters' AND c.teachingMaterial=:tmid ORDER BY c.left"),
    @NamedQuery(name = "queryChapterListWithParentWithEnableTrue", query = "SELECT c FROM Chapter c WHERE c.enable=1 AND c.primaryCategory='chapters' AND c.teachingMaterial=:tmid And c.parent=:pid ORDER BY c.left"),
    @NamedQuery(name = "countQueryChapterListWithoutParent", query = "SELECT COUNT(c.identifier) AS total FROM Chapter c WHERE c.primaryCategory=:resourceType AND c.teachingMaterial=:tmid"),
    @NamedQuery(name = "countQueryChapterListWithParent", query = "SELECT COUNT(c.identifier) AS total FROM Chapter c WHERE c.primaryCategory=:resourceType AND c.teachingMaterial=:tmid And c.parent=:pid"),
    @NamedQuery(name = "getChaptersByLeftAndRight", query = "SELECT c FROM Chapter c WHERE c.primaryCategory='chapters' AND c.teachingMaterial=:tmid AND c.left>=:tleft AND c.right<=:tright ORDER BY c.left"),
    @NamedQuery(name = "getParentsByLeftAndRight", query = "SELECT c FROM Chapter c WHERE c.primaryCategory='chapters' AND c.teachingMaterial=:tmid AND c.left<=:tleft AND c.right>=:tright ORDER BY c.left"),
    @NamedQuery(name = "deleteRelationByChapterIds", query = "DELETE FROM ResourceRelation WHERE sourceUuid IN (:sids) OR target IN (:tids)"),
})
public class Chapter extends Education {

	/**
	* 
	*/
	@Column(name = "order_num")
	private Integer orderNum;
	/**
	* 
	*/
	@Column(name = "parent")
	private String parent;
	/**
	* 
	*/
	@Column(name = "teaching_material")
	private String teachingMaterial;

	@Column(name="tree_left")
	private Integer left;
	
	@Column(name="tree_right")
	private Integer right;

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getParent() {
		return this.parent;
	}


	@Override
	public IndexSourceType getIndexType() {
		this.setPrimaryCategory(IndexSourceType.ChapterType.getName());
		return IndexSourceType.ChapterType;
	}

	public Integer getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	public String getTeachingMaterial() {
		return teachingMaterial;
	}

	public void setTeachingMaterial(String teachingMaterial) {
		this.teachingMaterial = teachingMaterial;
	}

	public Integer getLeft() {
		return left;
	}

	public void setLeft(Integer left) {
		this.left = left;
	}

	public Integer getRight() {
		return right;
	}

	public void setRight(Integer right) {
		this.right = right;
	}

}
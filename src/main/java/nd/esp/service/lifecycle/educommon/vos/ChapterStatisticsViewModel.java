package nd.esp.service.lifecycle.educommon.vos;
/**
 * 统计教材章节下资源数量的viewModel
 * @author xiezy
 * @date 2016年7月13日
 */
public class ChapterStatisticsViewModel {
	/**
	 * 父章节id
	 */
	private String parent;
	/**
	 * 章节名称
	 */
	private String chapterTitle;
	private Integer counts;
	
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public Integer getCounts() {
		return counts;
	}
	public void setCounts(Integer counts) {
		this.counts = counts;
	}
	public String getChapterTitle() {
		return chapterTitle;
	}
	public void setChapterTitle(String chapterTitle) {
		this.chapterTitle = chapterTitle;
	}
}

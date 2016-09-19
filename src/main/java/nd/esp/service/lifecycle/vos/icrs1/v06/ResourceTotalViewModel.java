package nd.esp.service.lifecycle.vos.icrs1.v06;

public class ResourceTotalViewModel {
	
	//couserwares 总数
	private int totalCourseware;
	//assets 总数
	private int totalMultimedia;
	//questions 总数
	private int totalBasicQuestion;
	//coursewareobjects
	private int totalFunnyQuestion;
	
	public int getTotalCourseware() {
		return totalCourseware;
	}
	public void setTotalCourseware(int totalCourseware) {
		this.totalCourseware = totalCourseware;
	}
	public int getTotalMultimedia() {
		return totalMultimedia;
	}
	public void setTotalMultimedia(int totalMultimedia) {
		this.totalMultimedia = totalMultimedia;
	}
	public int getTotalBasicQuestion() {
		return totalBasicQuestion;
	}
	public void setTotalBasicQuestion(int totalBasicQuestion) {
		this.totalBasicQuestion = totalBasicQuestion;
	}
	public int getTotalFunnyQuestion() {
		return totalFunnyQuestion;
	}
	public void setTotalFunnyQuestion(int totalFunnyQuestion) {
		this.totalFunnyQuestion = totalFunnyQuestion;
	}
}

package nd.esp.service.lifecycle.support.enums;
/**
 * 多实例同步变量主键
 * @author Administrator
 *
 */
public enum SynVariable {
	deleteDirtyTask(1),queryAsyncTask(2),esSynTask(3),notifTask(4),importDataSync(5);
	
	private final int pid;
	SynVariable(int pid){
		this.pid = pid;
	}
	
	public int getValue(){
		return this.pid;
	}
}

package nd.esp.service.lifecycle.support.categorysync;

public class CategorySyncConstant {
	public final static Integer TYPE_CATEGORY = 1;
	public final static Integer TYPE_CATEGORY_DATA = 2;
	
	public final static Integer UNHANDLE = 0;
	public final static Integer HANDLED = 1;
	
	public final static Integer OPERATION_DELETE = 0;
	public final static Integer OPERATION_UPDATE = 1;
	
	public final static Integer UNCERTAIN_ERROR = 1;
	public final static Integer PATCH_ERROR = 2;
	public final static Integer UPDATE_CATEGORY_SYNC_ERROR = 3;
	public final static Integer INSERT_CATEGORY_SYNC_ERROR = 4;
}

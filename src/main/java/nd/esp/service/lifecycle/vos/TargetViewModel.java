package nd.esp.service.lifecycle.vos;

/**
 *  维度数据viewModel(用于查关系中的target)
 * 
 * <br>Created 2015年5月7日 下午2:39:46
 * @version  0.3
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public class TargetViewModel {
    /**
     * 维度数据uuid
     */
    private String identifier;

	/**
	 * 维度数据ndCode
	 */
	private String ndCode;
	/**
	 * 维度数据中文名称
	 */
	private String title;
	/**
	 * 维度数据英文名称
	 */
	private String shortName;
	
	public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getNdCode() {
		return ndCode;
	}
	public void setNdCode(String ndCode) {
		this.ndCode = ndCode;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	

}

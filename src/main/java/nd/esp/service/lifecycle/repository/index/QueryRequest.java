package nd.esp.service.lifecycle.repository.index;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.util.StringUtils;




public class QueryRequest {
	private String keyword;
	private int type = Integer.MAX_VALUE;
	private int subtype = Integer.MAX_VALUE;
	private int offset = 0;
	private int limit = 10;
	private String extraKeyWord;
	/**
	 * 默认查询域
	 */
	private String df;
	
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSubtype() {
		return subtype;
	}

	public void setSubtype(int subtype) {
		this.subtype = subtype;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isAll() {
		return ((getKeyword() == null || "".equals(getKeyword()))
				&& getType() == Integer.MAX_VALUE && getSubtype() == Integer.MAX_VALUE);
	}

	public boolean hasKeyWord() {
		return (getKeyword() != null && !"".equals(getKeyword()));
	}

	public boolean hasType() {
		return (type != Integer.MAX_VALUE);
	}

	public boolean hasSubType() {
		return (subtype != Integer.MAX_VALUE);
	}

	public void clear() {
		keyword = "";
		type = Integer.MAX_VALUE;
		subtype = Integer.MAX_VALUE;
		offset = 0;
		limit = 10;
	}
	
	public String getExtraKeyWord() {
		return extraKeyWord;
	}

	public void setExtraKeyWord(String extraKeyWord) {
		this.extraKeyWord = extraKeyWord;
	}

	public String getDf() {
		return df;
	}

	public void setDf(String df) {
		this.df = df;
	}

	@Override
	public String toString() {
		return "QueryRequest [keyword=" + keyword + ", offset=" + offset
				+ ", limit=" + limit + ", df=" + df + "]";
	}
	
	public String getQuerySyntax(String param){
		StringBuffer sb = new StringBuffer();
		if(!StringUtils.isEmpty(param)){
			setKeyword(param);
		}
		
		if(!StringUtils.isEmpty(df)){
			sb.append(" AND "+df+":");
			if(StringUtils.isEmpty(getKeyword())){
				sb.append("* ");
			}else{
				sb.append(getKeyword());
			}
		}else{
			sb.append("AND text:");
			if(StringUtils.isEmpty(getKeyword())){
				sb.append("* ");
			}else{
				sb.append(getKeyword());
			}
		}
		
		return sb.toString();
	}
	
	public String getQuerySyntax(){
		return getQuerySyntax("");
	}
}

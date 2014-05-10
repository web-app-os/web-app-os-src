package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrudBean extends AbstractExecuteBean implements Serializable {

	private static final long serialVersionUID = 2116054513925125865L;

	@JsonProperty("OFFSET")
	private String offset;
	@JsonProperty("LIMIT")
	private String limit;
	@JsonProperty("PAGER")
	private PagerBean pager;
	@JsonProperty("COND")
	private List<ConditionBean> conds;
	@JsonProperty("COL")
	private List<ConditionBean> cols;
	@JsonProperty("ARGS")
	private List<ProcedureArgBean> args;
	@JsonProperty("SORT")
	private List<String> sorts;

	private int resultCount;

	private Map<Integer, Integer> ids = new HashMap<Integer, Integer>();

	public String getOffset() {
		return this.offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public String getLimit() {
		return this.limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public PagerBean getPager() {
		return this.pager;
	}

	public void setPager(PagerBean pager) {
		this.pager = pager;
	}

	public List<ConditionBean> getConds() {
		return this.conds;
	}

	public void setConds(List<ConditionBean> conds) {
		this.conds = conds;
	}


	public List<ConditionBean> getCols() {
		return this.cols;
	}

	public void setCols(List<ConditionBean> cols) {
		this.cols = cols;
	}

	public List<ProcedureArgBean> getArgs() {
		return this.args;
	}

	public void setArgs(List<ProcedureArgBean> args) {
		this.args = args;
	}

	public List<String> getSorts() {
		return this.sorts;
	}

	public void setSorts(List<String> sorts) {
		this.sorts = sorts;
	}

	public Map<Integer, Integer> getId() {
		return this.ids;
	}

	public Integer getId(String row) {
		return this.ids.get(row);
	}

	public void putId(Integer row, Integer id) {
		this.ids.put(row, id);
	}

	public int getResultCount() {
		return this.resultCount;
	}

	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}
}

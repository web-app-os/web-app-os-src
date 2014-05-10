package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;
import java.util.List;

public class ExecuteBean implements Serializable {

	private static final long serialVersionUID = -5482235352222231597L;

	private List<AbstractExecuteBean> executeInfoList;
	private ResultBean resultInfo;

	public List<AbstractExecuteBean> getExecuteInfoList() {
		return this.executeInfoList;
	}
	public void setExecuteInfoList(List<AbstractExecuteBean> executeInfoList) {
		this.executeInfoList = executeInfoList;
	}
	public ResultBean getResultInfo() {
		return this.resultInfo;
	}
	public void setResultInfo(ResultBean resultInfo) {
		this.resultInfo = resultInfo;
	}
}

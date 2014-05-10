package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

public class SystemConstantBean implements Serializable {

	private static final long serialVersionUID = 5315869861404219789L;

	private String category;
	private String key;
	private String value;
	private String dataType;
	private Integer displayOrder;
	private Object realValue;

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category.toUpperCase();
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key.toUpperCase();
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDataType() {
		return this.dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Integer getDisplayOrder() {
		return this.displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Object getRealValue() {
		return this.realValue;
	}

	public void setRealValue(Object realValue) {
		this.realValue = realValue;
	}
}

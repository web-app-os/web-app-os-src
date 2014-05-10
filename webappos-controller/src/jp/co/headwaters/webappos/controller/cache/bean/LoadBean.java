package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoadBean extends CrudBean implements Serializable {

	private static final long serialVersionUID = 2312686891837060324L;

	@JsonProperty("NOT_FOUND_ERR")
	private boolean isNotFooundError;

	public boolean isNotFooundError() {
		return this.isNotFooundError;
	}

	public void setNotFooundError(boolean isNotFooundError) {
		this.isNotFooundError = isNotFooundError;
	}
}

package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ColumnCommentBean implements Serializable {

	private static final long serialVersionUID = -8787031665420928881L;

	@JsonProperty("CIPHER")
	private String cipher;
	@JsonProperty("RANDOM")
	private String random;

	public String getCipher() {
		return this.cipher;
	}

	public void setCipher(String cipher) {
		this.cipher = cipher;
	}

	public String getRandom() {
		return this.random;
	}

	public void setRandom(String random) {
		this.random = random;
	}
}

package jp.co.headwaters.webappos.controller.cache.bean;


public class ImageCacheBean {

	private String key;
	private byte[] imageData;
	private long expireDate;

	public ImageCacheBean(String key, byte[] imageData, long expireDate) {
		this.key = key;
		this.imageData = imageData;
		this.expireDate = expireDate;
	}

	public boolean isExipired() {
		return this.expireDate < System.currentTimeMillis();
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public byte[] getImageData() {
		return this.imageData;
	}

	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	public long getExpireDate() {
		return this.expireDate;
	}

	public void setExpireDate(long expireDate) {
		this.expireDate = expireDate;
	}
}

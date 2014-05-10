package jp.co.headwaters.webappos.controller.enumation;

/**
 * ContentTypeを表す列挙子
 */
public enum ContentTypeEnum {

	HTML("html"), //$NON-NLS-1$
	XML("xml"); //$NON-NLS-1$

	private String type;

	private ContentTypeEnum(final String type) {
		this.type = type;
	}

	public static ContentTypeEnum getContentType(final String type) {
		for (ContentTypeEnum e : ContentTypeEnum.values()) {
			if (type.equals(e.getContentType())) {
				return e;
			}
		}
		throw new IllegalArgumentException(type);
	}

	public String getContentType() {
		return this.type;
	}
}

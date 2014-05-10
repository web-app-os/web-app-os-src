package jp.co.headwaters.webappos.controller.enumation;

/**
 * 画像の表示方法を表す列挙子
 */
public enum StretchTypeEnum {

	/**
	 * 対象の領域を埋めるように画像のサイズを変更します。縦横比は維持されません。
	 */
	FILL("0","fill"), //$NON-NLS-1$ //$NON-NLS-2$
	/**
	 * 画像の縦横比を維持しながら、対象の領域に収まるようにサイズを変更します。
	 */
	UNIFORM("1","Uniform"), //$NON-NLS-1$ //$NON-NLS-2$
	/**
	 * 画像の縦横比を維持しながら、対象の領域が埋まるようにサイズを変更します。
	 * 縦横比がコンテンツと異なる場合は、対象の領域に合わせて元のコンテンツを切り抜きます。
	 */
	UNIFORMTOFILL("2","UniformToFill"); //$NON-NLS-1$ //$NON-NLS-2$

	private String key;
	private String type;

	private StretchTypeEnum(final String key, final String type) {
		this.key = key;
		this.type = type;
	}

	public static StretchTypeEnum getStretchTypeByKey(final String key) {
		if (key == null) {
			return UNIFORMTOFILL;
		}
		for (StretchTypeEnum e : StretchTypeEnum.values()) {
			if (key.equalsIgnoreCase(e.getKey())) {
				return e;
			}
		}
		throw new IllegalArgumentException(key);
	}

	public static StretchTypeEnum getStretchType(final String type) {
		if (type == null) {
			return UNIFORMTOFILL;
		}
		for (StretchTypeEnum e : StretchTypeEnum.values()) {
			if (type.equalsIgnoreCase(e.getType())) {
				return e;
			}
		}
		throw new IllegalArgumentException(type);
	}

	public String getKey() {
		return this.key;
	}

	public String getType() {
		return this.type;
	}
}

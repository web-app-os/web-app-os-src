package jp.co.headwaters.webappos.controller.enumation;

/**
 * ストアドプロシージャの言語を表す列挙子
 */
public enum PgLanguageEnum {

	SQL("sql"), //$NON-NLS-1$
	PGPGSQL("plpgsql"); //$NON-NLS-1$

	private String lang;

	private PgLanguageEnum(final String lang) {
		this.lang = lang;
	}

	public static PgLanguageEnum getLanguage(final String lang) {
		for (PgLanguageEnum e : PgLanguageEnum.values()) {
			if (lang.equals(e.getLanguage())) {
				return e;
			}
		}
		throw new IllegalArgumentException(lang);
	}

	public String getLanguage() {
		return this.lang;
	}
}

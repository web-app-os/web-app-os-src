package jp.co.headwaters.webappos.controller.enumation;

/**
 * CRUD処理を表す列挙子
 */
public enum CrudEnum {

	/** Exampleを指定してヒット件数を取得する */
	COUNT("COUNT", "countByExample"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Exampleを指定してヒット件数を取得する */
	COUNT_ALL("COUNTALL", "countAllByExample"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Exampleを指定してレコードを取得する(対象テーブルのみ) */
	SELECT_BY_EXAMPLE("SELECT", "selectByExample"), //$NON-NLS-1$ //$NON-NLS-2$
	/** PKを指定してレコードを取得する(対象テーブルのみ) */
	SELECT_BY_PK("SELECTBYPK", "selectByExample"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Exampleを指定してレコードを取得する(参照テーブル含む) */
	SELECT_ALL_BY_EXAMPLE("SELECTALL", "selectAllByExample"), //$NON-NLS-1$ //$NON-NLS-2$
	/** 登録 */
	INSERT("INSERT", "insertSelective"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Exampleを指定して更新する */
	UPDATE("UPDATE", "updateByExampleSelective"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Exampleを指定して更新する */
	UPDATE_ALL("UPDATEALL", "updateByExample"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Exampleを指定して削除する */
	DELETE("DELETE", "deleteByExample"); //$NON-NLS-1$ //$NON-NLS-2$

	private String method;
	private String statementId;

	private CrudEnum(final String method, final String statementId) {
		this.method = method;
		this.statementId = statementId;
	}

	public static CrudEnum getCrud(final String method) {
		for (CrudEnum e : CrudEnum.values()) {
			if (method.equals(e.getMethod())) {
				return e;
			}
		}
		throw new IllegalArgumentException(method);
	}

	public String getMethod() {
		return this.method;
	}

	public String getStatementId() {
		return this.statementId;
	}
}

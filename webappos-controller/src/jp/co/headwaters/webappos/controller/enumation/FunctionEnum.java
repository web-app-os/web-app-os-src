package jp.co.headwaters.webappos.controller.enumation;

/**
 * サーバ機能名を表す列挙子
 */
public enum FunctionEnum {

	/** CRUD機能 */
	FUNCTION_CRUD("crud"), //$NON-NLS-1$
	/** メール送信機能 */
	FUNCTION_SENDMAIL("sendmail"), //$NON-NLS-1$
	/** ストアドプロシージャ */
	FUNCTION_PROCEDURE("procedure"), //$NON-NLS-1$
	/** ファイルアップロード機能 */
	FUNCTION_UPLOAD("upload"), //$NON-NLS-1$
	/** ファイルダウンロード機能 */
	FUNCTION_DOWNLOAD("download"); //$NON-NLS-1$

	private String funcName;

	private FunctionEnum(final String funcName) {
		this.funcName = funcName;
	}

	public static FunctionEnum getFunction(final String funcName) {
		for (FunctionEnum e : FunctionEnum.values()) {
			if (funcName.equals(e.getFunctionName())) {
				return e;
			}
		}
		throw new IllegalArgumentException(funcName);
	}

	public String getFunctionName() {
		return this.funcName.toUpperCase();
	}
}

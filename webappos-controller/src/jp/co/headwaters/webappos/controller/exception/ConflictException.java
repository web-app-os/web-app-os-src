package jp.co.headwaters.webappos.controller.exception;

public class ConflictException extends WebAppOSException {

	private static final long serialVersionUID = -6398506356238343262L;

	public ConflictException() {
		super();
	}

	public ConflictException(String key) {
		super(key);
	}

	public ConflictException(String key, String arg1) {
		super(key, arg1);
	}

	public ConflictException(String key, String arg1, String arg2) {
		super(key, arg1, arg2);
	}

	public ConflictException(String key, String arg1, String arg2, String arg3) {
		super(key, arg1, arg2, arg3);
	}
}

package jp.co.headwaters.webappos.controller.exception;

public class InvalidTokenException extends WebAppOSException {

	private static final long serialVersionUID = -1726899781642324413L;

	public InvalidTokenException() {
		super();
	}

	public InvalidTokenException(String key) {
		super(key);
	}

	public InvalidTokenException(String key, String arg1) {
		super(key, arg1);
	}

	public InvalidTokenException(String key, String arg1, String arg2) {
		super(key, arg1, arg2);
	}

	public InvalidTokenException(String key, String arg1, String arg2, String arg3) {
		super(key, arg1, arg2, arg3);
	}
}

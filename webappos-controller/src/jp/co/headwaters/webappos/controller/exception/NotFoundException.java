package jp.co.headwaters.webappos.controller.exception;

public class NotFoundException extends WebAppOSException {

	private static final long serialVersionUID = -1726899781642324413L;

	public NotFoundException() {
		super();
	}

	public NotFoundException(String key) {
		super(key);
	}

	public NotFoundException(String key, String arg1) {
		super(key, arg1);
	}

	public NotFoundException(String key, String arg1, String arg2) {
		super(key, arg1, arg2);
	}

	public NotFoundException(String key, String arg1, String arg2, String arg3) {
		super(key, arg1, arg2, arg3);
	}
}

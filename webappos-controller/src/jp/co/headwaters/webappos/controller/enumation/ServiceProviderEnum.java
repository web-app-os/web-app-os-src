package jp.co.headwaters.webappos.controller.enumation;

/**
 * OAuth ServiceProviderを表す列挙子
 */
public enum ServiceProviderEnum {

	FACEBOOK("fb"), //$NON-NLS-1$
	TWITTER("twtr"); //$NON-NLS-1$

	private String name;

	private ServiceProviderEnum(final String name) {
		this.name = name;
	}

	public static ServiceProviderEnum getServiceProvider(final String name) {
		for (ServiceProviderEnum e : ServiceProviderEnum.values()) {
			if (name.equals(e.getName())) {
				return e;
			}
		}
		throw new IllegalArgumentException(name);
	}

	public String getName() {
		return this.name;
	}
}

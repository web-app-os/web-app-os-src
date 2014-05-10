package jp.co.headwaters.webappos.controller.security;

import jp.co.headwaters.webappos.controller.ControllerConstants;

public abstract class CipherFactory {

	public static Cipher create(String tyep) {
		Cipher cipher = null;

		if (ControllerConstants.CIPHER_TYPE_BLOWFISH.equalsIgnoreCase(tyep)) {
			cipher = new Blowfish();
		} else if (ControllerConstants.CIPHER_TYPE_SMD5.equalsIgnoreCase(tyep)) {
			cipher = new SMD5();
		}
		return cipher;
	}
}

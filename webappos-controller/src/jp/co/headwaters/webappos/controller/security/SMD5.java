package jp.co.headwaters.webappos.controller.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;

public class SMD5 implements jp.co.headwaters.webappos.controller.security.Cipher {

	private static String _key = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_SECURITY_CIPHER_KEY);

	public String encrypt(String value) throws WebAppOSException {
		try {
			if (StringUtils.isEmpty(value)) {
				return null;
			}
			if (StringUtils.isEmpty(_key)) {
				throw new WebAppOSException("err.902"); //$NON-NLS-1$
			}
			byte[] hashedPassword = digest(value.getBytes(), _key.getBytes());
			return byte2Hex(hashedPassword);
		} catch (Exception e) {
			throw new WebAppOSException(e);
		}
	}

	public String decrypt(String value) throws WebAppOSException {
		return null;
	}

	private static byte[] digest(byte[] password, byte[] salt) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
		md.update(salt);
		md.update(password);
		return md.digest();
	}

	private static String byte2Hex(byte[] bytes) {
		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			if ((0xFF & bytes[i]) < 16) {
				hex.append("0"); //$NON-NLS-1$
			}
			hex.append(Integer.toHexString(0xFF & bytes[i]));
		}
		return hex.toString();
	}
}

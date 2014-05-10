package jp.co.headwaters.webappos.controller.security;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

public class Blowfish implements jp.co.headwaters.webappos.controller.security.Cipher {

	private static String _key = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_SECURITY_CIPHER_KEY);
	private static String _initiv = ControllerConstants.SECURITY_CIPHER_INIT_IV;
	private static String _algorithm = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_SECURITY_CIPHER_ALGORITHM);
	private static String _transformation = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_SECURITY_CIPHER_TRANSFORMATION);

	public String encrypt(String value) throws WebAppOSException {
		try {
			if (StringUtils.isEmpty(value)) {
				return null;
			}
			if (StringUtils.isEmpty(_key)) {
				throw new WebAppOSException("err.902"); //$NON-NLS-1$
			}
			SecretKeySpec sksSpec = new SecretKeySpec(_key.getBytes(), _algorithm);
			Cipher cipher = Cipher.getInstance(_transformation);
			AlgorithmParameterSpec iv = new IvParameterSpec(_initiv.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, sksSpec,iv);
			byte[] encrypted = cipher.doFinal(value.getBytes());
			return new String(Base64.encodeBase64(encrypted));
		} catch (Exception e) {
			throw new WebAppOSException(e);
		}
	}

	public String decrypt(String value) throws WebAppOSException {
		try {
			if (StringUtils.isEmpty(value)) {
				return null;
			}
			byte[] encrypted = Base64.decodeBase64(value.getBytes());
			SecretKeySpec sksSpec = new SecretKeySpec(_key.getBytes(), _algorithm);
			Cipher cipher = Cipher.getInstance(_transformation);
			AlgorithmParameterSpec iv = new IvParameterSpec(_initiv.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, sksSpec, iv);
			byte[] decrypted = cipher.doFinal(encrypted);
			return new String(decrypted);
		} catch (Exception e) {
			throw new WebAppOSException(e);
		}
	}
}

package jp.co.headwaters.webappos.controller.security;

import jp.co.headwaters.webappos.controller.exception.WebAppOSException;

public interface Cipher {

	public String encrypt(String text) throws WebAppOSException;

	public String decrypt(String text) throws WebAppOSException;
}

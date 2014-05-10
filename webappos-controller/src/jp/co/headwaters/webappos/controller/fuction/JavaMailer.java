package jp.co.headwaters.webappos.controller.fuction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;

public class JavaMailer {
	protected final Pattern ADDRESS_PATTERN = Pattern.compile("(.+)?<(.+)>"); //$NON-NLS-1$

	protected String charset = "ISO-2022-JP"; //$NON-NLS-1$

	protected String host;
	protected String port;
	protected String username;
	protected String password;
	protected Integer connectiontimeout;
	protected Integer timeout;

	protected InternetAddress from;
	protected InternetAddress[] to;
	protected InternetAddress[] cc;
	protected InternetAddress[] bcc;
	protected String subject;
	protected String body;

	/**
	 * 文字セットを設定します。デフォルトはISO-2022-JP
	 * @param charset 文字セットの識別名
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * サーバ情報を設定します
	 *
	 * @param host ホスト名
	 * @param port ポート名
	 * @param username ユーザ名。NULLまたは空白の場合は認証を行わない
	 * @param password パスワード。NULLまたは空白の場合は認証を行わない
	 */
	public void setServerInfo(String host, String port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	/**
	 * タイムアウトを設定します
	 *
	 * @param connectionTimeout 接続時のタイムアウトミリ秒。NULLの場合タイムアウト判定を行わない
	 * @param sendTimeout サーバへの送受信時のタイムアウトミリ秒。NULLの場合タイムアウト判定を行わない
	 */
	public void setTimeout(Integer connectionTimeout, Integer sendTimeout) {
		this.connectiontimeout = connectionTimeout;
		this.timeout = sendTimeout;
	}

	/**
	 * Fromを設定します
	 * @param address 送信者アドレス
	 * @param name 送信者名
	 * @throws AddressException
	 */
	public void setFrom(String address, String name) throws UnsupportedEncodingException, AddressException {
		if (StringUtils.isEmpty(name)) {
			this.from = new InternetAddress(address, name, this.charset);
		} else {
			this.from = new InternetAddress(address);
		}
	}

	/**
	 * toを追加します
	 * @param addresses 送信先アドレス<br>
	 * 表示名を指定する際は「表示名<アドレス>」の形式<br>
	 * 複数を一度に登録する際はカンマ(,)区切り
	 * @throws UnsupportedEncodingException
	 * @throws AddressException
	 */
	public void addTo(String addresses) throws AddressException, UnsupportedEncodingException {
		this.to = addInternetAddress(this.to, addresses);
	}

	/**
	 * ccを追加します
	 * @param addresses ccアドレス<br>
	 * 表示名を指定する際は「表示名<アドレス>」の形式<br>
	 * 複数を一度に登録する際はカンマ(,)区切り
	 * @throws UnsupportedEncodingException
	 * @throws AddressException
	 */
	public void addCc(String addresses) throws AddressException, UnsupportedEncodingException {
		this.cc = addInternetAddress(this.cc, addresses);
	}

	/**
	 * bccを追加します
	 * @param addresses bccアドレス<br>
	 * 表示名を指定する際は「表示名<アドレス>」の形式<br>
	 * 複数を一度に登録する際はカンマ(,)区切り
	 * @throws UnsupportedEncodingException
	 * @throws AddressException
	 */
	public void addBcc(String addresses) throws AddressException, UnsupportedEncodingException {
		this.bcc = addInternetAddress(this.bcc, addresses);
	}

	/**
	 * to,cc,bccをクリアします
	 */
	public void clearInternetAddress(){
		this.to = null;
		this.cc = null;
		this.bcc = null;
	}

	/**
	 * 題名を設定します
	 * @param subject 題名
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * 本文を設定します
	 * @param body 本文
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * プロパティの値に基づきメールを送信します
	 *
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public void send() throws MessagingException, UnsupportedEncodingException {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", this.host); //$NON-NLS-1$
		props.setProperty("mail.smtp.port", this.port); //$NON-NLS-1$
		if (!StringUtils.isEmpty(this.username) && !StringUtils.isEmpty(this.password)) {
			props.setProperty("mail.smtp.auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (this.connectiontimeout != null) {
			props.put( "mail.smtp.connectiontimeout", this.connectiontimeout); //$NON-NLS-1$
		}
		if (this.timeout != null) {
			props.put( "mail.smtp.timeout", this.timeout); //$NON-NLS-1$
		}
// TODO:メール for ssl
//		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");  // for ssl
//		props.put("mail.smtp.socketFactory.fallback", "false");  // for ssl
//		props.put("mail.smtp.socketFactory.port", port);      // for ssl

		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);

		try {
			message.setRecipients(RecipientType.TO, this.to);
			if (this.cc != null) {
				message.setRecipients(RecipientType.CC, this.cc);
			}
			if (this.bcc != null) {
				message.setRecipients(RecipientType.BCC, this.bcc);
			}
			if (this.from != null) {
				message.setFrom(this.from);
			}
			message.setSubject(this.subject, this.charset);
			message.setContent(this.body, "text/html; charset=" + this.charset);
//			message.setText(this.body, this.charset);

			// 設定の保存
			message.saveChanges();

			// 送信
			if (!StringUtils.isEmpty(this.username) && !StringUtils.isEmpty(this.password)) {
				Transport transport = session.getTransport("smtp"); //$NON-NLS-1$
				transport.connect(this.username, this.password);
				transport.sendMessage(message, message.getAllRecipients());
				transport.close();
			} else {
				Transport.send(message);
				message.writeTo(System.out);
			}
		} catch (MessagingException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * InternetAddress配列にアドレスを追加します
	 *
	 * @param prop 追加対象のプロパティ
	 * @param address 追加アドレス<br>
	 * 表示名を指定する際は<>で囲む<br>
	 * 複数を一度に登録する際はカンマ(,)区切り
	 * @return
	 * @throws AddressException
	 * @throws UnsupportedEncodingException
	 */
	private InternetAddress[] addInternetAddress(InternetAddress[] prop, String addresses)
			throws AddressException, UnsupportedEncodingException {
		List<InternetAddress> tmpAddress = new ArrayList<InternetAddress>();
		if (prop != null && prop.length > 0) {
			tmpAddress.addAll(Arrays.asList(prop));
		}
		String[] array = addresses.split(","); //$NON-NLS-1$

		for (String one : array) {
			Matcher m = this.ADDRESS_PATTERN.matcher(one);
			if (m.find()) {
				tmpAddress.add(new InternetAddress(m.group(2).trim(), m.group(1).trim(), this.charset));
			} else {
				tmpAddress.add(new InternetAddress(one.trim()));
			}
		}
		return tmpAddress.toArray(new InternetAddress[1]);
	}
}

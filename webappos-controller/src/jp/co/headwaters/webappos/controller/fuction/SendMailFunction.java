package jp.co.headwaters.webappos.controller.fuction;

import static jp.co.headwaters.webappos.controller.utils.ControllerUtils.*;
import static jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.SystemConstantKeys;
import jp.co.headwaters.webappos.controller.cache.SchemaColumnCache;
import jp.co.headwaters.webappos.controller.cache.SystemConstantCache;
import jp.co.headwaters.webappos.controller.cache.bean.AbstractExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.CrudBean;
import jp.co.headwaters.webappos.controller.cache.bean.FunctionBean;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.cache.bean.SystemConstantBean;
import jp.co.headwaters.webappos.controller.enumation.CrudEnum;
import jp.co.headwaters.webappos.controller.enumation.FunctionEnum;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.model.CommonExample;
import jp.co.headwaters.webappos.controller.model.CommonExample.Criteria;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.controller.utils.DaoUtils;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class SendMailFunction extends AbstractFunction {

	protected final Pattern SUBJECT_PATTERN = Pattern.compile("^Subject:(.+)?(\\r\\n|\\r|\\n)"); //$NON-NLS-1$
	protected final Pattern TO_PATTERN = Pattern.compile("^To:(.+)?(\\r\\n|\\r|\\n)"); //$NON-NLS-1$
	protected final Pattern CC_PATTERN = Pattern.compile("^Cc:(.+)?(\\r\\n|\\r|\\n)"); //$NON-NLS-1$
	protected final Pattern BCC_PATTERN = Pattern.compile("^Bcc:(.+)?(\\r\\n|\\r|\\n)"); //$NON-NLS-1$

	/** Mailer本体 */
	protected JavaMailer _mailer;

	/** FROMアドレス初期値 */
	protected String _mailFromAddress;
	/** FROM名初期値 */
	protected String _mailFromName;

	/** サイト名 */
	protected String _siteName;

	/** ハッシュマップ */
	protected Map<String, Object> _params;

	/** freeMarkerコンフィグ */
	protected Configuration _configuration;

	@Override
	protected void initialize() throws Exception {
		super.initialize();

		Map<String, SystemConstantBean> mailConstants = SystemConstantCache.getSystemConstantMap(SystemConstantKeys.CATEGORY_MAIL);
		if (mailConstants == null || mailConstants.size() == 0) {
			throw new WebAppOSException("err.300"); //$NON-NLS-1$
		}

		String charset = null;
		String host = null;
		String port = null;
		String userName = null;
		String password = null;
		String connectionTimeout = null;
		String timeout = null;
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_CHARSET)) {
			charset = mailConstants.get(SystemConstantKeys.KEY_MAIL_CHARSET).getValue();
		} else {
			throw new WebAppOSException("err.301", SystemConstantKeys.KEY_MAIL_CHARSET); //$NON-NLS-1$
		}
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_HOST)) {
			host = mailConstants.get(SystemConstantKeys.KEY_MAIL_HOST).getValue();
		} else {
			throw new WebAppOSException("err.301", SystemConstantKeys.KEY_MAIL_HOST); //$NON-NLS-1$
		}
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_PORT)) {
			port = mailConstants.get(SystemConstantKeys.KEY_MAIL_PORT).getValue();
		} else {
			throw new WebAppOSException("err.301", SystemConstantKeys.KEY_MAIL_PORT); //$NON-NLS-1$
		}
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_USERNAME)) {
			userName = mailConstants.get(SystemConstantKeys.KEY_MAIL_USERNAME).getValue();
		}
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_PASSWORD)) {
			password = mailConstants.get(SystemConstantKeys.KEY_MAIL_PASSWORD).getValue();
		}
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_CONNECTION_TIMEOUT)) {
			connectionTimeout = mailConstants.get(SystemConstantKeys.KEY_MAIL_CONNECTION_TIMEOUT).getValue();
		} else {
			throw new WebAppOSException("err.301", SystemConstantKeys.KEY_MAIL_CONNECTION_TIMEOUT); //$NON-NLS-1$
		}
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_TIMEOUT)) {
			timeout = mailConstants.get(SystemConstantKeys.KEY_MAIL_TIMEOUT).getValue();
		} else {
			throw new WebAppOSException("err.301", SystemConstantKeys.KEY_MAIL_TIMEOUT); //$NON-NLS-1$
		}
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_FROM_ADDRESS)) {
			this._mailFromAddress = mailConstants.get(SystemConstantKeys.KEY_MAIL_FROM_ADDRESS).getValue();
		} else {
			throw new WebAppOSException("err.301", SystemConstantKeys.KEY_MAIL_FROM_ADDRESS); //$NON-NLS-1$
		}
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_FROM_NAME)) {
			this._mailFromName = mailConstants.get(SystemConstantKeys.KEY_MAIL_FROM_NAME).getValue();
		} else {
			throw new WebAppOSException("err.301", SystemConstantKeys.KEY_MAIL_FROM_NAME); //$NON-NLS-1$
		}
		if (mailConstants.containsKey(SystemConstantKeys.KEY_MAIL_SITE_NAME)) {
			this._siteName = mailConstants.get(SystemConstantKeys.KEY_MAIL_SITE_NAME).getValue();
		} else {
			throw new WebAppOSException("err.301", SystemConstantKeys.KEY_MAIL_SITE_NAME); //$NON-NLS-1$
		}

		// メーラを設定する
		this._mailer = new JavaMailer();
		this._mailer.setCharset(charset);
		this._mailer.setServerInfo(host, port, userName, password);
		this._mailer.setTimeout(Integer.valueOf(connectionTimeout), Integer.valueOf(timeout));

		// freeMarkerコンフィグを設定する
		this._configuration = new Configuration();
		this._configuration.setDirectoryForTemplateLoading(new File(getMailTemplatePath()));
		this._configuration.setDefaultEncoding("utf-8"); //$NON-NLS-1$

		// freeMarkerで使うハッシュマップを初期化する
		this._params = new HashMap<String, Object>();
	}

	private String getMailTemplatePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_WEBAPPS_PATH));
		if (!sb.toString().endsWith(getFileSparator())) {
			sb.append(getFileSparator());
		}
		sb.append(ControllerConstants.WEBAPPS_MAIL_TEMPLATE_DIR);
		return sb.toString();
	}

	@Override
	protected void execute(AbstractExecuteBean function) throws Exception {
		FunctionBean mailFunction = (FunctionBean)function;
		if (!FunctionEnum.FUNCTION_SENDMAIL.getFunctionName().equalsIgnoreCase(mailFunction.getType())) {
			throw new WebAppOSException();
		}

		Map<String, Object> mailParams = new HashMap<String, Object>();
		String templateName = mailFunction.getMethod();

		if (StringUtils.isEmpty(mailFunction.getToIdColumnName())
				|| StringUtils.isEmpty(mailFunction.getToAddressColumnName())) {
			addAddress(mailFunction);
			createMailParamMap(function.getResult(), mailParams);
			sendMail(templateName, mailParams);
		} else {
			String[] addressColumnInfo = mailFunction.getToAddressColumnName().split(ControllerConstants.REGEX_COLUMN_DELIMITER);
			String addressTalbeName = addressColumnInfo[0].toUpperCase();
			String addressColumnName = addressColumnInfo[1].toUpperCase();

			String[] ids = mailFunction.getToIds();
			if (ids == null) {
				ids = getIdColParams(mailFunction);
				if (ids == null || ids.length == 0) {
					Map<Integer, Integer> idmap = this._ids.get(getSchemaColumnKey(addressTalbeName, ControllerConstants.PK_COLUMN_NAME));
					if (idmap != null && idmap.size() > 0) {
						ids = new String[idmap.size()];
						for (int i = 0; i < ids.length; i++) {
							ids[i] = String.valueOf(idmap.get(i));
						}
					}
					if (ids == null || ids.length == 0) {
						throw new WebAppOSException("err.302"); //$NON-NLS-1$
					}
				}
			}

			SchemaColumnBean addressColumn = SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(addressTalbeName, ControllerConstants.PK_COLUMN_NAME));

			List<Object> idList = new ArrayList<Object>();
			for (String val1 : ids) {
				for (String val2 : Arrays.asList(val1.split(","))) { //$NON-NLS-1$
					if (StringUtils.isEmpty(val2)) {
						continue;
					}
					idList.add(convertStringToDbType(val2, addressColumn));
				}
			}

			String mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_BY_EXAMPLE.getMethod(), addressTalbeName);
			CommonExample example = new CommonExample();
			Criteria criteria = example.createCriteria();
			criteria.andIn(ControllerConstants.PK_COLUMN_NAME, idList);

			// 送信アドレスを取得する
			List<?> entityList = this._sqlSession.selectList(mapperName, example);
			for (int i = 0; i < entityList.size(); i++) {
				mailParams = new HashMap<String, Object>();
				Map<String, Object> map = DaoUtils.convertEntityToMap(entityList.get(i), false);
				String address = (String) map.get(ControllerUtils.getResultMapKey(addressTalbeName, addressColumnName));
				// 送信アドレスを設定する
				addAddress(mailFunction);
				addTo(address);
				// メールテンプレートにbindするmapを生成する
				createMailParamMap(function.getResult(), mailParams);
				mailParams.put(addressTalbeName, map);
				sendMail(templateName, mailParams);
			}
		}

		if (!StringUtils.isEmpty(function.getTarget())) {
			// メール送信情報を登録する
			CrudBean insInfo = new CrudBean();
			insInfo.setMethod(CrudEnum.INSERT.getMethod());
			insInfo.setTarget(function.getTarget());
			insInfo.setResult(function.getResult());
			new CrudFunction().execute(this._requestParams, this._resultMap, insInfo);
		}
	}

	private String[] getIdColParams(FunctionBean function){
		StringBuilder sb = new StringBuilder();
		sb.append(function.getResult());
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_CRUD_COLUMN);
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
		sb.append(function.getToIdColumnName().toUpperCase());
		return this._requestParams.get(sb.toString());
	}

	private void createMailParamMap(String resultName, Map<String, Object> mailParams) {
		if (this._requestParams == null) return;
		for (Map.Entry<String, String[]> param : this._requestParams.entrySet()) {
			String[] values = param.getValue();
			String[] key = param.getKey().split(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER, 3);
			String daoParamKey = null;
			Object daoParamValue = null;

			if (key[0].equals(resultName)) {
				if (ControllerConstants.REQUEST_PARAM_NAME_CRUD_COLUMN.equals(key[1])) {
					daoParamKey = key[2].toUpperCase();
					daoParamValue = StringUtils.join(values, ","); //$NON-NLS-1$
					if (!mailParams.containsKey(daoParamKey)) {
						mailParams.put(daoParamKey, daoParamValue);
					}
				}
			}
		}
	}

	private void addAddress(FunctionBean function) throws AddressException,
			UnsupportedEncodingException {

		SystemConstantBean systemConstant = null;
		if (function.getToList() != null) {
			for (String value : function.getToList()) {
				if (StringUtils.isEmpty(value)) {
					continue;
				}
				String[] values = value.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
				if (values.length == 1) {
					systemConstant = SystemConstantCache.getSystemConstant(values[0]);
				} else {
					systemConstant = SystemConstantCache.getSystemConstant(values[0], values[1]);
				}
				// system_constantに定義されたアドレスを取得する
				if (systemConstant != null) {
					if (!StringUtils.isEmpty(systemConstant.getValue())) {
						addTo(systemConstant.getValue());
					}
				}
			}
		}

		if (function.getCcList() != null) {
			for (String value : function.getCcList()) {
				if (StringUtils.isEmpty(value)) {
					continue;
				}
				String[] values = value.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
				if (values.length == 1) {
					systemConstant = SystemConstantCache.getSystemConstant(values[0]);
				} else {
					systemConstant = SystemConstantCache.getSystemConstant(values[0], values[1]);
				}
				if (systemConstant != null) {
					if (!StringUtils.isEmpty(systemConstant.getValue())) {
						addCc(systemConstant.getValue());
					}
				}
			}
		}

		if (function.getBccList() != null) {
			for (String value : function.getBccList()) {
				if (StringUtils.isEmpty(value)) {
					continue;
				}
				String[] values = value.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
				if (values.length == 1) {
					systemConstant = SystemConstantCache.getSystemConstant(values[0]);
				} else {
					systemConstant = SystemConstantCache.getSystemConstant(values[0], values[1]);
				}

				if (systemConstant != null) {
					if (!StringUtils.isEmpty(systemConstant.getValue())) {
						addBcc(systemConstant.getValue());
					}
				}
			}
		}
	}

	/**
	 * DTOをハッシュマップに展開しメールを送信します
	 *
	 * @param templateName テンプレート名
	 * @param dto テンプレートへassignするDTO
	 * @throws Exception 例外
	 */
	public void sendMail(String templateName, Map<String, Object> obj) throws Exception {

		// デフォルト送信者情報をセットします
		this._mailer.setFrom(this._mailFromAddress, this._mailFromName);

		if (this._resultMap != null) {
			this._params.putAll(this._resultMap);
		}

		// プロパティの情報をハッシュマップにセットします
		assign("SITE_NAME", this._siteName); //$NON-NLS-1$

		// DTOをハッシュマップへ展開します
		// ※assignされているマップと衝突した際は上書きされるので注意
		this._params.putAll(obj);

		// テンプレートを解析します
		fetch(templateName, this._params);

		// 送信します
		this._mailer.send();
		this._mailer.clearInternetAddress();
	}

	/**
	 * Fromを設定します
	 * @param address 送信者アドレス
	 * @param name 送信者名
	 * @throws AddressException
	 */
	public void setFrom(String address, String name) throws UnsupportedEncodingException, AddressException {
		this._mailer.setFrom(address, name);
	}

	/**
	 * Toを追加します
	 * @param addresses 送信先アドレス<br>
	 * 表示名を指定する際は<>で囲む<br>
	 * 複数を一度に登録する際はカンマ(,)区切り
	 * @throws UnsupportedEncodingException
	 * @throws AddressException
	 */
	public void addTo(String addresses) throws AddressException, UnsupportedEncodingException {
		this._mailer.addTo(addresses);
	}

	/**
	 * Ccを追加します
	 * @param addresses ccアドレス<br>
	 * 表示名を指定する際は<>で囲む<br>
	 * 複数を一度に登録する際はカンマ(,)区切り
	 * @throws UnsupportedEncodingException
	 * @throws AddressException
	 */
	public void addCc(String addresses) throws AddressException, UnsupportedEncodingException {
		this._mailer.addCc(addresses);
	}

	/**
	 * BCcを追加します
	 * @param addresses bccアドレス<br>
	 * 表示名を指定する際は<>で囲む<br>
	 * 複数を一度に登録する際はカンマ(,)区切り
	 * @throws UnsupportedEncodingException
	 * @throws AddressException
	 */
	public void addBcc(String addresses) throws AddressException, UnsupportedEncodingException {
		this._mailer.addBcc(addresses);
	}

	// freeMarker用メソッド
	///////////////////////////////////////////////////////
	/**
	 * ハッシュマップに値を設定します
	 * @param key
	 * @param value
	 */
	public void assign(String key, Object value) {
		this._params.put(key.toUpperCase(), value);
	}

	/**
	 * freeMarkerテンプレートファイルを取得する
	 *
	 * @param templateName テンプレート名
	 * @return テンプレートファイル
	 * @throws IOException
	 */
	protected Template getTemplate(String templateName) throws IOException {
		// テンプレートファイルを取得
		StringBuilder sb = new StringBuilder(templateName);
		sb.append(".ftl"); //$NON-NLS-1$
		return this._configuration.getTemplate(sb.toString());
	}

	/**
	 * freeMarkerからテンプレート情報を取得してフィールドに設定する
	 *
	 * @param templateName テンプレート名
	 * @param map freeMakerへ送るハッシュマップ
	 * @throws IOException
	 * @throws TemplateException
	 * @throws AddressException
	 * @throws Exception
	 */
	protected void fetch(String templateName, Map<String, Object> map) throws IOException, TemplateException,
			AddressException {
		Template template = getTemplate(templateName);
		Matcher m;

		map = ControllerUtils.toHtmlString(map);

		StringWriter out = new StringWriter();
		template.process(map, out);
		String message = out.toString();

		// Subjectを取得
		m = this.SUBJECT_PATTERN.matcher(message);
		if (m.find()) {
			this._mailer.setSubject(m.group(1).trim());
			message = m.replaceFirst(""); //$NON-NLS-1$
		}
		// Toを取得
		m = this.TO_PATTERN.matcher(message);
		if (m.find()) {
			addTo(m.group(1));
			message = m.replaceFirst(""); //$NON-NLS-1$
		}
		// Ccを取得
		m = this.CC_PATTERN.matcher(message);
		if (m.find()) {
			addCc(m.group(1));
			message = m.replaceFirst(""); //$NON-NLS-1$
		}
		// Bccを取得
		m = this.BCC_PATTERN.matcher(message);
		if (m.find()) {
			addBcc(m.group(1));
			message = m.replaceFirst(""); //$NON-NLS-1$
		}

		// 先頭と末尾の改行をトリムする
		m = Pattern.compile("^(\\r\\n|\\r)+|(\\r\\n|\\r)+$").matcher(message); //$NON-NLS-1$
		if (m.find()) {
			message = m.replaceAll(""); //$NON-NLS-1$
		}
		message = message.replace('－','\u2212')
						.replace('―','\u2014')
						.replace('～','\u301C')
						.replace('∥','\u2016')
						.replace('￠','\u00A2')
						.replace('￡','\u00A3')
						.replace('￢','\u00AC');
		this._mailer.setBody(message);
	}
}

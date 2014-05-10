package jp.co.headwaters.webappos.controller.utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.SchemaColumnCache;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.enumation.DataTypeEnum;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.security.Cipher;
import jp.co.headwaters.webappos.controller.security.CipherFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;

public class ConvertDateTypeUtils {

	public static String convertDbTypeToString(Object obj) {
		if (obj == null) return null;

		String result = null;
		if (obj.getClass().toString().equals(Date.class.toString())) {
			SimpleDateFormat fomatter = new SimpleDateFormat(ControllerConstants.FORMAT_PATTERN_TIMESTAMP);
			result = fomatter.format((Date)obj);
		} else if (obj.getClass().toString().equals(Boolean.class.toString())) {
			if ((Boolean)obj){
				result = ControllerConstants.RESULT_VALUE_TRUE;
			} else {
				result = ControllerConstants.RESULT_VALUE_FALSE;
			}
		} else {
			result = String.valueOf(obj);
		}

		return result;
	}

	public static Object convertStringToDbType(String tableName, String columnName, String value) throws WebAppOSException {
		SchemaColumnBean schemaColumn = SchemaColumnCache.getSchemaColumn(ControllerUtils.getSchemaColumnKey(tableName, columnName));
		return convertStringToDbType(value, schemaColumn);
	}

	public static Object convertStringToDbType(String value, SchemaColumnBean schemaColumn) throws WebAppOSException {
		if (schemaColumn == null) {
			return null;
		}
		if (schemaColumn.getColumnComment() != null) {
			if (value == null){
				if (!StringUtils.isEmpty(schemaColumn.getColumnComment().getRandom())) {
					value =  RandomStringUtils.randomAlphanumeric(Integer.parseInt(schemaColumn.getColumnComment().getRandom()));
				}
			}
			if (!StringUtils.isEmpty(schemaColumn.getColumnComment().getCipher())) {
				Cipher cipher = CipherFactory.create(schemaColumn.getColumnComment().getCipher());
				return cipher.encrypt(value);
			}
		}
		return ConvertDateTypeUtils.convertStringToDbType(value, schemaColumn.getDataType().toLowerCase());
	}

	public static Object convertStringToDbType(String value, String dataType) throws WebAppOSException {
		if (value == null || dataType == null) {
			return null;
		}

		Object result = null;
		if (DataTypeEnum.DATA_TYPE_SMALLINT.getDataType().equals(dataType)
				|| DataTypeEnum.DATA_TYPE_INTEGER.getDataType().equals(dataType)
				|| DataTypeEnum.DATA_TYPE_BIGINT.getDataType().equals(dataType)
				|| DataTypeEnum.DATA_TYPE_NUMERIC.getDataType().equals(dataType)
				|| DataTypeEnum.DATA_TYPE_REAL.getDataType().equals(dataType)
				|| DataTypeEnum.DATA_TYPE_DOUBLE_PRECISION.getDataType().equals(dataType)) {
			result = convertStringToNumber(value, dataType);
		} else if (DataTypeEnum.DATA_TYPE_CHARACTER_VARYING.getDataType().equals(dataType)
				|| DataTypeEnum.DATA_TYPE_CHARACTER.getDataType().equals(dataType)
				|| DataTypeEnum.DATA_TYPE_TEXT.getDataType().equals(dataType)) {
			result = value;
		} else if (DataTypeEnum.DATA_TYPE_TIMESTAMP_WITHOUT_TIME_ZONE.getDataType().equals(dataType)
				|| (DataTypeEnum.DATA_TYPE_TIMESTAMP_WITH_TIME_ZONE.getDataType().equals(dataType))
				|| (DataTypeEnum.DATA_TYPE_DATE.getDataType().equals(dataType))) {
			result = convertStringToDate(value);
		} else if (DataTypeEnum.DATA_TYPE_BYTEA.getDataType().equals(dataType)) {
			result = value.getBytes();
		} else if (DataTypeEnum.DATA_TYPE_BOOLEAN.getDataType().equals(dataType)) {
			if (value.toString().equals("1") || value.toString().equals("t")) {
				result = Boolean.TRUE;
			} else {
				result = Boolean.valueOf(value.toString());
			}
		} else if (DataTypeEnum.DATA_TYPE_BIT.getDataType().equals(dataType)
				|| DataTypeEnum.DATA_TYPE_BIT_VARYING.getDataType().equals(dataType)) {
		    try {
				PGobject obj = new PGobject();
			    obj.setType("bit"); //$NON-NLS-1$
				obj.setValue(value.toString());
			    result = obj;
			} catch (SQLException e) {
				throw new WebAppOSException(e);
			}
		} else {
			throw new WebAppOSException("err.900"); //$NON-NLS-1$
		}
		return result;
	}

	public static Date convertStringToDate(String value) {
		Calendar cal = Calendar.getInstance();
		cal.setLenient(false);

		value = getDefualtPatternString(value);

		int yyyy = Integer.parseInt(value.substring(0, 4));
		int MM = Integer.parseInt(value.substring(5, 7));
		int dd = Integer.parseInt(value.substring(8, 10));
		int HH = Integer.parseInt(value.substring(11, 13));
		int mm = Integer.parseInt(value.substring(14, 16));
		int ss = Integer.parseInt(value.substring(17, 19));
		int SSS = Integer.parseInt(value.substring(20, 23));
		cal.clear();
		cal.set(yyyy, MM - 1, dd);
		cal.set(Calendar.HOUR_OF_DAY, HH);
		cal.set(Calendar.MINUTE, mm);
		cal.set(Calendar.SECOND, ss);
		cal.set(Calendar.MILLISECOND, SSS);
		return cal.getTime();
	}

	/**
	 * 様々な日付、時刻文字列をデフォルトの日付フォーマットへ変換します。
	 *
	 * ●デフォルトの日付フォーマットは以下になります。
	 *     日付+時刻の場合：yyyy/MM/dd HH:mm:ss.SSS
	 *
	 * @param target 変換対象の文字列
	 * @return デフォルトの日付フォーマット
	 * @throws IllegalArgumentException
	 */
	private static String getDefualtPatternString(String target) {
		if (StringUtils.isEmpty(target)) {
			return null;
		}

		StringBuffer result = new StringBuffer();
		String yyyy = null;
		String MM = "01"; //$NON-NLS-1$
		String dd = "01"; //$NON-NLS-1$
		String HH = "00"; //$NON-NLS-1$
		String mm = "00"; //$NON-NLS-1$
		String ss = "00"; //$NON-NLS-1$
		String SSS = "000"; //$NON-NLS-1$

		// "-" or "/" が無い場合
		if (target.indexOf("/") == -1 && target.indexOf("-") == -1) { //$NON-NLS-1$ //$NON-NLS-2$
			if (target.length() >= 4) {
				yyyy = target.substring(0, 4);
			}
			if (target.length() >= 6) {
				MM = target.substring(4, 6);
			}
			if (target.length() >= 8) {
				dd = target.substring(6, 8);
			}
			if (target.length() >= 10){
				HH = target.substring(8, 10);
			}
			if (target.length() >= 12){
				mm = target.substring(10, 12);
			}
			if (target.length() >= 14){
				ss = target.substring(12, 14);
			}
			if (target.length() >= 17){
				SSS = target.substring(14, 17);
			}
		} else {
			StringTokenizer token = new StringTokenizer(target, "_/-:. "); //$NON-NLS-1$
			for (int i = 0; token.hasMoreTokens(); i++) {
				String temp = token.nextToken();
				switch (i) {
				case 0:// 年の部分
					if (temp.length() == 2) {
						yyyy = String.format("20%s", temp); //$NON-NLS-1$
					} else {
						yyyy = temp;
					}
					break;
				case 1:// 月の部分
					MM = String.format("%1$02d", Integer.parseInt(temp)); //$NON-NLS-1$
					break;
				case 2:// 日の部分
					dd = String.format("%1$02d", Integer.parseInt(temp)); //$NON-NLS-1$
					break;
				case 3:// 時間の部分
					HH = String.format("%1$02d", Integer.parseInt(temp)); //$NON-NLS-1$
					break;
				case 4:// 分の部分
					mm = String.format("%1$02d", Integer.parseInt(temp)); //$NON-NLS-1$
					break;
				case 5:// 秒の部分
					ss = String.format("%1$02d", Integer.parseInt(temp)); //$NON-NLS-1$
					break;
				case 6:// ミリ秒の部分
					SSS = String.format("%1$03d", Integer.parseInt(temp)); //$NON-NLS-1$
					break;
				}
			}
		}

		result.append(yyyy);
		result.append("/"); //$NON-NLS-1$
		result.append(MM);
		result.append("/"); //$NON-NLS-1$
		result.append(dd);
		result.append(" "); //$NON-NLS-1$
		result.append(HH);
		result.append(":"); //$NON-NLS-1$
		result.append(mm);
		result.append(":"); //$NON-NLS-1$
		result.append(ss);
		result.append("."); //$NON-NLS-1$
		result.append(SSS);

		return result.toString();
	}

	public static Number convertStringToNumber(String value, String dataType) {
		Number result = null;
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		if (DataTypeEnum.DATA_TYPE_SMALLINT.getDataType().equals(dataType)) {
			result = Short.valueOf(value);
		} else if (DataTypeEnum.DATA_TYPE_INTEGER.getDataType().equals(dataType)) {
			result = Integer.valueOf(value);
		} else if (DataTypeEnum.DATA_TYPE_BIGINT.getDataType().equals(dataType)) {
			result = Long.valueOf(value);
		} else if (DataTypeEnum.DATA_TYPE_NUMERIC.getDataType().equals(dataType)) {
			result = new BigDecimal(value);
		} else if (DataTypeEnum.DATA_TYPE_REAL.getDataType().equals(dataType)) {
			result = Float.valueOf(value);
		} else if (DataTypeEnum.DATA_TYPE_DOUBLE_PRECISION.getDataType().equals(dataType)) {
			result = Double.valueOf(value);
		}
		return result;
	}
}

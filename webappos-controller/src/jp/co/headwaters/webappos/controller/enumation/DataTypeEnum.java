package jp.co.headwaters.webappos.controller.enumation;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DBのデータ型を表す列挙子
 */
public enum DataTypeEnum {

	DATA_TYPE_SMALLINT("smallint", "SMALLINT", Short.class.getName()),
	DATA_TYPE_INTEGER("integer", "INTEGER", Integer.class.getName()),
	DATA_TYPE_BIGINT("bigint", "BIGINT", Long.class.getName()),
	DATA_TYPE_NUMERIC("numeric", "NUMERIC", BigDecimal.class.getName()),
	DATA_TYPE_REAL("real", "REAL", Float.class.getName()),
	DATA_TYPE_DOUBLE_PRECISION("double precision", "DOUBLE", Double.class.getName()),
	DATA_TYPE_CHARACTER_VARYING("character varying", "VARCHAR", String.class.getName()),
	DATA_TYPE_CHARACTER("character", "CHAR", String.class.getName()),
	DATA_TYPE_TEXT("text", "LONGVARCHAR", String.class.getName()),
	DATA_TYPE_TIMESTAMP_WITHOUT_TIME_ZONE("timestamp without time zone", "TIMESTAMP", Date.class.getName()),
	DATA_TYPE_TIMESTAMP_WITH_TIME_ZONE("timestamp with time zone", "TIMESTAMP", Date.class.getName()),
	DATA_TYPE_DATE("date", "DATE", Date.class.getName()),
	DATA_TYPE_BYTEA("bytea", "BINARY", "byte[]"),
	DATA_TYPE_BOOLEAN("boolean", "BOOLEAN", Boolean.class.getName()),
	DATA_TYPE_BIT("bit", "BIT", Boolean.class.getName()),
	DATA_TYPE_BIT_VARYING("bit varying", "BIT", Boolean.class.getName()),
	DATA_TYPE_RECORD("record", null, null),
	DATA_TYPE_MAP("map", null, null),
	DATA_TYPE_LIST("list", null, null),
	DATA_TYPE_VOID("void", null, null);

	private String type;
	private String jdbcType;
	private String clazz;

	private DataTypeEnum(final String type, final String jdbcType, final String clazz) {
		this.type = type;
		this.jdbcType = jdbcType;
		this.clazz = clazz;
	}

	public static DataTypeEnum getDataType(final String type) {
		for (DataTypeEnum e : DataTypeEnum.values()) {
			if (type.equals(e.getDataType())) {
				return e;
			}
		}
		throw new IllegalArgumentException(type);
	}

	public String getDataType() {
		return this.type;
	}

	public String getJdbcType() {
		return this.jdbcType;
	}

	public String getClazz() {
		return this.clazz;
	}
}

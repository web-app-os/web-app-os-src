 package jp.co.headwaters.webappos.generator.mybatis.plugin;

import static jp.co.headwaters.webappos.generator.utils.DataBaseUtils.*;
import static org.mybatis.generator.internal.util.JavaBeansUtil.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.cache.bean.RelationKeyBean;
import jp.co.headwaters.webappos.generator.GeneratorConstants;
import jp.co.headwaters.webappos.generator.mybatis.bean.JoinElementInfo;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;
import jp.co.headwaters.webappos.generator.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;

public class AddRelationPlugin extends PluginAdapter {

	private static final Log _logger = LogFactory.getLog(AddRelationPlugin.class);

	private Map<String, List<RelationKeyBean>> _exportedKeys = null;
	private Map<String, List<RelationKeyBean>> _importedKeys = null;

	private boolean _isError;

	private XmlElement _selectByExampleElement;

	@Override
	public void setContext(Context context) {
		super.setContext(context);

		CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
		commentConfig.addProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS, Boolean.TRUE.toString());
		context.setCommentGeneratorConfiguration(commentConfig);

		this._isError = !getRelationInfo(context);
	}

	@Override
    public void setProperties(Properties properties) {
		Properties conf = new Properties();
		try {
			conf.load(this.getClass().getResourceAsStream(ControllerConstants.PATH_DELIMITER + GeneratorConstants.PROPERTY_FILE_NAME));
		} catch (IOException e) {
			_logger.error(MessageUtils.getString("err.100"), e); //$NON-NLS-1$
		}
		super.setProperties(conf);
	}

	@Override
	public boolean validate(List<String> warnings) {
		boolean valid = true;

		if (this.properties.size() == 0) {
			warnings.add(MessageUtils.getString("err.001")); //$NON-NLS-1$
			valid = false;
		}

		if (this._isError) {
			valid = false;
		}
		return valid;
	}

	/**
	 * Modelクラスに関連があるクラスを追加するように拡張する
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {

		String tableName = introspectedTable.getTableConfiguration().getTableName();

		// 自分が親
		List<RelationKeyBean> exportedKey = this._exportedKeys.get(tableName);
		if (exportedKey != null) {
			for (RelationKeyBean relationKeys : exportedKey) {
				String property = getCamelCaseString(relationKeys.getFkTableName(), false);
				FullyQualifiedJavaType javaType = getExportedJavaType(relationKeys.getFkTableName());
				topLevelClass.addField(getJavaBeansField(javaType, property));
				topLevelClass.addMethod(getJavaBeansGetter(javaType, property));
				topLevelClass.addMethod(getJavaBeansSetter(javaType, property));
				topLevelClass.addImportedType("java.util.List"); //$NON-NLS-1$
			}
		}

		// 自分が子
		List<RelationKeyBean> importedKey = this._importedKeys.get(tableName);
		if (importedKey != null) {
			for (RelationKeyBean relationKeys : importedKey) {
				String property = getCamelCaseString(relationKeys.getPkTableName(), false);
				FullyQualifiedJavaType javaType = getImportedJavaType(relationKeys.getPkTableName());
				topLevelClass.addField(getJavaBeansField(javaType, property));
				topLevelClass.addMethod(getJavaBeansGetter(javaType, property));
				topLevelClass.addMethod(getJavaBeansSetter(javaType, property));
			}
		}
		return true;
	}

	/**
	 * 利用しない為、Exampleクラスは作成しないように拡張する
	 */
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		return false;
	}

	/**
	 * columnにテーブル名を付与するように拡張する
	 */
	@Override
	public boolean sqlMapResultMapWithoutBLOBsElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		String tableName = introspectedTable.getTableConfiguration().getTableName();
		StringBuilder newColumn = new StringBuilder();
		for (Element e : element.getElements()) {
			XmlElement resultElement = (XmlElement)e;
			for (Attribute attribute : resultElement.getAttributes()) {
				if ("column".equalsIgnoreCase(attribute.getName())) { //$NON-NLS-1$
					newColumn.setLength(0);
					newColumn.append(tableName);
					newColumn.append("_"); //$NON-NLS-1$
					newColumn.append(attribute.getValue());
					resultElement.getAttributes().remove(attribute);
					resultElement.addAttribute(new Attribute("column", newColumn.toString())); //$NON-NLS-1$
					break;
				}
			}
		}
		return true;
	}

	/**
	 * Base_Column_Listにテーブル名を付与するように拡張する
	 */
	@Override
	public boolean sqlMapBaseColumnListElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		String tableName = introspectedTable.getTableConfiguration().getTableName();
		StringBuilder orgColumns = new StringBuilder();
		StringBuilder newColumns = new StringBuilder();
		for (Element e : element.getElements()) {
			orgColumns.append(((TextElement) e).getContent());
		}
		element.getElements().removeAll(element.getElements());
		String[] columns = orgColumns.toString().split(","); //$NON-NLS-1$
		for (int i = 0; i < columns.length; i++) {
			newColumns.append(tableName);
			newColumns.append("."); //$NON-NLS-1$
			newColumns.append(columns[i].trim());
			newColumns.append(" as "); //$NON-NLS-1$
			newColumns.append(tableName);
			newColumns.append("_"); //$NON-NLS-1$
			newColumns.append(columns[i].trim());
			if (i != columns.length - 1) {
				newColumns.append(", "); //$NON-NLS-1$
			}
			if (newColumns.length() > 80) {
				element.addElement(new TextElement(newColumns.toString()));
				newColumns.setLength(0);
			}
		}
		if (newColumns.length() > 0) {
			element.addElement((new TextElement(newColumns.toString())));
		}
		return true;
	}

	/**
	 * parameterTypeを汎用Exampleクラスに変更する
	 */
	@Override
	public boolean sqlMapCountByExampleElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		updateParameterTypeToGenericExample(element);
		return true;
	}

	/**
	 * parameterTypeを汎用Exampleクラスに変更する
	 */
	@Override
	public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		updateParameterTypeToGenericExample(element);
		return true;
	}

	/**
	 * 関連テーブルのマッピング設定を追加するように拡張する
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document,
			IntrospectedTable introspectedTable) {

		// ------------------------------------------------------------------
		// resultMap Element
		// ------------------------------------------------------------------
		XmlElement answer = new XmlElement("resultMap"); //$NON-NLS-1$
		answer.addAttribute(new Attribute("id", GeneratorConstants.MYBATIS_RESULT_MAP_ID_FULL)); //$NON-NLS-1$
		answer.addAttribute(new Attribute("extends", introspectedTable.getBaseResultMapId())); //$NON-NLS-1$
		answer.addAttribute(new Attribute("type", introspectedTable.getBaseRecordType())); //$NON-NLS-1$
		document.getRootElement().addElement(answer);

		String tableName = introspectedTable.getTableConfiguration().getTableName();
		StringBuilder sb = new StringBuilder();
		String property = null;

		// 自分が子
		List<RelationKeyBean> importedKey = this._importedKeys.get(tableName);
		if (importedKey != null) {
			for (RelationKeyBean relationKeys : importedKey) {
				property = relationKeys.getPkTableName();

				sb.setLength(0);
				sb.append(getMapperName(introspectedTable, property));
				sb.append("."); //$NON-NLS-1$
				sb.append(GeneratorConstants.MYBATIS_RESULT_MAP_ID_REF_PREFIX);
				sb.append(getCamelCaseString(tableName, true));

				XmlElement element = new XmlElement("association"); //$NON-NLS-1$
				element.addAttribute(new Attribute("property", getCamelCaseString(property, false))); //$NON-NLS-1$
				element.addAttribute(new Attribute("resultMap", sb.toString())); //$NON-NLS-1$
				answer.addElement(element);
			}
		}

		// 自分が親
		List<RelationKeyBean> exportedKey = this._exportedKeys.get(tableName);
		if (exportedKey != null) {
			List<XmlElement> resultMapElement = new ArrayList<XmlElement>();
			for (RelationKeyBean relationKeys : exportedKey) {
				property = relationKeys.getFkTableName();

				sb.setLength(0);
				sb.append(getMapperName(introspectedTable, property));
				sb.append("."); //$NON-NLS-1$
				sb.append("BaseResultMap");
//				sb.append(GeneratorConstants.MYBATIS_RESULT_MAP_ID_REF_PREFIX);
//				sb.append(getCamelCaseString(tableName, true));

				XmlElement element = new XmlElement("collection"); //$NON-NLS-1$
				element.addAttribute(new Attribute("property", getCamelCaseString(property, false))); //$NON-NLS-1$
				element.addAttribute(new Attribute("resultMap", sb.toString())); //$NON-NLS-1$
				answer.addElement(element);

				resultMapElement.add(element);
			}

			for (XmlElement element : resultMapElement) {
				for (Attribute attribute : element.getAttributes()) {
					if ("property".equals(attribute.getName())) { //$NON-NLS-1$
						property = attribute.getValue();

						sb.setLength(0);
						sb.append(GeneratorConstants.MYBATIS_RESULT_MAP_ID_REF_PREFIX);
						sb.append(StringUtils.capitalize(property));

						XmlElement resultElement = new XmlElement("resultMap"); //$NON-NLS-1$
						resultElement.addAttribute(new Attribute("id", sb.toString())); //$NON-NLS-1$
						resultElement.addAttribute(new Attribute("extends", introspectedTable.getBaseResultMapId())); //$NON-NLS-1$
						resultElement.addAttribute(new Attribute("type", introspectedTable.getBaseRecordType())); //$NON-NLS-1$
						document.getRootElement().addElement(resultElement);
//
//						for (XmlElement element2 : resultMapElement) {
//							for (Attribute attribute2 : element2.getAttributes()) {
//								if ("property".equals(attribute2.getName())) { //$NON-NLS-1$
//									if (!property.equals(attribute2.getValue())) {
//										resultElement.addElement(element2);
//									}
//									break;
//								}
//							}
//						}
					}
				}
			}
		}

		// 関連情報を取得する
		List<JoinElementInfo> exportedJoinElementInfo = createJoinElementInfo(introspectedTable, exportedKey);
		List<JoinElementInfo> importedJoinElementInfo = createJoinElementInfo(introspectedTable, importedKey);

		// ------------------------------------------------------------------
		// selectAllByPrimaryKey Element
		// ------------------------------------------------------------------
		if (introspectedTable.getRules().generateSelectByPrimaryKey()) {
			document.getRootElement().addElement(getSelectAllByPrimaryKeyElement(introspectedTable, importedJoinElementInfo));
		}

		// ------------------------------------------------------------------
		// selectAllByExample Element
		// ------------------------------------------------------------------
		XmlElement selectAllElement = getSelectAllByExample(exportedJoinElementInfo, importedJoinElementInfo);
		document.getRootElement().addElement(selectAllElement);

		// ------------------------------------------------------------------
		// countAllByExample Element
		// ------------------------------------------------------------------
		document.getRootElement().addElement(getCountAllByExample(selectAllElement, introspectedTable));

		return true;
	}

	/**
	 * 使用しない為、削除する
	 */
	@Override
	public boolean sqlMapInsertElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		return false;
	}

	/**
	 * parameterTypeをmapに変更し、mapのkeyをrecordとする
	 */
	@Override
	public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		updateParameterTypeToMap(element);
		element.addAttribute(new Attribute("useGeneratedKeys", "true")); //$NON-NLS-1$ //$NON-NLS-2$
		element.addAttribute(new Attribute("keyProperty", getKeyProperty())); //$NON-NLS-1$

		XmlElement columnElement = (XmlElement) element.getElements().get(1);
		for (Element e : columnElement.getElements()) {
			Attribute orgAttribute = ((XmlElement) e).getAttributes().get(0);
			StringBuilder sb = new StringBuilder();
			sb.append(GeneratorConstants.MYBATIS_MAP_RECORD_KEY);
			sb.append("."); //$NON-NLS-1$
			sb.append(orgAttribute.getValue());
			((XmlElement) e).getAttributes().remove(0);
			((XmlElement) e).getAttributes().add(0, new Attribute("test", sb.toString())); //$NON-NLS-1$
		}

		XmlElement valueElement = (XmlElement) element.getElements().get(2);
		for (Element e : valueElement.getElements()) {
			Attribute orgAttribute = ((XmlElement) e).getAttributes().get(0);
			StringBuilder newValue = new StringBuilder();
			newValue.append(GeneratorConstants.MYBATIS_MAP_RECORD_KEY);
			newValue.append("."); //$NON-NLS-1$
			newValue.append(orgAttribute.getValue());
			((XmlElement) e).getAttributes().remove(0);
			((XmlElement) e).getAttributes().add(0, new Attribute("test", newValue.toString())); //$NON-NLS-1$

			Element orgElement = ((XmlElement) e).getElements().get(0);
			StringBuilder newContent = new StringBuilder();
			newContent.append("#{"); //$NON-NLS-1$
			newContent.append(GeneratorConstants.MYBATIS_MAP_RECORD_KEY);
			newContent.append("."); //$NON-NLS-1$
			newContent.append(orgElement.getFormattedContent(0).substring(2));
			((XmlElement) e).getElements().remove(0);
			((XmlElement) e).getElements().add(0, new TextElement(newContent.toString()));
		}
		return true;
	}

	/**
	 * parameterTypeを汎用Exampleクラスに変更する
	 */
	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		updateParameterTypeToGenericExample(element);
		this._selectByExampleElement = element;
		return true;
	}

	/**
	 * 使用しない為、削除する
	 */
	public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

    /**
	 * parameterTypeをmapに変更し、mapのkeyをrecordとする
	 */
	public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		updateParameterTypeToMap(element);

		XmlElement setElement = (XmlElement) element.getElements().get(1);
		for (Element e : setElement.getElements()) {
			Attribute orgAttribute = ((XmlElement) e).getAttributes().get(0);
			StringBuilder newValue = new StringBuilder();
			newValue.append(GeneratorConstants.MYBATIS_MAP_RECORD_KEY);
			newValue.append("."); //$NON-NLS-1$
			newValue.append(orgAttribute.getValue());
			((XmlElement) e).getAttributes().remove(0);
			((XmlElement) e).getAttributes().add(0, new Attribute("test", newValue.toString())); //$NON-NLS-1$

			Element orgElement = ((XmlElement) e).getElements().get(0);
			String content = orgElement.getFormattedContent(0);
			StringBuilder newContent = new StringBuilder();
			newContent.append(content.substring(0, content.indexOf("{") - 1)); //$NON-NLS-1$
			newContent.append("#{"); //$NON-NLS-1$
			newContent.append(GeneratorConstants.MYBATIS_MAP_RECORD_KEY);
			newContent.append("."); //$NON-NLS-1$
			newContent.append(content.substring(content.indexOf("{") + 1)); //$NON-NLS-1$
			((XmlElement) e).getElements().remove(0);
			((XmlElement) e).getElements().add(0, new TextElement(newContent.toString()));
		}
		return true;
	}

	/**
	 * PrimaryKey、createdは更新できないようにする
	 */
	public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {

		for (int i = element.getElements().size() - 1; i >= 1; i--) {
			Element orgElement = element.getElements().get(i);
			if (orgElement instanceof TextElement) {
				element.getElements().remove(orgElement);
			}
		}

		StringBuilder sb = new StringBuilder();
        sb.append("set "); //$NON-NLS-1$
		Iterator<IntrospectedColumn> iter = introspectedTable.getNonBLOBColumns().iterator();
		while (iter.hasNext()) {
			IntrospectedColumn introspectedColumn = iter.next();
			if (introspectedTable.getPrimaryKeyColumns().contains(introspectedColumn)) {
				continue;
			}
			if (introspectedColumn.getActualColumnName().equalsIgnoreCase(ControllerConstants.CREATED_COLUMN_NAME)) {
				continue;
			}
			sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
			sb.append(" = "); //$NON-NLS-1$
			sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "record.")); //$NON-NLS-1$

			if (iter.hasNext()) {
				sb.append(',');
			}

			element.addElement(element.getElements().size() - 1, new TextElement(sb.toString()));

			// set up for the next column
			if (iter.hasNext()) {
				sb.setLength(0);
				OutputUtilities.xmlIndent(sb, 1);
			}
		}
		return true;
	}

	private boolean getRelationInfo(Context context) {
		Connection connection = null;
		ResultSet exportResult = null;
		ResultSet importeResult = null;

		this._exportedKeys = new HashMap<String, List<RelationKeyBean>>();
		this._importedKeys = new HashMap<String, List<RelationKeyBean>>();

		try {
			connection = getConnection(context.getJdbcConnectionConfiguration());
			DatabaseMetaData meta = connection.getMetaData();
			for (TableConfiguration tc : context.getTableConfigurations()) {

				// 自分が親
				exportResult = meta.getExportedKeys(tc.getCatalog(), tc.getSchema(), tc.getTableName());
				while (exportResult.next()) {
					if (exportResult.getString("pktable_name") == null) { //$NON-NLS-1$
						continue;
					}
					RelationKeyBean relationKeys = new RelationKeyBean();
					String pkTableName = exportResult.getString("pktable_name"); //$NON-NLS-1$
					relationKeys.setPkTableName(pkTableName);
					relationKeys.setPkColumnName(exportResult.getString("pkcolumn_name")); //$NON-NLS-1$
					relationKeys.setFkTableName(exportResult.getString("fktable_name")); //$NON-NLS-1$
					relationKeys.setFkColumnName(exportResult.getString("fkcolumn_name")); //$NON-NLS-1$
					if (this._exportedKeys.get(pkTableName) == null) {
						this._exportedKeys.put(pkTableName, new ArrayList<RelationKeyBean>());
					}
					this._exportedKeys.get(pkTableName).add(relationKeys);
				}

				// 自分が子
				importeResult = meta.getImportedKeys(tc.getCatalog(), tc.getSchema(), tc.getTableName());
				while (importeResult.next()) {
					if (importeResult.getString("fktable_name") == null) { //$NON-NLS-1$
						continue;
					}
					RelationKeyBean relationKeys = new RelationKeyBean();
					String fkTableName = importeResult.getString("fktable_name"); //$NON-NLS-1$
					relationKeys.setPkTableName(importeResult.getString("pktable_name")); //$NON-NLS-1$
					relationKeys.setPkColumnName(importeResult.getString("pkcolumn_name")); //$NON-NLS-1$
					relationKeys.setFkTableName(fkTableName);
					relationKeys.setFkColumnName(importeResult.getString("fkcolumn_name")); //$NON-NLS-1$
					if (this._importedKeys.get(fkTableName) == null) {
						this._importedKeys.put(fkTableName, new ArrayList<RelationKeyBean>());
					}
					this._importedKeys.get(fkTableName).add(relationKeys);
				}
			}
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.100"), e); //$NON-NLS-1$
			return false;
		} finally {
			closeResultSet(exportResult);
			closeResultSet(importeResult);
			closeConnection(connection);
		}
		WebAppOSCache.getInstance().setImportedKeys(this._importedKeys);
		return true;
	}

	private static FullyQualifiedJavaType getExportedJavaType(String fkTableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("List<"); //$NON-NLS-1$
		sb.append(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_MODEL_PACKAGE));
		sb.append("."); //$NON-NLS-1$
		sb.append(getCamelCaseString(fkTableName, true));
		sb.append(">"); //$NON-NLS-1$
		return new FullyQualifiedJavaType(sb.toString());
	}

	private static FullyQualifiedJavaType getImportedJavaType(String fkTableName) {
		StringBuilder sb = new StringBuilder();
		sb.append(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_MODEL_PACKAGE));
		sb.append("."); //$NON-NLS-1$
		sb.append(getCamelCaseString(fkTableName, true));
		return new FullyQualifiedJavaType(sb.toString());
	}

	private static Field getJavaBeansField(FullyQualifiedJavaType type, String property) {
		Field field = new Field();
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setType(type);
		field.setName(property);
		return field;
	}

	private static Method getJavaBeansGetter(FullyQualifiedJavaType type, String property) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(type);
		method.setName(getGetterMethodName(property, type));

		StringBuilder sb = new StringBuilder();
		sb.append("return "); //$NON-NLS-1$
		sb.append(property);
		sb.append(";"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		return method;
	}

	private static Method getJavaBeansSetter(FullyQualifiedJavaType type, String property) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName(getSetterMethodName(property));
		method.addParameter(new Parameter(type, property));

		StringBuilder sb = new StringBuilder();
		sb.append("this."); //$NON-NLS-1$
		sb.append(property);
		sb.append(" = "); //$NON-NLS-1$
		sb.append(property);
		sb.append(";"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		return method;
	}

	private static void updateParameterTypeToGenericExample(XmlElement element) {
		for (Attribute orgAttribute : element.getAttributes()) {
			if ("parameterType".equalsIgnoreCase(orgAttribute.getName())) { //$NON-NLS-1$
				element.getAttributes().remove(orgAttribute);
				break;
			}
		}
		element.addAttribute(new Attribute("parameterType", GeneratorConstants.MYBATIS_EXAMPLE_CLASS)); //$NON-NLS-1$
	}

	private static void updateParameterTypeToMap(XmlElement element){
		for (Attribute orgAttribute : element.getAttributes()) {
			if ("parameterType".equalsIgnoreCase(orgAttribute.getName())) { //$NON-NLS-1$
				element.getAttributes().remove(orgAttribute);
				break;
			}
		}
		element.addAttribute(new Attribute("parameterType", "map")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static List<JoinElementInfo> createJoinElementInfo(IntrospectedTable introspectedTable,
			List<RelationKeyBean> allRelationKeys) {
		List<JoinElementInfo> result = new ArrayList<JoinElementInfo>();
		List<String> joinTableNames = new ArrayList<String>();
		String joinTableName = null;
		String joinLeftColumn = null;
		String joinRightColumn = null;

		if (allRelationKeys == null) return result;

		String tableName = introspectedTable.getTableConfiguration().getTableName();
		joinTableNames.add(tableName);
		for (int i = 0; i < allRelationKeys.size(); i++) {
			RelationKeyBean relationKeys = allRelationKeys.get(i);
			if (tableName.equals(relationKeys.getPkTableName())) {
				joinTableName = relationKeys.getFkTableName();
				joinLeftColumn = relationKeys.getFkTableName() + "." + relationKeys.getFkColumnName(); //$NON-NLS-1$
				joinRightColumn = relationKeys.getPkTableName() + "." + relationKeys.getPkColumnName(); //$NON-NLS-1$
			} else {
				if (joinTableNames.contains(relationKeys.getPkTableName())) {
					if (!joinTableNames.contains(relationKeys.getFkTableName())) {
						joinTableName = relationKeys.getFkTableName();
						joinLeftColumn = relationKeys.getFkTableName() + "." + relationKeys.getFkColumnName(); //$NON-NLS-1$
						joinRightColumn = relationKeys.getPkTableName() + "." + relationKeys.getPkColumnName(); //$NON-NLS-1$
					} else {
						continue;
					}
				} else {
					joinTableName = relationKeys.getPkTableName();
					joinLeftColumn = joinTableName + "." + relationKeys.getPkColumnName(); //$NON-NLS-1$
					joinRightColumn = relationKeys.getFkTableName() + "." + relationKeys.getFkColumnName(); //$NON-NLS-1$
				}
			}
			joinTableNames.add(joinTableName);

			JoinElementInfo joinElementInfo = new JoinElementInfo();
			// refid
			StringBuilder sb = new StringBuilder();
			sb.append(getMapperName(introspectedTable, joinTableName));
			sb.append("."); //$NON-NLS-1$
			sb.append("Base_Column_List"); //$NON-NLS-1$
			joinElementInfo.setIncludeRefId(sb.toString());
			// join table
			joinElementInfo.setJoinTable(joinTableName);
			// join column
			sb.setLength(0);
			sb.append(joinLeftColumn);
			sb.append(" = "); //$NON-NLS-1$
			sb.append(joinRightColumn);
			joinElementInfo.setJoinColumn(sb.toString());
			result.add(joinElementInfo);
		}
		return result;
	}

	private XmlElement getSelectAllByPrimaryKeyElement(IntrospectedTable introspectedTable, List<JoinElementInfo> importedJoinElementInfo) {
		StringBuilder sb = new StringBuilder();

		XmlElement answer = new XmlElement("select"); //$NON-NLS-1$
		answer.addAttribute(new Attribute("id", GeneratorConstants.MYBATIS_STATEMENT_ID_SELECT_ALL_BY_PRIMARY_KEY)); //$NON-NLS-1$
		answer.addAttribute(new Attribute("resultMap",GeneratorConstants.MYBATIS_RESULT_MAP_ID_FULL)); //$NON-NLS-1$
		String parameterType;
		if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			parameterType = introspectedTable.getPrimaryKeyType();
		} else {
			// PK fields are in the base class. If more than on PK
			// field, then they are coming in a map.
			if (introspectedTable.getPrimaryKeyColumns().size() > 1) {
				parameterType = "map"; //$NON-NLS-1$
			} else {
				parameterType = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType().toString();
			}
		}
		answer.addAttribute(new Attribute("parameterType", parameterType)); //$NON-NLS-1$

		answer.addElement(new TextElement("select ")); //$NON-NLS-1$

        XmlElement baseColumn = new XmlElement("include"); //$NON-NLS-1$
        baseColumn.addAttribute(new Attribute("refid", introspectedTable.getBaseColumnListId())); //$NON-NLS-1$
		answer.addElement(baseColumn);
		for (JoinElementInfo joinElementInfo : importedJoinElementInfo) {
			XmlElement element = new XmlElement("include"); //$NON-NLS-1$
			element.addAttribute(new Attribute("refid", joinElementInfo.getIncludeRefId())); //$NON-NLS-1$
			answer.addElement(new TextElement(",")); //$NON-NLS-1$
			answer.addElement(element);
		}

		String from = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
		sb.setLength(0);
		sb.append("from "); //$NON-NLS-1$
		sb.append(from);
		answer.addElement(new TextElement(sb.toString()));
		for (JoinElementInfo joinElementInfo : importedJoinElementInfo) {
			sb.setLength(0);
			sb.append("left join "); //$NON-NLS-1$
			sb.append(joinElementInfo.getJoinTable());
			sb.append(" on "); //$NON-NLS-1$
			sb.append(joinElementInfo.getJoinColumn());
			TextElement element = new TextElement(sb.toString());
			answer.addElement(element);
		}

		boolean and = false;
		for (IntrospectedColumn introspectedColumn : introspectedTable
				.getPrimaryKeyColumns()) {
			sb.setLength(0);
			if (and) {
				sb.append("  and "); //$NON-NLS-1$
			} else {
				sb.append("where "); //$NON-NLS-1$
				and = true;
			}
			sb.append(from);
			sb.append("."); //$NON-NLS-1$
			sb.append(MyBatis3FormattingUtilities
					.getAliasedEscapedColumnName(introspectedColumn));
			sb.append(" = "); //$NON-NLS-1$
			sb.append(MyBatis3FormattingUtilities
					.getParameterClause(introspectedColumn));
			answer.addElement(new TextElement(sb.toString()));
		}
		return answer;
	}

	private XmlElement getSelectAllByExample(List<JoinElementInfo> exportedJoinElementInfo, List<JoinElementInfo> importedJoinElementInfo) {
		StringBuilder sb = new StringBuilder();
		XmlElement answer = new XmlElement(this._selectByExampleElement);
		for (int i = answer.getAttributes().size() - 1; i >= 0; i--) {
			Attribute orgAttribute = answer.getAttributes().get(i);
			if ("id".equalsIgnoreCase(orgAttribute.getName())) { //$NON-NLS-1$
				answer.getAttributes().remove(orgAttribute);
			}
			if ("resultMap".equalsIgnoreCase(orgAttribute.getName())) { //$NON-NLS-1$
				answer.getAttributes().remove(orgAttribute);
			}
		}
		answer.addAttribute(new Attribute("id", GeneratorConstants.MYBATIS_STATEMENT_ID_SELECT_ALL_BY_EXAMPLE_KEY)); //$NON-NLS-1$
		answer.addAttribute(new Attribute("resultMap", GeneratorConstants.MYBATIS_RESULT_MAP_ID_FULL)); //$NON-NLS-1$
		for (int i = importedJoinElementInfo.size() - 1; i >= 0; i--) {
			sb.setLength(0);
			sb.append("left join "); //$NON-NLS-1$
			sb.append(importedJoinElementInfo.get(i).getJoinTable());
			sb.append(" on "); //$NON-NLS-1$
			sb.append(importedJoinElementInfo.get(i).getJoinColumn());
			TextElement element = new TextElement(sb.toString());
			answer.addElement(4, element);
		}
		for (int i = exportedJoinElementInfo.size() - 1; i >= 0; i--) {
			JoinElementInfo joinElementInfo = exportedJoinElementInfo.get(i);

			XmlElement eachElement = new XmlElement("foreach"); //$NON-NLS-1$
			eachElement.getAttributes().add(new Attribute("collection", "joinTables"));
			eachElement.getAttributes().add(new Attribute("item", "tableName"));

			XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
			ifElement.getAttributes().add(new Attribute("test", "tableName == '" + joinElementInfo.getJoinTable() + "'")); //$NON-NLS-1$

			sb.setLength(0);
			sb.append("left join "); //$NON-NLS-1$
			sb.append(joinElementInfo.getJoinTable());
			sb.append(" on "); //$NON-NLS-1$
			sb.append(joinElementInfo.getJoinColumn());
			TextElement joinElement = new TextElement(sb.toString());

			ifElement.addElement(joinElement);
			eachElement.addElement(ifElement);
			answer.addElement(4, eachElement);
		}

		for (int i = importedJoinElementInfo.size() - 1; i >= 0; i--) {
			XmlElement element = new XmlElement("include"); //$NON-NLS-1$
			element.addAttribute(new Attribute("refid", importedJoinElementInfo.get(i).getIncludeRefId())); //$NON-NLS-1$
			answer.addElement(3, element);
			answer.addElement(3, new TextElement(",")); //$NON-NLS-1$
		}
		for (int i = exportedJoinElementInfo.size() - 1; i >= 0; i--) {
			JoinElementInfo joinElementInfo = exportedJoinElementInfo.get(i);

			XmlElement eachElement = new XmlElement("foreach"); //$NON-NLS-1$
			eachElement.getAttributes().add(new Attribute("collection", "joinTables"));
			eachElement.getAttributes().add(new Attribute("item", "tableName"));

			XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
			ifElement.getAttributes().add(new Attribute("test", "tableName == '" + joinElementInfo.getJoinTable() + "'")); //$NON-NLS-1$

			XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
			includeElement.addAttribute(new Attribute("refid", joinElementInfo.getIncludeRefId())); //$NON-NLS-1$
			ifElement.addElement(new TextElement(",")); //$NON-NLS-1$
			ifElement.addElement(includeElement);

			eachElement.addElement(ifElement);
			answer.addElement(3, eachElement);
		}
		return answer;
	}

	private XmlElement getCountAllByExample(XmlElement selectAllElement, IntrospectedTable introspectedTable) {
		StringBuilder sb = new StringBuilder();
		XmlElement answer = new XmlElement(selectAllElement);
		for (int i = answer.getAttributes().size() - 1; i >= 0; i--) {
			Attribute orgAttribute = answer.getAttributes().get(i);
			if ("id".equalsIgnoreCase(orgAttribute.getName())) { //$NON-NLS-1$
				answer.getAttributes().remove(orgAttribute);
			}
			if ("resultMap".equalsIgnoreCase(orgAttribute.getName())) { //$NON-NLS-1$
				answer.getAttributes().remove(orgAttribute);
			}
		}
		answer.addAttribute(new Attribute("id", GeneratorConstants.MYBATIS_STATEMENT_ID_COUNT_ALL_BY_PRIMARY_KEY)); //$NON-NLS-1$
		answer.addAttribute(new Attribute("resultType", Integer.class.getName())); //$NON-NLS-1$

		boolean isFromStatement = true;
		for (int i = answer.getElements().size() - 1; i >= 0; i--) {
			Element orgElement = answer.getElements().get(i);
			if (orgElement instanceof TextElement) {
				if (",".equals(((TextElement) orgElement).getContent())) { //$NON-NLS-1$
					answer.getElements().remove(orgElement);
				}
				if (((TextElement) orgElement).getContent().startsWith("from")){
					isFromStatement = false;
				}
			} else {
				if (orgElement.getFormattedContent(0).startsWith("<if test=\"orderByClause")) {
					answer.getElements().remove(orgElement);
				}
				if (!isFromStatement && !orgElement.getFormattedContent(0).startsWith("<if test=\"_parameter != null\" >")) { //$NON-NLS-1$
					answer.getElements().remove(orgElement);
				}
			}
		}

		answer.addElement(1, new TextElement(" count(*) from (select distinct ")); //$NON-NLS-1$
		sb.setLength(0);
		String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
		if (!introspectedTable.getPrimaryKeyColumns().isEmpty()){
			for (IntrospectedColumn primaryKeyColumn : introspectedTable.getPrimaryKeyColumns()) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(tableName);
				sb.append('.');
				sb.append(primaryKeyColumn.getActualColumnName());
			}
		} else {
			sb.append("*"); //$NON-NLS-1$
		}
		answer.addElement(2, new TextElement(sb.toString()));

		answer.addElement(new TextElement(") as x")); //$NON-NLS-1$

		return answer;
	}

	private static String getMapperName(IntrospectedTable introspectedTable, String tableName){
		StringBuilder sb = new StringBuilder();
		sb.append(introspectedTable.getMyBatis3XmlMapperPackage());
		sb.append("."); //$NON-NLS-1$
		sb.append(getCamelCaseString(tableName, true));
		sb.append("Mapper"); //$NON-NLS-1$
		return sb.toString();
	}

	private static String getKeyProperty(){
		StringBuilder sb = new StringBuilder();
		sb.append(ControllerConstants.MYBATIS_MAP_KEY_RECORD);
		sb.append('.');
		sb.append(ControllerConstants.PK_COLUMN_NAME);
		return sb.toString();
	}
}
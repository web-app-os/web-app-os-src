package jp.co.headwaters.webappos.generator.mybatis.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.generator.GeneratorConstants;
import jp.co.headwaters.webappos.generator.utils.GeneratorUtils;
import jp.co.headwaters.webappos.generator.utils.PropertyUtils;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;

public class MapperConfigPlugin extends PluginAdapter {

	private JDBCConnectionConfiguration _jdbcConnectionConfiguration;
	private List<String> _mapperFiles;

	public MapperConfigPlugin() {
		this._mapperFiles = new ArrayList<String>();
	}

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		this._jdbcConnectionConfiguration = context.getJdbcConnectionConfiguration();
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean sqlMapGenerated(GeneratedXmlFile sqlMap,
			IntrospectedTable introspectedTable) {
		this._mapperFiles.add(getMapperName(sqlMap.getTargetPackage(), sqlMap.getFileName()));
		return true;
	}

	@Override
	public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles() {
		addExtraMapperFiles();

		addSystemMapperFiles();

		GeneratedXmlFile gxf = new GeneratedXmlFile(getDocument(),
				ControllerConstants.MYBATIS_CONFIG_FILE_NAME,
				GeneratorConstants.OUTPUT_PROPERTY_DIR,
				GeneratorUtils.getOutputPath(),
				false, this.context.getXmlFormatter());

		List<GeneratedXmlFile> answer = new ArrayList<GeneratedXmlFile>(1);
		answer.add(gxf);
		return answer;
	}

	private static String getMapperName(String targetPackage, String fileName) {
		StringBuilder sb = new StringBuilder();
		sb.append(targetPackage);
		sb.append('.');
		String temp = sb.toString();
		sb.setLength(0);
		sb.append(temp.replace('.', '/'));
		sb.append(fileName);
		return sb.toString();
	}

	private void addSystemMapperFiles() {
		this._mapperFiles.add(getMapperName(
				PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_MAPPER_PACKAGE),
				ControllerConstants.PROCEDURE_MAPPER_FILE_NAME));
	}

	private void addExtraMapperFiles() {
		File mapperDir = new File(GeneratorUtils.getInputMapperPath());
		if (mapperDir.listFiles() != null) {
			for (File file : mapperDir.listFiles()) {
				if (!file.isDirectory()){
					this._mapperFiles.add(getMapperName(
							PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_MAPPER_PACKAGE),
							file.getName()));
				}
			}
		}
	}

	private Document getDocument() {
		Document document = new Document(
				XmlConstants.MYBATIS3_MAPPER_CONFIG_PUBLIC_ID,
				XmlConstants.MYBATIS3_MAPPER_CONFIG_SYSTEM_ID);
		XmlElement root = new XmlElement("configuration"); //$NON-NLS-1$
		document.setRootElement(root);
		addSettingsElement(root);
		addEnvironmentsElement(root);
		addMappersElement(root);
		return document;
	}

	private static void addSettingsElement(XmlElement root) {
		XmlElement settings = new XmlElement("settings"); //$NON-NLS-1$
		root.addElement(settings);

		XmlElement setting = new XmlElement("setting"); //$NON-NLS-1$
		// TODO:取り急ぎ、最低限の設定のみを行う。
		// http://mybatis.github.io/mybatis-3/ja/configuration.html#settings
		setting.addAttribute(new Attribute("name", "mapUnderscoreToCamelCase")); //$NON-NLS-1$ //$NON-NLS-2$
		setting.addAttribute(new Attribute("value", "true")); //$NON-NLS-1$ //$NON-NLS-2$
		settings.addElement(setting);
	}

	private void addEnvironmentsElement(XmlElement root) {
		// TODO:環境毎に設定を変えられるようにもできるが取り急ぎ、最低限の設定のみ行う。
		// 開発orステージングor本番
		XmlElement environments = new XmlElement("environments"); //$NON-NLS-1$
		environments.addAttribute(new Attribute("default", "production")); //$NON-NLS-1$ //$NON-NLS-2$
		root.addElement(environments);

		XmlElement environment = new XmlElement("environment"); //$NON-NLS-1$
		environment.addAttribute(new Attribute("id", "production")); //$NON-NLS-1$ //$NON-NLS-2$
		environments.addElement(environment);

		XmlElement transactionManager = new XmlElement("transactionManager"); //$NON-NLS-1$
		transactionManager.addAttribute(new Attribute("type", "JDBC")); //$NON-NLS-1$ //$NON-NLS-2$
		environment.addElement(transactionManager);

		XmlElement dataSource = new XmlElement("dataSource"); //$NON-NLS-1$
		dataSource.addAttribute(new Attribute("type", "POOLED")); //$NON-NLS-1$ //$NON-NLS-2$
		environment.addElement(dataSource);

		XmlElement property;
		property = new XmlElement("property"); //$NON-NLS-1$
		property.addAttribute(new Attribute("name", "driver")); //$NON-NLS-1$ //$NON-NLS-2$
		property.addAttribute(new Attribute("value", this._jdbcConnectionConfiguration.getDriverClass())); //$NON-NLS-1$
		dataSource.addElement(property);
		property = new XmlElement("property"); //$NON-NLS-1$
		property.addAttribute(new Attribute("name", "url")); //$NON-NLS-1$ //$NON-NLS-2$
		property.addAttribute(new Attribute("value", this._jdbcConnectionConfiguration.getConnectionURL())); //$NON-NLS-1$
		dataSource.addElement(property);
		property = new XmlElement("property"); //$NON-NLS-1$
		property.addAttribute(new Attribute("name", "username")); //$NON-NLS-1$ //$NON-NLS-2$
		property.addAttribute(new Attribute("value", this._jdbcConnectionConfiguration.getUserId())); //$NON-NLS-1$
		dataSource.addElement(property);
		property = new XmlElement("property"); //$NON-NLS-1$
		property.addAttribute(new Attribute("name", "password")); //$NON-NLS-1$ //$NON-NLS-2$
		property.addAttribute(new Attribute("value", this._jdbcConnectionConfiguration.getPassword())); //$NON-NLS-1$
		dataSource.addElement(property);
	}

	private void addMappersElement(XmlElement root) {
		XmlElement mappers = new XmlElement("mappers"); //$NON-NLS-1$
		root.addElement(mappers);

		XmlElement mapper;
		for (String mapperFile : this._mapperFiles) {
			mapper = new XmlElement("mapper"); //$NON-NLS-1$
			mapper.addAttribute(new Attribute("resource", mapperFile)); //$NON-NLS-1$
			mappers.addElement(mapper);
		}
	}
}
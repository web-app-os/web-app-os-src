package jp.co.headwaters.webappos.generator.mybatis;

import static jp.co.headwaters.webappos.generator.utils.DataBaseUtils.*;
import static jp.co.headwaters.webappos.generator.utils.GeneratorUtils.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.generator.GeneratorConstants;
import jp.co.headwaters.webappos.generator.mybatis.bean.TableInfo;
import jp.co.headwaters.webappos.generator.utils.GeneratorUtils;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;
import jp.co.headwaters.webappos.generator.utils.PropertyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.internal.DefaultShellCallback;

public class ExtraMyBatisGenerator {

	private static final Log _logger = LogFactory.getLog(ExtraMyBatisGenerator.class);
	private Configuration _config = null;

	public boolean generate(String dbPassword) {
		List<String> warnings = new ArrayList<String>();
		try {
			this._config = createMyBatisConfiguration(dbPassword);
			MyBatisGenerator myBatisGenerator = new MyBatisGenerator(this._config, new DefaultShellCallback(false), warnings);
			myBatisGenerator.generate(null);
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.100"), e); //$NON-NLS-1$
			return false;
		}

		if (warnings.size() != 0) {
			for (String warning : warnings){
				_logger.error(warning);
			}
			return false;
		}

		return true;
	}

	public Configuration getConfiguration() {
		return this._config;
	}

	private Configuration createMyBatisConfiguration(String dbPassword)
			throws InvalidConfigurationException, SQLException {
		List<String> errors = new ArrayList<String>();
		Configuration config = new Configuration();

		String rootPackage = getRootPackage();
		String contextName = getContextName();
		PropertyUtils.putProperty(GeneratorConstants.PROPERTY_KEY_MODEL_PACKAGE, getModelPackage(rootPackage));
		PropertyUtils.putProperty(GeneratorConstants.PROPERTY_KEY_MAPPER_PACKAGE, getMapperPackage(rootPackage));

		// --------------------------------------------------------------------
		// <context> Element
		// --------------------------------------------------------------------
		Context context = new Context(ModelType.FLAT);
		config.addContext(context);
		context.setId(contextName);
		context.setTargetRuntime(GeneratorConstants.MYBATIS_TARGET_RUNTIME);
		context.addProperty("javaFileEncoding", GeneratorConstants.OUTPUT_JAVA_FILE_ENCODING); //$NON-NLS-1$

		// --------------------------------------------------------------------
		// <plugin> Element
		// --------------------------------------------------------------------
		for (String plugin : GeneratorConstants.MYBATIS_PLUGIN_CLASSES) {
			PluginConfiguration pluginConfiguration = new PluginConfiguration();
			pluginConfiguration.setConfigurationType(plugin);
			pluginConfiguration.validate(errors, contextName);
			context.addPluginConfiguration(pluginConfiguration);
		}

		// --------------------------------------------------------------------
		// <jdbcConnection> Element
		// --------------------------------------------------------------------
		JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
		jdbcConnectionConfiguration.setDriverClass("org.postgresql.Driver"); //$NON-NLS-1$
		jdbcConnectionConfiguration.setConnectionURL(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_CONNECTION_URL) + contextName);
		jdbcConnectionConfiguration.setUserId(contextName);
		jdbcConnectionConfiguration.setPassword(dbPassword);
		jdbcConnectionConfiguration.validate(errors);
		context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

		// --------------------------------------------------------------------
		// <javaModelGenerator> Element
		// --------------------------------------------------------------------
		JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
		javaModelGeneratorConfiguration.setTargetPackage(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_MODEL_PACKAGE));
		javaModelGeneratorConfiguration.setTargetProject(GeneratorUtils.getOutputSrcPath());
		javaModelGeneratorConfiguration.addProperty("rootClass", GeneratorConstants.MYBATIS_MODEL_ROOT_CLASS); //$NON-NLS-1$
		javaModelGeneratorConfiguration.validate(errors, contextName);
		context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

		// --------------------------------------------------------------------
		// <sqlMapGenerator> Element
		// --------------------------------------------------------------------
		SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
		sqlMapGeneratorConfiguration.setTargetPackage(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_MAPPER_PACKAGE));
		sqlMapGeneratorConfiguration.setTargetProject(GeneratorUtils.getOutputSrcPath());
		sqlMapGeneratorConfiguration.validate(errors, contextName);
		context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

		// --------------------------------------------------------------------
		// <table> Element
		// --------------------------------------------------------------------
		List<TableInfo> tables = getTableInfo(jdbcConnectionConfiguration);
		for (int i = 0; i < tables.size(); i++) {
			TableInfo tableInfo = tables.get(i);
			TableConfiguration tableConfiguration = new TableConfiguration(context);
			tableConfiguration.setTableName(tableInfo.getTableName());
			if (tableInfo.isView()) {
				tableConfiguration.setInsertStatementEnabled(false);
				tableConfiguration.setUpdateByPrimaryKeyStatementEnabled(false);
				tableConfiguration.setUpdateByExampleStatementEnabled(false);
				tableConfiguration.setDeleteByPrimaryKeyStatementEnabled(false);
				tableConfiguration.setDeleteByExampleStatementEnabled(false);
			}
			tableConfiguration.validate(errors, i);
			context.addTableConfiguration(tableConfiguration);
		}

		context.validate(errors);
		config.validate();
		if (errors.size() > 0) {
			throw new InvalidConfigurationException(errors);
		}

		return config;
	}

	private List<TableInfo> getTableInfo(JDBCConnectionConfiguration config) throws SQLException {
		List<TableInfo> tables = new ArrayList<TableInfo>();
		List<String> excludeTables = Arrays.asList(GeneratorConstants.MYBATIS_EXCLUDE_TABLE_NAMES);

		Connection connection = null;
		ResultSet result = null;
		try {
			connection = getConnection(config);
			DatabaseMetaData meta = connection.getMetaData();
			String types[] = { "TABLE", "VIEW" }; //$NON-NLS-1$ //$NON-NLS-2$
			result = meta.getTables(null, null, "%", types); //$NON-NLS-1$
			while (result.next()) {
				String tableName = result.getString("TABLE_NAME"); //$NON-NLS-1$
				if (excludeTables.contains(tableName.toLowerCase())) {
					continue;
				}
				tables.add(new TableInfo(tableName,
						result.getString("TABLE_TYPE").toUpperCase().equals("VIEW") ? true : false)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} finally {
			closeResultSet(result);
			closeConnection(connection);
		}
		return tables;
	}

	private static String getModelPackage(String rootPackage) {
		StringBuilder sb = new StringBuilder();
		sb.append(rootPackage);
		sb.append("."); //$NON-NLS-1$
		sb.append(ControllerConstants.MYBATIS_MODEL_PACKAGE);
		return sb.toString();
	}

	private static String getMapperPackage(String rootPackage) {
		StringBuilder sb = new StringBuilder();
		sb.append(rootPackage);
		sb.append("."); //$NON-NLS-1$
		sb.append(ControllerConstants.MYBATIS_MAPPER_PACKAGE);
		return sb.toString();
	}
}

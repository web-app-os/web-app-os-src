package jp.co.headwaters.webappos.generator;

import java.io.File;
import java.io.IOException;

import jp.co.headwaters.webappos.generator.db.SystemTableLoader;
import jp.co.headwaters.webappos.generator.mybatis.ExtraMyBatisGenerator;
import jp.co.headwaters.webappos.generator.utils.FileUtils;
import jp.co.headwaters.webappos.generator.utils.GeneratorUtils;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;
import jp.co.headwaters.webappos.generator.utils.PropertyUtils;
import jp.co.headwaters.webappos.generator.web.JspGenerator;
import jp.co.headwaters.webappos.generator.web.PropertyGenerator;
import jp.co.headwaters.webappos.generator.web.StrutsXmlGenerator;
import jp.co.headwaters.webappos.generator.web.WebAppOSDatGenerator;
import jp.co.headwaters.webappos.generator.web.WebXmlGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.generator.config.JDBCConnectionConfiguration;

public class Main {
	private static final Log _logger = LogFactory.getLog(Main.class);

	private static String _contextName = null;
	private static String _dbPassword = null;
	private static boolean _isFullBuild = true;

	public static void main(String[] args) {
		_logger.info("WEB APP OS generate start"); //$NON-NLS-1$
		try {
			// validate
			if (!validate(args)) {
				exit(-1);
			}

			// load generator.properties
			try {
				PropertyUtils.load();
			} catch (IOException e) {
				_logger.error(MessageUtils.getString("err.001"), e); //$NON-NLS-1$
				exit(-2);
			}

			if (_isFullBuild) {
				// remake output directory
				clearOutputDirectory();

				// generate webappos.properties
				if (!new PropertyGenerator().generate()) {
					exit(-3);
				}

				// generate MyBatis mapper
				ExtraMyBatisGenerator myBatisGenerator = new ExtraMyBatisGenerator();
				if (!myBatisGenerator.generate(_dbPassword)) {
					exit(-4);
				}
				JDBCConnectionConfiguration jDBCConnectionConfiguration =
						myBatisGenerator.getConfiguration().getContext(GeneratorUtils.getContextName()).getJdbcConnectionConfiguration();

				// load system tables
				if (!new SystemTableLoader().load(jDBCConnectionConfiguration)) {
					exit(-5);
				}
			}

			// generate jsp
			if (!new JspGenerator(_isFullBuild).generate()) {
				exit(-6);
			}

			// generate struts.xml
			if (!StrutsXmlGenerator.generate()) {
				exit(-7);
			}

			// generate web.xml
			if (!WebXmlGenerator.generate()) {
				exit(-8);
			}

			// generate apache config
			// TODO:β版では実装しない

			// generate webappos.dat
			if (!WebAppOSDatGenerator.generate()) {
				exit(-9);
			}

			exit(0);
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.900"), e); //$NON-NLS-1$
			exit(-99);
		}
	}

	public static String getContextName() {
		return _contextName;
	}

	private static void exit(int status) {
		_logger.info("WEB APP OS generate end status = " + status); //$NON-NLS-1$
		System.exit(status);
	}

	private static boolean validate(String[] args) {
		if (args == null || args.length < 2) {
			return false;
		}
		_contextName = args[0];
		_dbPassword = args[1];
		if (args.length >= 3 && "diff".equalsIgnoreCase(args[2])) {
			_isFullBuild = false;
		}
		return true;
	}

	private static void clearOutputDirectory() {
		FileUtils.deleteFile(new File(GeneratorUtils.getOutputPath()));
		new File(GeneratorUtils.getOutputJspPath()).mkdirs();
		new File(GeneratorUtils.getOutputWebInfPath()).mkdir();
		new File(GeneratorUtils.getOutputSrcPath()).mkdir();
		new File(GeneratorUtils.getOutputPropertyPath()).mkdir();
		new File(GeneratorUtils.getOutputDatPath()).mkdir();
	}
}

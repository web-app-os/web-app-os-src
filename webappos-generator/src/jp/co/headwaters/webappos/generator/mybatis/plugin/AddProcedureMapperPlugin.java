 package jp.co.headwaters.webappos.generator.mybatis.plugin;

import static jp.co.headwaters.webappos.generator.utils.DataBaseUtils.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.cache.bean.ProcedureInfoBean;
import jp.co.headwaters.webappos.controller.enumation.DataTypeEnum;
import jp.co.headwaters.webappos.controller.enumation.PgLanguageEnum;
import jp.co.headwaters.webappos.generator.GeneratorConstants;
import jp.co.headwaters.webappos.generator.utils.GeneratorUtils;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;
import jp.co.headwaters.webappos.generator.utils.PropertyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.config.Context;

public class AddProcedureMapperPlugin extends PluginAdapter {

	private static final Log _logger = LogFactory.getLog(AddProcedureMapperPlugin.class);
	private static final String SELECT_PROC_INFO =
												"select" + //$NON-NLS-1$
												"  proname," + //$NON-NLS-1$
												"  lanname," + //$NON-NLS-1$
												"  prorettype::regtype as prorettype," + //$NON-NLS-1$
												"  pronargs," + //$NON-NLS-1$
												"  oidvectortypes(pg_proc.proargtypes) as proargtypes," + //$NON-NLS-1$
												"  proargnames " + //$NON-NLS-1$
												" from" + //$NON-NLS-1$
												"  pg_proc " + //$NON-NLS-1$
												" inner join pg_user on pg_user.usesysid = pg_proc.proowner " + //$NON-NLS-1$
												" inner join pg_language on pg_language.oid = pg_proc.prolang " + //$NON-NLS-1$
												" where " + //$NON-NLS-1$
												"  pg_user.usename = current_user " + //$NON-NLS-1$
												"order by pg_proc.proname"; //$NON-NLS-1$
	private boolean _isError;

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		this._isError = !getProcedureInfo(context);
	}

	@Override
	public boolean validate(List<String> warnings) {
		boolean valid = true;

		if (this._isError) {
			valid = false;
		}
		return valid;
	}

	@Override
	public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles() {
		GeneratedXmlFile gxf = new GeneratedXmlFile(getDocument(),
				ControllerConstants.PROCEDURE_MAPPER_FILE_NAME,
				PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_MAPPER_PACKAGE),
				GeneratorUtils.getOutputSrcPath(),
				false, this.context.getXmlFormatter());

		List<GeneratedXmlFile> answer = new ArrayList<GeneratedXmlFile>(1);
		answer.add(gxf);
		return answer;
	}

	private boolean getProcedureInfo(Context context) {
		Map<String, ProcedureInfoBean> procedureMap = new LinkedHashMap<String, ProcedureInfoBean>();
		WebAppOSCache.getInstance().setProcedureMap(procedureMap);

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		ProcedureInfoBean procedureInfo = null;
		String proname = null;
		short argnum = 0;
		Map<String, DataTypeEnum> args = null;
		List<String> outNames;
		try {
			connection = getConnection(context.getJdbcConnectionConfiguration());
			stmt = connection.createStatement();
			rs = stmt.executeQuery(SELECT_PROC_INFO);

			while (rs.next()) {
				if ("trigger".equalsIgnoreCase(rs.getString("prorettype").toLowerCase())) { //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}

				args = new LinkedHashMap<String, DataTypeEnum>();
				outNames = new ArrayList<>();

				procedureInfo = new ProcedureInfoBean();
				proname = rs.getString("proname"); //$NON-NLS-1$
				procedureInfo.setName(proname.toLowerCase());
				procedureInfo.setLang(PgLanguageEnum.getLanguage(rs.getString("lanname").toLowerCase())); //$NON-NLS-1$
				procedureInfo.setRetType(DataTypeEnum.getDataType(rs.getString("prorettype").toLowerCase())); //$NON-NLS-1$
				argnum = rs.getShort("pronargs"); //$NON-NLS-1$
				procedureInfo.setArgNum(argnum);
				procedureInfo.setArgs(args);
				procedureInfo.setOutNames(outNames);
				procedureMap.put(proname, procedureInfo);

				String[] proargtypes = rs.getString("proargtypes").split(","); //$NON-NLS-1$ //$NON-NLS-2$
				if (rs.getArray("proargnames") != null) { //$NON-NLS-1$
					String[] proargnames = (String[]) rs.getArray("proargnames").getArray(); //$NON-NLS-1$
					for (int i = 0; i < proargnames.length; i++) {
						if (i < argnum) {
							// in
							args.put(proargnames[i].toLowerCase(), DataTypeEnum.getDataType(proargtypes[i].trim()));
						} else {
							// out
							outNames.add(proargnames[i].toLowerCase());
						}
					}
				}
			}
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.102"), e); //$NON-NLS-1$
			return false;
		} finally {
			closeResultSet(rs);
			closeStatement(stmt);
			closeConnection(connection);
		}
		return true;
	}

	private Document getDocument() {
		Document document = new Document(
				XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID,
				XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);
		XmlElement root = new XmlElement("mapper"); //$NON-NLS-1$
		root.addAttribute(new Attribute("namespace", getNamespace())); //$NON-NLS-1$
		document.setRootElement(root);

		Map<String, ProcedureInfoBean> procMap = WebAppOSCache.getInstance().getProcedureMap();
		for (ProcedureInfoBean procInfo : procMap.values()) {
			XmlElement select = new XmlElement("select"); //$NON-NLS-1$
			select.addAttribute(new Attribute("id", procInfo.getName())); //$NON-NLS-1$
			if (procInfo.getArgNum() > 0) {
				select.addAttribute(new Attribute("parameterType", "map")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!procInfo.getRetType().equals(DataTypeEnum.DATA_TYPE_VOID)) {
				if (procInfo.getRetType().equals(DataTypeEnum.DATA_TYPE_RECORD)) {
					select.addAttribute(new Attribute("resultType", "map")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					select.addAttribute(new Attribute("resultType", procInfo.getRetType().getClazz())); //$NON-NLS-1$
				}
			} else {
				select.addAttribute(new Attribute("resultType", "map")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			select.addElement(new TextElement(getStatement(procInfo)));
			root.addElement(select);
		}
		return document;
	}

	private String getNamespace() {
		StringBuilder sb = new StringBuilder();
		sb.append(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_MAPPER_PACKAGE));
		sb.append('.');
		sb.append(ControllerConstants.PROCEDURE_MAPPER_NAME);
		return sb.toString();
	}

	private String getStatement(ProcedureInfoBean procInfo) {
		StringBuilder sb = new StringBuilder();
		StringBuilder param = new StringBuilder();
		sb.append("select * from "); //$NON-NLS-1$
		sb.append(procInfo.getName());
		sb.append("("); //$NON-NLS-1$
		if (procInfo.getArgNum() > 0) {
			for (Entry<String, DataTypeEnum> arg : procInfo.getArgs().entrySet()){
				if (param.length() > 0){
					param.append(","); //$NON-NLS-1$
				}
				param.append("#{"); //$NON-NLS-1$
				param.append(arg.getKey());
				param.append(","); //$NON-NLS-1$
				param.append("jdbcType="); //$NON-NLS-1$
				param.append(arg.getValue().getJdbcType());
				param.append("}"); //$NON-NLS-1$
			}
		}
		sb.append(param);
		sb.append(")"); //$NON-NLS-1$
		return sb.toString();
	}
}
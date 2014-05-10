package jp.co.headwaters.webappos.controller.action.sysapi;

import static jp.co.headwaters.webappos.controller.utils.ControllerUtils.*;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.action.AbstractAction;
import jp.co.headwaters.webappos.controller.cache.SchemaColumnCache;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils;
import jp.co.headwaters.webappos.controller.utils.DaoUtils;
import jp.co.headwaters.webappos.controller.utils.MessageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

@SuppressWarnings("serial")
public class ValidationAction extends AbstractAction {

	private static final Log _logger = LogFactory.getLog(ValidationAction.class);

	public String unique() throws IOException {
		try {
			String table = this._request.getParameter(ControllerConstants.API_PARAM_TABLE_NAME);
			String column = this._request.getParameter(ControllerConstants.API_PARAM_COLUMN_NAME);
			String value = this._request.getParameter(ControllerConstants.API_PARAM_VALUE);
			if (table == null || column == null || value == null) {
				_logger.warn(MessageUtils.getString("warn.104", getLogMessage(table, column, value))); //$NON-NLS-1$
				this._response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}

			SchemaColumnBean schemaColumn = SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(table, column));
			Object daoParamValue = ConvertDateTypeUtils.convertStringToDbType(value, schemaColumn);

			SqlSession sqlSession = DaoUtils.openSqlSession();
			if (DaoUtils.isConflict(sqlSession, schemaColumn, daoParamValue)) {
				this._response.sendError(HttpServletResponse.SC_CONFLICT);
			}
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			this._response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return null;
	}

}
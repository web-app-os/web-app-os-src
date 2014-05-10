package jp.co.headwaters.webappos.controller.fuction;

import jp.co.headwaters.webappos.controller.enumation.FunctionEnum;

public abstract class FunctionFactory {

	public static AbstractFunction create(String type) {
		AbstractFunction function = null;
		if (FunctionEnum.FUNCTION_SENDMAIL.getFunctionName().equalsIgnoreCase(type)) {
			function = new SendMailFunction();
		} else if (FunctionEnum.FUNCTION_PROCEDURE.getFunctionName().equalsIgnoreCase(type)) {
			function = new ProcedureFunction();
		}else{
			// default
			function = new CrudFunction();
		}
		return function;
	}
}

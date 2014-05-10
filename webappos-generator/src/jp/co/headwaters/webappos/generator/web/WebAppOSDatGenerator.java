package jp.co.headwaters.webappos.generator.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.generator.utils.GeneratorUtils;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebAppOSDatGenerator {

	private static final Log _logger = LogFactory.getLog(WebAppOSDatGenerator.class);

	public static boolean generate() {
		try {
			try (
					FileOutputStream fos = new FileOutputStream(getDatFile());
					ObjectOutputStream oos = new ObjectOutputStream(fos)) {
				WebAppOSCache cache = WebAppOSCache.getInstance();
				oos.writeObject(cache);
			}
		} catch (IOException e) {
			_logger.error(MessageUtils.getString("err.500"), e); //$NON-NLS-1$
			return false;
		}

		return true;
	}

	public static File getDatFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(GeneratorUtils.getOutputDatPath());
		sb.append(ControllerConstants.WEBAPPS_DAT_FILE_NAME);
		return new File(sb.toString());
	}
}

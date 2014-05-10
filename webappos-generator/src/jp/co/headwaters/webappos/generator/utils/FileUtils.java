package jp.co.headwaters.webappos.generator.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.generator.GeneratorConstants;

import org.apache.commons.lang3.StringUtils;

public class FileUtils {

	public static void writeFile(File file, String content, String fileEncoding) throws IOException {
		String charsetName = fileEncoding;
		if (charsetName == null) {
			charsetName = (String) null;
		}
		try (
				FileOutputStream fos = new FileOutputStream(file, false);
				OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
				BufferedWriter bw = new BufferedWriter(osw)) {
			bw.write(content);
		}
	}

	public static void copyFile(File file, String newFilePath) throws IOException {
		try (
				FileInputStream fis = new FileInputStream(file);
				FileChannel ifc = fis.getChannel()) {
			File outFile = new File(newFilePath);
			try (
					FileOutputStream fos = new FileOutputStream(outFile);
					FileChannel ofc = fos.getChannel()) {
				ifc.transferTo(0, ifc.size(), ofc);
			}
		}
	}

	public static void deleteFile(File file) {
		if (file.exists() == false) {
			return;
		}

		if (file.isFile()) {
			file.delete();
		}

		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteFile(files[i]);
			}
			file.delete();
		}
	}

	public static String getFileExtension(String filename) {
		if (StringUtils.isEmpty(filename)) {
			return ""; //$NON-NLS-1$
		}
		int lastDotPos = filename.lastIndexOf('.');
		if (lastDotPos == -1) {
			return filename;
		} else if (lastDotPos == 0) {
			return filename;
		} else {
			return filename.substring(lastDotPos + 1).toLowerCase();
		}
	}

	public static String removeFileExtension(String filename) {
		if (StringUtils.isEmpty(filename)) {
			return filename;
		}

		int lastDotPos = filename.lastIndexOf('.');
		if (lastDotPos == -1) {
			return filename;
		} else if (lastDotPos == 0) {
			return filename;
		} else {
			return filename.substring(0, lastDotPos);
		}
	}

	public static String convertExtensionHtmlToJsp(String target) {
		String result = target;
		if (StringUtils.isEmpty(target)) {
			return result;
		}

		for (String extension : GeneratorConstants.INPUT_FILE_EXTENSION) {
			result = result.replaceAll(extension + "$", ControllerConstants.JSP_EXTENSION); //$NON-NLS-1$
		}

		return result;
	}

	public static FilenameFilter getTargetFileFilter() {
		return getFileExtensionFilter(GeneratorConstants.INPUT_FILE_EXTENSION);
	}

	public static FilenameFilter getFileExtensionFilter(String[] extensions) {
		final String[] _extensions = extensions;
		return new FilenameFilter() {
			public boolean accept(File file, String name) {
				boolean result = false;
				for (String extension : _extensions) {
					if (name.endsWith(extension)) {
						result = true;
						break;
					}
				}
				return result;
			}
		};
	}
}
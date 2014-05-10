package jp.co.headwaters.webappos.controller.action.sysapi;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.action.AbstractAction;
import jp.co.headwaters.webappos.controller.cache.ImageDataCache;
import jp.co.headwaters.webappos.controller.cache.bean.ImageCacheBean;
import jp.co.headwaters.webappos.controller.enumation.StretchTypeEnum;
import jp.co.headwaters.webappos.controller.security.Cipher;
import jp.co.headwaters.webappos.controller.security.CipherFactory;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.controller.utils.MessageUtils;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionContext;

@SuppressWarnings("serial")
public class ImageAction extends AbstractAction {

	private static final Log _logger = LogFactory.getLog(ImageAction.class);

	public String execute() {
		try {
			if (this.path == null) {
				_logger.warn(MessageUtils.getString("warn.104", getLogMessage())); //$NON-NLS-1$
				outputDefaultImage();
				return null;
			}
			this.stretchType = StretchTypeEnum.getStretchTypeByKey(this.type);

			Cipher cipher = CipherFactory.create("blowfish"); //$NON-NLS-1$
			this.path = cipher.decrypt(this.path);

			this.path = getUploadFilePath();

			if (!isTargetExtension()) {
				_logger.warn(MessageUtils.getString("warn.107", getLogMessage())); //$NON-NLS-1$
				outputDefaultImage();
				return null;
			}

			// TODO:権限チェック

			byte[] imageData = null;
			String cacheKey = this.path + "," + this.dw + "," + this.dh; //$NON-NLS-1$ //$NON-NLS-2$
			ImageDataCache cache = ImageDataCache.getInstance();
			ImageCacheBean imageCache = cache.getImageData(cacheKey);
			if (imageCache != null) {
				imageData = imageCache.getImageData();
			} else {
				imageData = getImageBytes(this.path);
			}

			if (imageData != null){
				outputImage(imageData, this.path);
				ImageCacheBean tempImageCacheBean = new ImageCacheBean(cacheKey, imageData, getExpireDateTime());
				cache.addImageData(tempImageCacheBean);
			} else {
				_logger.warn(MessageUtils.getString("warn.106", getLogMessage())); //$NON-NLS-1$
				outputDefaultImage();
			}
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			try {
				outputDefaultImage();
			} catch (IOException e1) {
				_logger.error(e1.getMessage(), e1);
			}
		}
		return null;
	}

	private String getUploadFilePath(){
		StringBuilder sb = new StringBuilder();
		sb.append(getText(ControllerConstants.PROPERTY_KEY_FILE_BASE_DIR));
		sb.append(this.path);
		return sb.toString();
	}

	private boolean isTargetExtension() {
		String target = ControllerUtils.getFileExtension(this.path);
		for (String extension : ControllerConstants.IMAGE_FILE_EXTENSION) {
			if (extension.equalsIgnoreCase(target)) {
				return true;
			}
		}
		return false;
	}

	private byte[] getImageBytes(String path) throws IOException {
		byte[] result = null;
		ByteArrayOutputStream bos = null;
		BufferedOutputStream os = null;
		try {
			BufferedImage image = null;
			bos = new ByteArrayOutputStream();
			os = new BufferedOutputStream(bos);

			File file = new File(path);
			image = ImageIO.read(file);
			image = resizeImage(image);

			ImageIO.write(image, ControllerUtils.getFileExtension(path), os);
			os.flush();

			result = bos.toByteArray();
		} finally {
			if (os != null) {
				os.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
		return result;
	}

	private BufferedImage resizeImage(BufferedImage image) {
		BufferedImage resizedImage = image;
		if (this.dw != null && this.dh != null) {
			int orgWidth = image.getWidth();
			int orgHeight = image.getHeight();
			int width = this.dw;
			int height = this.dh;
			int x = 0;
			int y = 0;
			if (StretchTypeEnum.FILL.equals(this.stretchType)) {
				resizedImage = new BufferedImage(width, height, image.getType());
				resizedImage.getGraphics().drawImage(image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING), 0, 0, width, height, null);
			} else {
				double reduceRateHeight = 1;
				double reduceRateWidth = 1;
				if (this.dw != null) {
					reduceRateWidth = ((double) width / orgWidth);
				}
				if (this.dh != null) {
					reduceRateHeight = ((double) height / orgHeight);
				}

				if (StretchTypeEnum.UNIFORM.equals(this.stretchType)) {
					// 小さい方の縮小率でリサイズ
					if (reduceRateWidth < reduceRateHeight) {
						width = (int) (orgWidth * reduceRateWidth);
						height = (int) (orgHeight * reduceRateWidth);
					} else {
						width = (int) (orgWidth * reduceRateHeight);
						height = (int) (orgHeight * reduceRateHeight);
					}
					resizedImage = new BufferedImage(width, height, image.getType());
					resizedImage.getGraphics().drawImage(image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING), 0, 0, width, height, null);
				} else {
					int tempWidth = 0;
					int tempHeight = 0;
					// 大きい方の縮小率でリサイズ
					if (reduceRateWidth < reduceRateHeight) {
						tempWidth = (int) Math.ceil(orgWidth * reduceRateHeight);
						tempHeight = (int) Math.ceil(orgHeight * reduceRateHeight);
					} else {
						tempWidth = (int) Math.ceil(orgWidth * reduceRateWidth);
						tempHeight = (int) Math.ceil(orgHeight * reduceRateWidth);
					}
					resizedImage = new BufferedImage(tempWidth, tempHeight, image.getType());
					resizedImage.getGraphics().drawImage(image.getScaledInstance(tempWidth, tempHeight, Image.SCALE_AREA_AVERAGING), 0, 0, tempWidth, tempHeight, null);

					// トリミング
					if (reduceRateWidth < reduceRateHeight) {
						x = ((int) (orgWidth * reduceRateHeight) - width) / 2;
					} else {
						y = ((int) (orgHeight * reduceRateWidth) - height) / 2;
					}
					resizedImage = resizedImage.getSubimage(x, y, width, height);
				}
			}
		}
		return resizedImage;
	}

	private long getExpireDateTime() {
		long expire = 1000 * 60 * 60;
		String setting = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_IMAGE_CACHE_EXPIRE);
		if (!StringUtils.isEmpty(setting)){
			expire = Long.parseLong(setting);
			expire *= 1000;
		}
		long expireDate = System.currentTimeMillis() + expire;
		return expireDate;
	}

	private void outputImage(byte[] image, String path) throws IOException {
		if (image == null) return;

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		byteOut.write(image, 0, image.length);
		this._response.setContentType(getContentType(path));
		this._response.setContentLength(byteOut.size());
		OutputStream out = null;
		try {
			out = this._response.getOutputStream();
			out.write(byteOut.toByteArray());
			out.flush();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private void outputDefaultImage() throws IOException {
		String defaultImagePath = getNoImagePath();
		if (defaultImagePath != null) {
			outputImage(getImageBytes(defaultImagePath), defaultImagePath);
		}
	}

	private String getNoImagePath() {
		ActionContext ac = ActionContext.getContext();
        ServletContext sc = (ServletContext)ac.get(StrutsStatics.SERVLET_CONTEXT);
        return sc.getRealPath(getText(ControllerConstants.PROPERTY_KEY_NO_IMAGE_PATH));
	}

	private String getContentType(String path) {
		String target = ControllerUtils.getFileExtension(path);
		if ("jpg".equalsIgnoreCase(target) || "jpeg".equalsIgnoreCase(target)){
			return "image/jpeg";
		} else if ("png".equalsIgnoreCase(target)) {
			return "image/png";
		} else if ("gif".equalsIgnoreCase(target)) {
			return "image/gif";
		} else if ("bmp".equalsIgnoreCase(target)) {
			return "image/x-bmp";
		}
		return null;
	}

	private String getLogMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("path="); //$NON-NLS-1$
		sb.append(this.path);
		sb.append(","); //$NON-NLS-1$
		sb.append("dw="); //$NON-NLS-1$
		sb.append(this.dw);
		sb.append(","); //$NON-NLS-1$
		sb.append("dh="); //$NON-NLS-1$
		sb.append(this.dh);
		return sb.toString();
	}

	// ---------- request parameter ------------------------------
	private String path;
	private Integer dw;
	private Integer dh;
	private String type;
	private StretchTypeEnum stretchType;

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getDw() {
		return this.dw;
	}

	public void setDw(Integer dw) {
		this.dw = dw;
	}

	public Integer getDh() {
		return this.dh;
	}

	public void setDh(Integer dh) {
		this.dh = dh;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
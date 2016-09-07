package nd.esp.service.lifecycle.support.cs;

import java.math.BigDecimal;

import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.nd.sdp.cs.sdk.Dentry;
import com.nd.sdp.cs.sdk.ExtendUploadData;
import com.nd.sdp.cs.sdk.UploadProgressCallBack;

/**
 * CS服务类
 * 
 * @author xiezy
 * @date 2016年9月5日
 */
public class ContentServiceHelper {
	private static final Logger LOG = LoggerFactory.getLogger(ContentServiceHelper.class);
	
	/**
	 * 文件上传 -- byte上传
	 * @author xiezy
	 * @date 2016年9月7日
	 * @param bytes			文件的byte[] 
	 * @param path			父目录项路径，支持自动创建目录
	 * @param fileName		文件名,传的时候为【覆盖上传】
	 * @param filePath		文件完整路径
	 * @param serviceName	申请的内容服务名称
	 * @param session		会话标签
	 * @return
	 */
	public static Dentry uploadByByte(byte[] bytes, String path, 
			String fileName, String filePath, String serviceName, String session) {
		
		try {
			// 参数设置
			Dentry request = new Dentry();
			ExtendUploadData requestData = new ExtendUploadData();
			
			//如果有传此项，将会覆盖旧文件数据 dentryId和filePath二选一 覆盖上传时不用传parentId，path，name
			// request.setDentryId("96340084-775d-4740-ae2f-b2dea289618b");
			//如果有传此项，将会覆盖旧文件数据，dentryId和filePath二选一 覆盖上传时不用传parentId，path，name
			if(StringUtils.hasText(filePath)){
				requestData.setFilePath(filePath);
			}
			//父目录项id（UUID），（path 和 parent_id 二选一）
			// request.setParentId("96340084-775d-4740-ae2f-b2dea289618b");
			
			requestData.setExpireDays(0); // 过期天数，0-永不过期，可选，默认：0
			request.setPath(path); // 父目录项路径，支持自动创建目录，（path 和 parent_id 二选一）
			request.setName(fileName); // 文件名，包括后缀名 必选
//			request.setOtherName("otherName"); // 备注名 可选
//			request.setInfo(info); // 自定义元数据，如：{tags: [xx,xx], title: xx, note: xx, content: xx},可选
//			request.getInode().setMd5("96340084-775d-4740-ae2f-b2dea289618b"); // 整个文件的MD5码，文件秒传时必选
//			request.getInode().setMeta(meta); // 图片，音频，视频等元数据： {width: 1024, height: 768}，可选
//			request.getInode().setMime("image/jpeg"); // 文件mime 可选
			request.setScope(0); // 0-私密，1-公开，默认：0，可选
//			byte[] bytes = getFileBytes(); // 文件的byte[]数据
			// 调用
			Dentry result = request.upload(serviceName, bytes, requestData,
					session, new UploadProgressCallBack() {
						@Override
						public void onUploadProgressCallBack(Long bytesWritten,
								Long totalBytesWritten,
								Long totalBytesExpectedToWrite) {
							BigDecimal b = new BigDecimal(Double
									.valueOf(totalBytesExpectedToWrite)
									/ Double.valueOf(totalBytesWritten) * 100D);
							double process = b
									.setScale(2, BigDecimal.ROUND_HALF_UP)
									.doubleValue();
							
							LOG.info("上传了" + process + "%");
						}
					});
			
			if(result == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	            		LifeCircleErrorMessageMapper.CSSdkFail);
			}
			
			return result;
		} catch (Exception e) {
			
			LOG.error(LifeCircleErrorMessageMapper.CSSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
            		LifeCircleErrorMessageMapper.CSSdkFail.getCode(),e.getLocalizedMessage());
		}
	}
}

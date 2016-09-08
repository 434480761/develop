package nd.esp.service.lifecycle.support.cs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.CollectionUtils;
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
			
			requestData.setExpireDays(CSConstant.CS_DEFAULT_EXPIRE_DAYS); // 过期天数，0-永不过期，可选，默认：0
			request.setPath(path); // 父目录项路径，支持自动创建目录，（path 和 parent_id 二选一）
			request.setName(fileName); // 文件名，包括后缀名 必选
//			request.setOtherName("otherName"); // 备注名 可选
//			request.setInfo(info); // 自定义元数据，如：{tags: [xx,xx], title: xx, note: xx, content: xx},可选
//			request.getInode().setMd5("96340084-775d-4740-ae2f-b2dea289618b"); // 整个文件的MD5码，文件秒传时必选
//			request.getInode().setMeta(meta); // 图片，音频，视频等元数据： {width: 1024, height: 768}，可选
//			request.getInode().setMime("image/jpeg"); // 文件mime 可选
			request.setScope(CSConstant.CS_SCOPE_PUBLIC); // 0-私密，1-公开，默认：0，可选
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
	            		LifeCircleErrorMessageMapper.CSSdkFail.getCode(),
	            		"CS SDK -- 文件上传出错！");
			}
			
			return result;
		} catch (Exception e) {
			
			LOG.error(LifeCircleErrorMessageMapper.CSSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
            		LifeCircleErrorMessageMapper.CSSdkFail.getCode(),e.getLocalizedMessage());
		}
	}
	
	/**
	 * 添加目录项
	 * @author xiezy
	 * @date 2016年9月8日
	 * @param path			父目录项路径，支持自动创建目录
	 * @param dirname		目录项名（文件一般包括扩展名，支持重命名）
	 * @param serviceName	申请的内容服务名称
	 * @param session		会话标签
	 * @return
	 */
	public static Dentry createDir(String path, String dirname, String serviceName, String session){
		try {
			//参数设置
			Dentry request = new Dentry();
			//request.setParentId("96340084-775d-4740-ae2f-b2dea289618b");  //父目录项id（UUID），（path 和 parent_id 二选一）
			request.setPath(path);                     //父目录项路径，支持自动创建目录，（path 和 parent_id 二选一）
			request.setName(dirname);                        //目录项名（文件一般包括扩展名，支持重命名），必选
//			request.setOtherName("otherName");                  //备注名  可选
//			request.setInfo(info);                      //自定义元数据，如：{tags: [xx,xx], title: xx, note: xx, content: xx}  可选
			request.setScope(CSConstant.CS_SCOPE_PUBLIC);                        //0-私密，1-公开，默认：0，可选
			//调用
			Dentry result = request.create(serviceName, CSConstant.CS_DEFAULT_CAPACITY, CSConstant.CS_DEFAULT_EXPIRE_DAYS, session);
			
			if(result == null){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	            		LifeCircleErrorMessageMapper.CSSdkFail.getCode(),
	            		"CS SDK -- 添加目录项出错！");
			}
			
			return result;
		} catch (Exception e) {
			
			LOG.error(LifeCircleErrorMessageMapper.CSSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
            		LifeCircleErrorMessageMapper.CSSdkFail.getCode(),e.getLocalizedMessage());
		}
	}
	
	/**
	 * 通过path获取CS目录项
	 * 
	 * 备注：获取目录项 实际是使用批量获取目录项的方式实现，目的是为了保证和原先http api请求方式一样
	 * @author xiezy
	 * @date 2016年9月8日
	 * @param path			目录项Path
	 * @param serviceName	申请的内容服务名称
	 * @param session		会话标签
	 * @return
	 */
	public static Dentry getDentry(String path, String serviceName, String session){
		try {
			List<String> paths = new ArrayList<String>();
			paths.add(path);
			
			//选择存在目录项不存在时是否抛出异常 默认true-抛异常 false-不抛异常，只返回已查询到的记录
			boolean exception = true;
			List<Dentry> list = Dentry.muliGet(
					serviceName, null, paths, CSConstant.CS_ORDERBY_UPDATEAT_DESC, session, exception);
			
			if(CollectionUtils.isEmpty(list)){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
	            		LifeCircleErrorMessageMapper.CSSdkFail.getCode(),
	            		"CS SDK -- 获取目录项出错！");
			}
			
			return list.get(0);
		} catch (Exception e) {

			LOG.error(LifeCircleErrorMessageMapper.CSSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
            		LifeCircleErrorMessageMapper.CSSdkFail.getCode(),e.getLocalizedMessage());
		}
	}
	
	/**
	 * 获取目录项列表
	 * @author xiezy
	 * @date 2016年9月8日
	 * @param path			目录项Path
	 * @param serviceName	申请的内容服务名称
	 * @param session		会话标签
	 * @return
	 */
	public static List<Dentry> getDentryItems(String path, String serviceName, String session){
		try {
			List<Dentry> list = Dentry.list(serviceName, path, null, null, null, 999, session);
			
			return list;
		} catch (Exception e) {

			LOG.error(LifeCircleErrorMessageMapper.CSSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
            		LifeCircleErrorMessageMapper.CSSdkFail.getCode(),e.getLocalizedMessage());
		}
	}
	
	/**
	 * 批量复制目录项
	 * @author xiezy
	 * @date 2016年9月8日
	 * @param parentId			复制的原目录项的父ID
	 * @param srcDentryIds		复制的原目录项ID列表
	 * @param dstDentryId		要复制的目标目录项ID
	 * @param serviceName		申请的内容服务名称
	 * @param session			会话标签
	 * @return
	 */
	public static List<Dentry> batchCopyDir(String parentId, List<String> srcDentryIds, 
			String dstDentryId, String serviceName, String session){
		try {
			List<Dentry> list = Dentry.muliCopy(serviceName, parentId, srcDentryIds, dstDentryId, session);
			
			return list;
		} catch (Exception e) {
			
			LOG.error(LifeCircleErrorMessageMapper.CSSdkFail.getMessage(),e);
            
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
            		LifeCircleErrorMessageMapper.CSSdkFail.getCode(),e.getLocalizedMessage());
		}
	}
	
	/**
	 * 拷贝 -- 组合CS接口后应用于NDR
	 * 
	 * 使用到: getDentry(),getDentryItems(),batchCopyDir()
	 * @author xiezy
	 * @date 2016年9月8日
	 * @param srcPath			源目录项path
	 * @param descPath			目标目录项path
	 * @param serviceName		申请的内容服务名称
	 * @param session			会话标签
	 */
	public static void copyDirOnNdr(String srcPath, String descPath, String serviceName, String session){
		//获取源目录项
		Dentry srcDentry = getDentry(srcPath, serviceName, session);
		//获取目标目录项
		Dentry descDentry = getDentry(descPath, serviceName, session);
		
		//复制的原目录项列表
		List<Dentry> srcDentryItems = getDentryItems(srcPath, serviceName, session);
		
		if(CollectionUtils.isNotEmpty(srcDentryItems)){
			List<String> srcDentryIds = new ArrayList<String>();
			for(Dentry dentry : srcDentryItems){
				srcDentryIds.add(dentry.getDentryId());
			}
			
			//拷贝
			batchCopyDir(srcDentry.getDentryId(), srcDentryIds, 
					descDentry.getDentryId(), serviceName, session);
		}
	}
}

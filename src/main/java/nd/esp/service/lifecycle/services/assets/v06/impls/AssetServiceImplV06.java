package nd.esp.service.lifecycle.services.assets.v06.impls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import nd.esp.service.lifecycle.daos.assets.v06.AssetDao;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.v06.AssetModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Asset;
import nd.esp.service.lifecycle.repository.sdk.AssetRepository;
import nd.esp.service.lifecycle.services.assets.v06.AssetServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * 业务实现类
 * @author xuzy
 *
 */
@Service("assetServiceV06")
@Transactional
public class AssetServiceImplV06 implements AssetServiceV06 {
	@Autowired
	private NDResourceService ndResourceService;
	
	@Autowired
	private AssetRepository assetRepository;
	
	@Autowired
	private AssetDao assetDao;
	
	@Override
	public AssetModel createAsset(AssetModel am) {
		if("auto_increment".equals(am.getTitle())){
			Pattern suitePattern = Pattern.compile("^套件[0-9]*$");
			Pattern subSuitePattern = Pattern.compile("^套件[0-9.]*$");
			
			String cp = am.getCustomProperties();
			Map<String,Object> map = ObjectUtils.fromJson(cp, Map.class);
			String category = (String)map.get("category");
			String parent = (String)map.get("parent");
			if("$RA0502".equals(category)){
				//套件目录
				//1、查找出所有的套件目录
				List<Asset> assetList = assetDao.queryByCategory(category);
				//2、判断是否存在重复
				String description = am.getDescription();
				if(description == null){
		            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
							 "LC/CHECK_PARAM_VALID_FAIL",
                             "description不能为空");
				}
				if(CollectionUtils.isNotEmpty(assetList)){
					for (Asset asset : assetList) {
						String des = asset.getDescription();
						if(des != null && des.equals(description)){
							throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
									 "LC/CHECK_PARAM_VALID_FAIL",
		                             "description已存在");
						}
					}
				}
				//3、根据parent获取所有的子节点
				if(parent != null){
					Asset parentAsset = null;
					//4、根据子节点算出新的编号
					List<String> titles = new ArrayList<String>();
					if(parent.toLowerCase().equals("root")){
						for (Asset asset : assetList) {
							if(suitePattern.matcher(asset.getTitle()).find()){
								titles.add(asset.getTitle());
							}
						}
					}else{
						//判断资源是否存在
						try {
							parentAsset = assetRepository.get(parent);
							if(parentAsset == null || !parentAsset.getEnable()){
								throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
										 "LC/CHECK_PARAM_VALID_FAIL",
			                             "parent对应的资源不存在");
							}
						} catch (EspStoreException e) {
							e.printStackTrace();
						}
						
						List<Asset> list2 = assetDao.queryBySourceId(parent,category);
						if(CollectionUtils.isNotEmpty(list2)){
							for (Asset asset : list2) {
								if(subSuitePattern.matcher(asset.getTitle()).find()){
									titles.add(asset.getTitle());
								}
							}
						}
					}
					String s = generateSuiteTitle(parentAsset,titles);
					am.setTitle(s);
				}
				
			}else if("$RA0503".equals(category)){
				//教学目标类型（套件）
				//1、判断教学目标类型是否重复
				List<Asset> assetList = assetDao.queryByCategory(category);
				String description = am.getDescription();
				if(description == null){
		            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
							 "LC/CHECK_PARAM_VALID_FAIL",
                             "description不能为空");
				}
				if(CollectionUtils.isNotEmpty(assetList)){
					for (Asset asset : assetList) {
						String des = asset.getDescription();
						if(des != null && des.equals(description)){
							throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
									 "LC/CHECK_PARAM_VALID_FAIL",
		                             "description已存在");
						}
					}
				}
				//2、算code
				Asset parentAsset = null;
				//判断资源是否存在
				try {
					parentAsset = assetRepository.get(parent);
					if(parentAsset == null || !parentAsset.getEnable()){
						throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
								 "LC/CHECK_PARAM_VALID_FAIL",
	                             "parent对应的资源不存在");
					}
				} catch (EspStoreException e) {
					e.printStackTrace();
				}
				String title = null;
				if(parentAsset != null && parentAsset.getTitle() != null){
					title = parentAsset.getTitle().substring(2);
				}
				try {
					int likeName = 0;
					if(title != null && title.contains(".")){
						title = title.substring(0,title.indexOf("."));
						likeName = Integer.valueOf(title);
					}
					List<Asset> list2 = assetDao.queryInsTypesByCategory(String.valueOf(likeName), category);
					if(CollectionUtils.isNotEmpty(list2)){
						int maxNum = 0;
						Pattern p = Pattern.compile("^[0-9]+$");
						for (Asset asset : list2) {
							if(p.matcher(asset.getTitle()).matches()){
								if(Integer.valueOf(asset.getTitle()) > maxNum){
									maxNum = Integer.valueOf(asset.getTitle()); 
								}
							}
						}
						if(maxNum > 0){
							am.setTitle(String.valueOf(maxNum+1));
						}else{
							am.setTitle(String.valueOf(likeName)+"01");
						}
					}else{
						am.setTitle(String.valueOf(likeName)+"01");
					}
				} catch (NumberFormatException e) {
					am.setTitle(String.valueOf(System.currentTimeMillis()));
				}
			}
		}
		return (AssetModel)ndResourceService.create(ResourceNdCode.assets.toString(), am);
	}

	@Override
	public AssetModel updateAsset(AssetModel am) {
		return (AssetModel)ndResourceService.update(ResourceNdCode.assets.toString(), am);
	}
	
	private String generateSuiteTitle(Asset parentAsset,List<String> titleList){
		if(CollectionUtils.isNotEmpty(titleList)){
			String max = titleList.get(0);
			if(max.length() > 2){
				if(max.contains(".")){
					String s = max.substring(max.lastIndexOf(".")+1);
					try {
						int i = Integer.valueOf(s);
						return max.substring(0,max.lastIndexOf(".")+1)+(i+1);
					} catch (NumberFormatException e) {
					}
				}else{
					String s = max.substring(2);
					try {
						int i = Integer.valueOf(s);
						return "套件"+(i+1);
					} catch (NumberFormatException e) {
					}
				}
			}
		}else{
			if(parentAsset != null){
				String title = parentAsset.getTitle();
				if(title != null && title.length() > 2){
					return title+ ".1";
				}else{
					return "套件-1";
				}
			}else{
				return "套件1";
			}
		}
		return "套件-0";
	}

	@Override
	public AssetModel patchAsset(AssetModel am) {
		return (AssetModel)ndResourceService.patch(ResourceNdCode.assets.toString(), am);
	}
}

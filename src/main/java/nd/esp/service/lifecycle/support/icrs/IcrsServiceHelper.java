package nd.esp.service.lifecycle.support.icrs;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.icrs.IcrsResource;
import nd.esp.service.lifecycle.repository.model.icrs.IcrsSyncErrorRecord;
import nd.esp.service.lifecycle.repository.sdk.icrs.IcrsResourceRepository;
import nd.esp.service.lifecycle.repository.sdk.icrs.IcrsSyncErrorRecordRepository;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.nd.gaea.client.http.WafSecurityHttpClient;

/**
 * 智慧教室-课堂数据统计平台 帮助类
 * 
 * @author xiezy
 * @date 2016年9月18日
 */
public class IcrsServiceHelper {
	private static final Logger LOG = LoggerFactory
			.getLogger(IcrsServiceHelper.class);

	@Autowired
	private IcrsResourceRepository icrsResourceRepository;
	@Autowired
	private IcrsSyncErrorRecordRepository icrsSyncErrorRecordRepository;

	@Qualifier(value = "defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate defaultJdbcTemplate;
	@Qualifier(value = "questionJdbcTemplate")
	@Autowired
	private JdbcTemplate questionJdbcTemplate;

	public void syncIcrsByType(String resType, boolean isInit) {
		String querySql = "select distinct ndr.identifier as id,ndr.enable as ndren,ndr.create_time as ct,rv.target as target "
				+ "from ndresource ndr inner join res_coverages rv "
				+ "on ndr.identifier=rv.resource ";
		
		// 需要过滤维度
		if (resType.equals(IndexSourceType.AssetType.getName())
				|| resType.equals(IndexSourceType.SourceCourseWareType.getName())
				|| resType.equals(IndexSourceType.QuestionType.getName())) {
			querySql += " inner join resource_categories rc on ndr.identifier=rc.resource ";
		}

		querySql += " where " + (isInit ? "ndr.enable=1 and " : "")
				+ "ndr.primary_category='" + resType + "' ";
		
		if(resType.equals(IndexSourceType.QuestionType.getName()) ||
				resType.equals(IndexSourceType.SourceCourseWareObjectType.getName())){
			querySql += " and ndr.estatus != '" + LifecycleStatus.CREATING.getCode() + "' ";
		}
		
		querySql += " and rv.target_type='User' and rv.strategy='OWNER' and rv.res_type='" + resType + "' ";
		
		if (resType.equals(IndexSourceType.AssetType.getName())) {// assets
			querySql += " and rc.primary_category='" + resType
					+ "' and rc.taxOnCode in "
					+ "('$RA0101','$RA0102','$RA0103','$RA0104')";
		}
		
		if (resType.equals(IndexSourceType.SourceCourseWareType.getName())) {// coursewares
			querySql += " and rc.primary_category='" + resType
					+ "' and rc.taxOnCode in "
					+ "('$F010003','$F060005','$F010004')";
		}
		
		if (resType.equals(IndexSourceType.QuestionType.getName())) {// questions
			querySql += " and rc.primary_category='" + resType
					+ "' and rc.taxOnCode not in "
					+ "('$RE0211','$RE0206')";
		}
		
//		if (resType
//				.equals(IndexSourceType.SourceCourseWareObjectType.getName())) {// coursewareobjects
//			querySql += " and rc.primary_category='" + resType
//					+ "' and rc.taxOnCode like '$RE04%'";
//		}

		if (!isInit) {// 获取当前时间前一小时的数据有变动的数据
			querySql += " and ndr.last_update > " + getOneHourAgoTime();
		}

		// 分页参数
		int offset = 0;
		int pageSize = 500;

		while (true) {
			querySql += "  limit " + offset + "," + pageSize;

			System.out.println("ICRS查询SQL:" + querySql);

			final List<SyncIcrsModel> list = new ArrayList<SyncIcrsModel>();
			getJdbcTemplate(resType).query(querySql, new RowMapper<String>() {
				@Override
				public String mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					SyncIcrsModel sim = new SyncIcrsModel();
					sim.setIdentifier(rs.getString("id"));
					sim.setEnable(rs.getInt("ndren"));
					sim.setCreateTime(rs.getLong("ct"));
					sim.setTarget(rs.getString("target"));
					list.add(sim);

					return null;
				}
			});

			// 方便错误信息记录
			String resId = "";
			Long createTime = 0L;
			String target = "";

			if (CollectionUtils.isNotEmpty(list)) {
				List<IcrsResource> syncList = new ArrayList<IcrsResource>();
				for (SyncIcrsModel model : list) {
					try {
						resId = model.getIdentifier();
						createTime = model.getCreateTime();
						target = model.getTarget();

						// 先查询是否同步过
						IcrsResource searchExample = new IcrsResource();
						searchExample.setResUuid(model.getIdentifier());
						List<IcrsResource> existList = icrsResourceRepository
								.getAllByExample(searchExample);

						// 如果已同步过，先删除旧数据
						if (CollectionUtils.isNotEmpty(existList)) {
							List<String> deleteIds = new ArrayList<String>();
							for (IcrsResource ir : existList) {
								deleteIds.add(ir.getIdentifier());
							}

							if (CollectionUtils.isNotEmpty(deleteIds)) {
								icrsResourceRepository.batchDel(deleteIds);
							}
						}

						if (model.getEnable() == 0) {
							continue;
						}

						// 新增同步记录
						// 1.查询用户姓名,调用UC接口
						String userName = getUserName(model.getTarget());
						// 2.获取对应学校id,调用Admin接口
						String schoolId = getSchoolId(model.getTarget());
						// 3.获取创建日期
						String createDate = getCreateDate(model.getCreateTime());
						// 4.获取创建时段
						Integer createHour = getCreateHour(model
								.getCreateTime());
						// 5.获取相关联的章节和教材id
						Map<String, String> chapterAndTmInfo = getChapterAndTeachingmaterialInfo(
								resType, model.getIdentifier());
						if (CollectionUtils.isNotEmpty(chapterAndTmInfo)) {
							for (String chapterId : chapterAndTmInfo.keySet()) {
								IcrsResource icrs = new IcrsResource();
								icrs.setIdentifier(UUID.randomUUID().toString());
								icrs.setResType(resType);
								icrs.setResUuid(model.getIdentifier());
								icrs.setSchoolId(schoolId);
								icrs.setTeacherId(model.getTarget());
								icrs.setTeacherName(userName);
								icrs.setCreateTime(new Timestamp(model
										.getCreateTime()));
								icrs.setCreateDate(createDate);
								icrs.setCreateHour(createHour);

								icrs.setChapterUuid(chapterId);
								icrs.setTeachmaterialUuid(chapterAndTmInfo
										.get(chapterId));

								// 获取教材的年级和学科维度
								Map<String, List<String>> gradeAndSubjectMap = getGradeAndSubjectCode(
										IndexSourceType.TeachingMaterialType
												.getName(),
										chapterAndTmInfo.get(chapterId));

								// 教材认为只有一个年级和学科维度
								if (CollectionUtils
										.isNotEmpty(gradeAndSubjectMap
												.get("grade"))) {
									icrs.setGradeCode(gradeAndSubjectMap.get(
											"grade").get(0));
								} else {
									icrs.setGradeCode("");
								}
								if (CollectionUtils
										.isNotEmpty(gradeAndSubjectMap
												.get("subject"))) {
									icrs.setSubjectCode(gradeAndSubjectMap.get(
											"subject").get(0));
								} else {
									icrs.setSubjectCode("");
								}

								syncList.add(icrs);
							}
						} else {
							// 获取资源本身的年级和学科维度
							Map<String, List<String>> gradeAndSubjectMap = getGradeAndSubjectCode(
									resType, model.getIdentifier());
							if (CollectionUtils.isNotEmpty(gradeAndSubjectMap
									.get("grade"))) {
								for (int i = 0; i < gradeAndSubjectMap.get(
										"grade").size(); i++) {
									IcrsResource icrs = new IcrsResource();
									icrs.setIdentifier(UUID.randomUUID()
											.toString());
									icrs.setResType(resType);
									icrs.setResUuid(model.getIdentifier());
									icrs.setSchoolId(schoolId);
									icrs.setTeacherId(model.getTarget());
									icrs.setTeacherName(userName);
									icrs.setCreateTime(new Timestamp(model
											.getCreateTime()));
									icrs.setCreateDate(createDate);
									icrs.setCreateHour(createHour);

									icrs.setChapterUuid("");
									icrs.setTeachmaterialUuid("");
									icrs.setGradeCode(gradeAndSubjectMap.get(
											"grade").get(i));
									icrs.setSubjectCode(gradeAndSubjectMap.get(
											"subject").get(i));

									syncList.add(icrs);
								}
							} else {
								IcrsResource icrs = new IcrsResource();
								icrs.setIdentifier(UUID.randomUUID().toString());
								icrs.setResType(resType);
								icrs.setResUuid(model.getIdentifier());
								icrs.setSchoolId(schoolId);
								icrs.setTeacherId(model.getTarget());
								icrs.setTeacherName(userName);
								icrs.setCreateTime(new Timestamp(model
										.getCreateTime()));
								icrs.setCreateDate(createDate);
								icrs.setCreateHour(createHour);

								icrs.setChapterUuid("");
								icrs.setTeachmaterialUuid("");
								icrs.setGradeCode("");
								icrs.setSubjectCode("");

								syncList.add(icrs);
							}
						}
					} catch (Exception e) {
						IcrsSyncErrorRecord errorRecord = new IcrsSyncErrorRecord();
						errorRecord.setIdentifier(UUID.randomUUID().toString());
						errorRecord.setResType(resType);
						errorRecord.setResUuid(resId);
						errorRecord.setCreateTime(new BigDecimal(createTime));
						errorRecord.setTarget(target);
						errorRecord.setErrorMessage(e.getMessage());
						try {
							icrsSyncErrorRecordRepository.add(errorRecord);
						} catch (EspStoreException e1) {
							LOG.error("icrs同步错误记录出现异常", e1);
						}

						continue;
					}
				}

				if (CollectionUtils.isNotEmpty(syncList)) {
					try {
						icrsResourceRepository.batchAdd(syncList);
					} catch (EspStoreException e) {
						IcrsSyncErrorRecord errorRecord = new IcrsSyncErrorRecord();
						errorRecord.setIdentifier(UUID.randomUUID().toString());
						errorRecord.setResType(resType);
						errorRecord.setResUuid(resId);
						errorRecord.setCreateTime(new BigDecimal(createTime));
						errorRecord.setTarget(target);
						errorRecord.setErrorMessage(e.getMessage());

						try {
							icrsSyncErrorRecordRepository.add(errorRecord);
						} catch (EspStoreException e1) {
							LOG.error("icrs同步错误记录出现异常", e1);
						}
					}
				}

			} else {
				break;
			}

			// 处理分页
			querySql = querySql.substring(0, querySql.lastIndexOf("limit"));
			offset += pageSize;
		}
	}

	/**
	 * 获取当前系统时间前一个小时的时间戳
	 * 
	 * @author xiezy
	 * @date 2016年9月12日
	 * @return
	 */
	private long getOneHourAgoTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY,
				calendar.get(Calendar.HOUR_OF_DAY) - 1);
		return calendar.getTimeInMillis();
	}

	/**
	 * 获取创建日期,格式：yyyy-MM-dd
	 * 
	 * @author xiezy
	 * @date 2016年9月18日
	 * @param createTime
	 * @return
	 */
	private String getCreateDate(Long createTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new Date(createTime));
	}

	/**
	 * 获取创建时段,格式：HH
	 * 
	 * @author xiezy
	 * @date 2016年9月18日
	 * @param createTime
	 * @return
	 */
	private Integer getCreateHour(Long createTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		String hour = sdf.format(new Date(createTime));

		return Integer.parseInt(hour) + 1;
	}

	/**
	 * 获取资源相关联的章节和教材id
	 * 
	 * @author xiezy
	 * @date 2016年9月18日
	 * @param resType
	 * @param resId
	 * @return
	 */
	private Map<String, String> getChapterAndTeachingmaterialInfo(
			String resType, String resId) {
		String querySql = "select distinct rr.source_uuid as cid,tm.identifier as tmid";
		querySql += " FROM resource_relations rr INNER JOIN chapters c ON rr.source_uuid=c.identifier";
		querySql += " INNER JOIN ndresource tm ON c.teaching_material=tm.identifier";
		querySql += " WHERE rr.enable=1 and rr.res_type='chapters' AND rr.resource_target_type='"
				+ resType + "'";
		querySql += " AND rr.target='" + resId + "'";
		querySql += " AND tm.primary_category='teachingmaterials' AND tm.enable=1";

		final Map<String, String> map = new HashMap<String, String>();
		defaultJdbcTemplate.query(querySql,
				new RowMapper<Map<String, String>>() {
					@Override
					public Map<String, String> mapRow(ResultSet rs, int rowNum)
							throws SQLException {

						map.put(rs.getString("cid"), rs.getString("tmid"));
						return null;
					}
				});

		return map;
	}

	/**
	 * 获取资源的年级和学科维度Code
	 * 
	 * @author xiezy
	 * @date 2016年9月18日
	 * @param resType
	 * @param resource
	 * @return
	 */
	private Map<String, List<String>> getGradeAndSubjectCode(String resType,
			String resource) {
		final List<String> gradeCodeList = new ArrayList<String>();
		final List<String> subjectCodeList = new ArrayList<String>();

		String querySql = "select distinct rc.taxonpath as path from resource_categories rc where rc.resource='"
				+ resource + "'";
		querySql += " and rc.primary_category='" + resType + "'";

		getJdbcTemplate(resType).query(querySql, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String path = rs.getString("path");
				if (StringUtils.hasText(path) && path.startsWith("K12/")) {
					List<String> items = Arrays.asList(path.split("/"));
					if (CollectionUtils.isNotEmpty(items) && items.size() == 6) {
						if (StringUtils.hasText(items.get(2))) {
							gradeCodeList.add(items.get(2));
						} else {
							gradeCodeList.add(items.get(1));
						}

						subjectCodeList.add(items.get(3));
					}
				}

				return null;
			}
		});

		Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
		resultMap.put("grade", gradeCodeList);
		resultMap.put("subject", subjectCodeList);

		return resultMap;
	}

	/**
	 * 获取用户姓名
	 * 
	 * @author xiezy
	 * @date 2016年9月14日
	 * @param userid
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getUserName(String userid) {
		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
		String url = LifeCircleApplicationInitializer.properties
				.getProperty("esp_uc_api_domain") + "users/" + userid;

		Map<String, Object> userInfo = new HashMap<String, Object>();
		try {
			userInfo = wafSecurityHttpClient.get(url, Map.class);
		} catch (Exception e) {
			return "";
		}

		if (CollectionUtils.isEmpty(userInfo)) {
			return "";
		}

		return (String) userInfo.get("nick_name");
	}

	/**
	 * 根据教师id获取学校id
	 * 
	 * @author xiezy
	 * @date 2016年9月14日
	 * @param teacherId
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getSchoolId(String teacherId) {
		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
		String url = LifeCircleApplicationInitializer.properties
				.getProperty("admin.uri") + "v06/schools/" + teacherId;

		Map<String, Object> schoolInfo = new HashMap<String, Object>();
		try {
			schoolInfo = wafSecurityHttpClient.get(url, Map.class);
		} catch (Exception e) {
			return "";
		}

		if (CollectionUtils.isEmpty(schoolInfo)
				|| !schoolInfo.containsKey("items")) {
			return "";
		}

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> items = (List<Map<String, Object>>) schoolInfo
				.get("items");
		if (CollectionUtils.isEmpty(items)) {
			return "";
		}

		if (!items.get(0).containsKey("node_id")) {
			return "";
		}

		Long schoolId = (Long) items.get(0).get("node_id");
		return schoolId.toString();
	}

	/**
	 * 根据资源类型返回JdbcTemplate
	 * 
	 * @author xiezy
	 * @date 2016年9月12日
	 * @param resType
	 * @return
	 */
	private JdbcTemplate getJdbcTemplate(String resType) {
		if (CommonServiceHelper.isQuestionDb(resType)) {
			return questionJdbcTemplate;
		}

		return defaultJdbcTemplate;
	}
}

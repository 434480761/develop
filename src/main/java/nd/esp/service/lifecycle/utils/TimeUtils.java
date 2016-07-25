package nd.esp.service.lifecycle.utils;

import java.sql.Timestamp;

/**
 * <p>Title: TimeUtils</p>
 * <p>Description: TimeUtils</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/21 </p>
 *
 * @author lanyl
 */
public class TimeUtils {

	/**
	 * 是否超过24小时
	 * @param timestamp
	 * @return
	 * @author lanyl
	 */
	public static Boolean getTimeIntervalWith24Hour(Timestamp timestamp ) {
		Timestamp endTime = currentTimestamp();
		return getIntervalWithHour(timestamp, endTime, 24);
	}


	/**
	 * 判断时间间隔
	 * @param startTime
	 * @param endTime
	 * @param stepHours
	 * @return
	 * @author lanyl
	 */
	public static Boolean getIntervalWithHour(Timestamp startTime,Timestamp endTime, int stepHours) {
		long step = endTime.getTime() - startTime.getTime();
		return ( (step/(1000*60*60)) >= stepHours)? true : false;
	}

	/**
	 * 获取系统当前时间
	 * @return
	 * @author lanyl
	 */
	public static Timestamp currentTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}
}

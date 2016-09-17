/**   
 * @Title: DateUtils.java 
 * @Package: com.nd.esp.store.common.utils 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年4月9日 下午6:53:19 
 * @version 1.3.1 
 */

package nd.esp.service.lifecycle.repository.common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.base.Stopwatch;

/**
 * The Class DateUtils.
 * 
 * @author Rainy(yang.lin)
 * @version V1.0
 * @Description
 * @date 2015年4月9日 下午6:53:19
 */

public class DateUtils {

	/** Logging. */
	private static Logger logger = LoggerFactory.getLogger(DateUtils.class);

	/** The Constant DEFAULT_ACCEPTABLE_FORMATS. */
	protected static final String[] DEFAULT_ACCEPTABLE_FORMATS;

	/** The Constant DEFAULT_PATTERN. */
	protected static final String DEFAULT_PATTERN;

	/** The Constant DEFAULT_TIMEZONE. */
	protected static final TimeZone DEFAULT_TIMEZONE;

	/** The Constant defaultFormat. */
	private final static ThreadSafeSimpleDateFormat defaultFormat;

	/** The Constant acceptableFormats. */
	private final static ThreadSafeSimpleDateFormat[] acceptableFormats;

	static {
		DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT+8");
		DEFAULT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
		final List<String> acceptablePatterns = new ArrayList<String>();

		acceptablePatterns.add("yyyy-MM-dd HH:mm:ss");
		acceptablePatterns.add("yyyy-MM-dd");
		acceptablePatterns.add("yyyy-MM-dd HH:mm:ss.S");

		DEFAULT_ACCEPTABLE_FORMATS = (String[]) acceptablePatterns
				.toArray(new String[acceptablePatterns.size()]);

		defaultFormat = new ThreadSafeSimpleDateFormat(DEFAULT_PATTERN,
				DEFAULT_TIMEZONE, Locale.CHINESE, 4, 20, false);
		acceptableFormats = acceptablePatterns != null ? new ThreadSafeSimpleDateFormat[acceptablePatterns
				.size()] : new ThreadSafeSimpleDateFormat[0];
		for (int i = 0; i < acceptablePatterns.size(); i++) {
			acceptableFormats[i] = new ThreadSafeSimpleDateFormat(
					acceptablePatterns.get(i), DEFAULT_TIMEZONE, 1, 20, false);
		}
	}

	/**
	 * Paser.
	 * 
	 * @param date
	 *            the date
	 * @return the date
	 */
	public static Date paser(String date) {
		return paser(date, false);
	}

	/**
	 * Paser.
	 * 
	 * @param date
	 *            the date
	 * @param ignore
	 *            the ignore
	 * @return the date
	 */
	public static Date paser(String date, boolean ignore) {
//		Stopwatch stopwatch = new Stopwatch().start();
		if (!StringUtils.isEmpty(date)) {
			try {
				
			    if (logger.isDebugEnabled()) {
                    
			        logger.debug("Time: {}", date);
			        
                }
			            
				return defaultFormat.parse(date);
			} catch (ParseException e) {

			    if (logger.isWarnEnabled()) {
                    
			        logger.warn("warn format: {}", e.getMessage());
			        
                }
			            
			}

			for (int i = 0; i < acceptableFormats.length; i++) {
				try {
				    
				    if (logger.isDebugEnabled()) {
                        
				        logger.debug("threadSafeSimpleDateFormat parse Time: {}", date);
				        
                    }
					
					return acceptableFormats[i].parse(date);
				} catch (ParseException e) {
					// try next

					if (i == acceptableFormats.length - 1) {
						if (ignore) {
						    
						    if (logger.isDebugEnabled()) {
                                
						        logger.debug("日期转换错误：{}", date);
						        
                            }
							        
						} else {
						    
						    if (logger.isErrorEnabled()) {
                                
						        logger.error("日期转换错误：{}", date);
						        
                            }
							        
							throw new IllegalArgumentException("解析日期错误，无效的日期格式");
						}
					}
				}
			}
		}
		
//		if (logger.isDebugEnabled()) {
//            
//		    logger.debug("paser cost time :{}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
//		    
//        }
		        
		return null;
	}

	public static String paser(Date date) {
		String datestr = defaultFormat.format(date);
		return datestr;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws ParseException
	 *             the parse exception
	 */
	public static void main(String[] args) throws ParseException {
		String a = "2015-04-09T16:02:41+0800";
		/*
		 * SimpleDateFormat sp = new
		 * SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		 * System.err.println(sp.parse(a).getTime());
		 */
		System.out.println(paser(a, true));
		System.out.println(paser(a, true).getTime());
		String b = "2015-04-09T19:41:06+0800";
		System.out.println(paser(null, true));
		System.out.println(paser(b, true).getTime());
	}
}

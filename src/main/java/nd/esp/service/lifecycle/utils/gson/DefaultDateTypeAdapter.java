package nd.esp.service.lifecycle.utils.gson;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 日期类型适配器
 *
 * @author bifeng.liu
 */
public class DefaultDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultDateTypeAdapter.class);

    protected static final String[] DEFAULT_ACCEPTABLE_FORMATS;
    protected static final String DEFAULT_PATTERN;
    protected static final TimeZone DEFAULT_TIMEZONE;

    static {
        //DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT++0:00");
        DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT+8");
        DEFAULT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
        final List acceptablePatterns = new ArrayList();
        acceptablePatterns.add("yyyy-MM-dd HH:mm:ss");
        acceptablePatterns.add("yyyy-MM-dd");
        acceptablePatterns.add("yyyy-MM-dd HH:mm:ss.S");
        acceptablePatterns.add("yyyy-MM-ddTHH:mm:ssZ");
        
        DEFAULT_ACCEPTABLE_FORMATS = (String[]) acceptablePatterns.toArray(new String[acceptablePatterns.size()]);
    }

    private final ThreadSafeSimpleDateFormat defaultFormat;
    private final ThreadSafeSimpleDateFormat[] acceptableFormats;
    private final Locale locale;


    /**
     * Construct a DateTypeAdapter with standard formats and lenient set off.
     */
    public DefaultDateTypeAdapter() {
        this(DEFAULT_PATTERN, DEFAULT_ACCEPTABLE_FORMATS, Locale.CHINESE, DEFAULT_TIMEZONE, false);
    }

    /**
     * Construct a DateTypeAdapter with standard formats and lenient set off.
     */
    public DefaultDateTypeAdapter(String defaultFormat) {
        this(defaultFormat, DEFAULT_ACCEPTABLE_FORMATS, Locale.CHINESE, DEFAULT_TIMEZONE, false);
    }

    /**
     * Construct a DateTypeAdapter.
     *
     * @param defaultFormat     the default format
     * @param acceptableFormats fallback formats
     * @param locale            locale to use for the format
     * @param timeZone          the TimeZone used to serialize the Date
     * @param lenient           the lenient setting of {@link java.text.SimpleDateFormat#setLenient(boolean)}
     * @since 1.4.4
     */
    public DefaultDateTypeAdapter(String defaultFormat, String[] acceptableFormats,
                                  Locale locale, TimeZone timeZone, boolean lenient) {
        this.locale = locale;
        this.defaultFormat = new ThreadSafeSimpleDateFormat(defaultFormat, timeZone, locale, 4, 20, lenient);
        this.acceptableFormats = acceptableFormats != null
                ? new ThreadSafeSimpleDateFormat[acceptableFormats.length]
                : new ThreadSafeSimpleDateFormat[0];
        for (int i = 0; i < this.acceptableFormats.length; i++) {
            this.acceptableFormats[i] = new ThreadSafeSimpleDateFormat(acceptableFormats[i], timeZone, 1, 20, lenient);
        }
    }


    /**
     * JSON字符串转为对象时调用
     *
     * @param json
     * @param typeOfT
     * @param context
     * @return
     * @throws JsonParseException
     */
    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonPrimitive)) {
            //加入对/Date(1415167614000+0800)/格式的支持
            try {
                //单个情况
                String str = json.toString();
                //HashMap map =new Gson().fromJson(str,new TypeToken<HashMap<String,String>>(){}.getType()); str=(String)map.values().iterator().next();
                 str = str.split(":")[1].replace("}]", "").replace("\"", "");
                return forceDate(str);
            } catch (Exception e) {
                // try next ...
                throw new JsonParseException("The date should be a string value");
            }
        }
        String str = json.getAsString();
        if(StringUtils.isEmpty(str)){
            return null;
        }

        try {
            return (Date)(((Class) typeOfT).getConstructor(Long.TYPE).newInstance(defaultFormat.parse(str).getTime()));
        } catch (Exception e) {
        	LOG.info("转换失败:{}",e.getMessage());
        }


        try {
            Date date =forceDate(str);
            if(date!=null){
                return date;
            }
        } catch (Exception e) {
            // try next ...

        }
        for (int i = 0; i < acceptableFormats.length; i++) {
            try {
                return acceptableFormats[i].parse(str);
            } catch (ParseException e3) {
                // no worries, let's try the next format.
            }
        }
        // no dateFormats left to try
        throw new JsonParseException("Cannot parse date " + str);

    }


    private Date forceDate(String str){
        str = str.replace("\"", "");
        Pattern pattern=Pattern.compile("/Date\\([0-9]{1,}\\+[0-9]{4}\\)/");
        Matcher matcher =pattern.matcher(str);
        if(matcher.matches()){
            pattern= Pattern.compile("[0-9]{1,}\\+[0-9]{4}");
            matcher =pattern.matcher(str);
            while(matcher.find()){
                String dateStr[]=matcher.group().split(Pattern.quote("+"));
                Calendar calendar=Calendar.getInstance() ;
                calendar.setTimeInMillis(Long.valueOf(dateStr[0]));
                return calendar.getTime();
            }
        }
        return null;
    }

    /**
     * 对象转为JSON字符串时调用
     *
     * @param src
     * @param typeOfSrc
     * @param context
     * @return
     */
    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        String dateFormatAsString = defaultFormat.format(src);//format.format(new Date(src.getTime()));
        return new JsonPrimitive(dateFormatAsString);
    }
}

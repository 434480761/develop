package nd.esp.service.lifecycle.utils.gson;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import nd.esp.service.lifecycle.support.Pool;

/**
 * 请在这里输入说明
 *
 * @author bifeng.liu
 */
public class ThreadSafeSimpleDateFormat {

    private final String formatString;
    private final Pool pool;
    private final TimeZone timeZone;

    public ThreadSafeSimpleDateFormat(
            String format, TimeZone timeZone, int initialPoolSize, int maxPoolSize,
            final boolean lenient) {
        this(format, timeZone, Locale.ENGLISH, initialPoolSize, maxPoolSize, lenient);
    }

    public ThreadSafeSimpleDateFormat(
            String format, TimeZone timeZone, final Locale locale, int initialPoolSize,
            int maxPoolSize, final boolean lenient) {
        formatString = format;
        this.timeZone = timeZone;
        pool = new Pool(initialPoolSize, maxPoolSize, new Pool.Factory() {
            public Object newInstance() {
                SimpleDateFormat dateFormat = new SimpleDateFormat(formatString, locale);
                dateFormat.setLenient(lenient);
                return dateFormat;
            }

        });
    }

    public String format(Date date) {
        DateFormat format = fetchFromPool();
        try {
            return format.format(date);
        } finally {
            pool.putInPool(format);
        }
    }

    public Date parse(String date) throws ParseException {
        DateFormat format = fetchFromPool();
        try {
            return format.parse(date);
        } finally {
            pool.putInPool(format);
        }
    }

    private DateFormat fetchFromPool() {
        DateFormat format = (DateFormat) pool.fetchFromPool();
        TimeZone tz = timeZone != null ? timeZone : TimeZone.getDefault();
        if (!tz.equals(format.getTimeZone())) {
            format.setTimeZone(tz);
        }
        return format;
    }

    public String toString() {
        return formatString;
    }
}

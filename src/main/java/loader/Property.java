package loader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    String propertyName() default "";
    String key() default "";
    DateFormat datePattern() default DateFormat.DOT;
    TimeFormat timePattern() default TimeFormat.DASH;
    enum DateFormat{
        DOT("\\d{4}\\.\\d{2}\\.\\d{2}", "\\."),
        DASH("\\d{4}-\\d{2}-\\d{2}", "-");
        final String pattern;
        final String separator;
        DateFormat(String pattern, String separator) {
            this.pattern = pattern;
            this.separator = separator;
        }
    }
    enum TimeFormat{
        COLON("\\d{2}:\\d{2}", ":"),
        DASH("\\d{2}-\\d{2}", "-");
        final String pattern;
        final String separator;
        TimeFormat(String pattern, String separator) {
            this.pattern = pattern;
            this.separator = separator;
        }
    }
}

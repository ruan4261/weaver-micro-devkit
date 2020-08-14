package weaver.interfaces.micro.devkit.core;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * 业务性质的公共缓存，被缓存的类必须为{@code final}状态。
 *
 * @author ruan4261
 */
public interface CacheBase {

    String EMPTY = "";
    String SUCCESS = "SUCCESS";
    String FAIL = "FAIL";
    String LINE_SEPARATOR = System.lineSeparator();
    BigDecimal ZERO = BigDecimal.ZERO;
    DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

}

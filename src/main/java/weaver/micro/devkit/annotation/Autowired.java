package weaver.micro.devkit.annotation;

import java.lang.annotation.*;

/**
 * Declare that the field or all fields in the class will be autowired.<br>
 * If you dont need to make all fields autowired, dont declare the annotation on the class.<br>
 * <br>
 * This annotation does not take effect on the field which decorated by static or final.
 *
 * @since 1.1.11
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.TYPE})
public @interface Autowired {
}

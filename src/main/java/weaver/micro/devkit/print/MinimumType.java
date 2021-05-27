package weaver.micro.devkit.print;

import java.lang.annotation.*;

/**
 * Print objects using the toString() function.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface MinimumType {
}

package weaver.micro.devkit.print;

import java.lang.annotation.*;

/**
 * The priority of Field takes precedence over the priority of Type.
 * Minimum types will not participate in deduplication.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.FIELD})
public @interface MinimumType {

    /**
     * If it is void, the class of the printed object will be used.
     */
    Class<?> serializationClass() default void.class;

    /**
     * Based on {@link #serializationClass()}, the returned value
     * will be call the method {@link Object#toString()},
     * if returned value is null, output 'null'.
     */
    String serializationMethod() default "toString";

    /**
     * The parameters list of {@link #serializationMethod()}.
     */
    Class<?>[] parametersList() default {};

    /**
     * It represents the position of the print object in the parameter list,
     * it starts at 1, and if it is 0, the printed object will be called,
     * it cannot be greater than the length of {@link #parametersList()}.
     * Others parameter position will be filled with null.
     */
    int callIndex() default 0;

}

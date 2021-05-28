package weaver.micro.devkit.print;

import sun.security.action.GetPropertyAction;
import weaver.micro.devkit.Cast;
import weaver.micro.devkit.util.StringUtils;

import java.security.AccessController;

/**
 * Specify the max depth of 512:<br>
 * java -Dweaver.micro.devkit.print.maxDepth=512
 * <hr/>
 * The problem about {@link StackOverflowError#getStackTrace()}
 * length is 1024, try set {@code -XX:MaxJavaStackTraceDepth},
 * it is a positive integer.
 *
 * @since 1.1.10
 */
public class ObjectDepthOverflowException extends RuntimeException {

    /**
     * From system property {@code weaver.micro.devkit.print.maxDepth},
     * default value is 512.
     */
    public final static int maxDepth;

    static {
        maxDepth = Cast.o2Integer(AccessController.doPrivileged(
                new GetPropertyAction("weaver.micro.devkit.print.maxDepth", "512")),
                512);
    }

    public ObjectDepthOverflowException(String s) {
        super(s);
    }

    public ObjectDepthOverflowException(Throwable cause) {
        super(cause);
    }

    public ObjectDepthOverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectDepthOverflowException(int depth, Throwable cause) {
        super("Current depth: " + depth, cause);
    }

    public ObjectDepthOverflowException(Object root) {
        super("The object [" + StringUtils.toStringNative(root) + "] overflowed max depth: " + maxDepth);
    }

}

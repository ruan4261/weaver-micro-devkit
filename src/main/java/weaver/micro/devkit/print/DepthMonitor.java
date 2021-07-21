package weaver.micro.devkit.print;

import sun.security.action.GetPropertyAction;
import weaver.micro.devkit.Cast;

import java.security.AccessController;

/**
 * Specify the max depth of 512:<br>
 * java -Dweaver.micro.devkit.print.maxDepth=512
 * <hr/>
 * The problem about {@link StackOverflowError#getStackTrace()}
 * length is 1024, try set {@code -XX:MaxJavaStackTraceDepth},
 * it is a positive integer.
 *
 * @since 1.1.14
 */
class DepthMonitor {

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

    private final Object ref;
    private int depth;

    public DepthMonitor(Object ref) {
        this.ref = ref;
    }

    protected int depth() {
        return this.depth;
    }

    protected void increase() {
        if (this.depth++ == maxDepth) {
            throw new ObjectDepthOverflowException(this.ref, this.depth);
        }
    }

    protected void decrease() {
        this.depth--;
    }

    protected void resolveStackOverflow(StackOverflowError e) {
        throw new ObjectDepthOverflowException(this.ref, this.depth, e);
    }

}

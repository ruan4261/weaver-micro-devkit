package weaver.micro.devkit.print;

import weaver.micro.devkit.util.StringUtils;

public class ObjectDepthOverflowException extends RuntimeException {

    public ObjectDepthOverflowException(String s) {
        super(s);
    }

    public ObjectDepthOverflowException(Throwable cause) {
        super(cause);
    }

    public ObjectDepthOverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectDepthOverflowException(Object ref, int depth) {
        super("Object [" + StringUtils.toStringNative(ref) + "] overflows depth: " + depth);
    }

    public ObjectDepthOverflowException(Object ref, int depth, Throwable cause) {
        super("Object [" + StringUtils.toStringNative(ref) + "] overflows depth: " + depth, cause);
    }

}

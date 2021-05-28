package weaver.micro.devkit.print;

public class VisualPrintUtils {

    public static void checkObjectDepth(int depth, Object root) {
        if (depth > ObjectDepthOverflowException.maxDepth) {
            throw new ObjectDepthOverflowException(root);
        }
    }

    public static void checkObjectDepth(int depth, String msg) {
        if (depth > ObjectDepthOverflowException.maxDepth) {
            throw new ObjectDepthOverflowException(msg);
        }
    }

}

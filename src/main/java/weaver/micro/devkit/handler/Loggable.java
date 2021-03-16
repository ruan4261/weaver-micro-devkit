package weaver.micro.devkit.handler;

/**
 * @since 1.1.3
 */
public interface Loggable {

    /**
     * @since 1.1.3
     */
    void log(String mes);

    /**
     * @since 1.1.3
     */
    void log(Throwable throwable);

    /**
     * @since 1.1.3
     */
    void log(String title, Throwable throwable);

}

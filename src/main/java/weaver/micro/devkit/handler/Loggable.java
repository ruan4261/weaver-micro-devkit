package weaver.micro.devkit.handler;

/**
 * @since 1.1.3
 */
public interface Loggable {

    void log(String mes);

    void log(Throwable throwable);

    void log(String title, Throwable throwable);

}

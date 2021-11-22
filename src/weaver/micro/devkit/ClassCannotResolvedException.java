package weaver.micro.devkit;

public class ClassCannotResolvedException extends Exception {

    public ClassCannotResolvedException() {
    }

    public ClassCannotResolvedException(String mes) {
        super(mes);
    }

    public ClassCannotResolvedException(Throwable e) {
        super(e);
    }

    public ClassCannotResolvedException(String mes, Throwable e) {
        super(mes, e);
    }

    public static ClassCannotResolvedException ThrowAutoFill(String clazz) throws ClassCannotResolvedException {
        throw new ClassCannotResolvedException("Class " + clazz + " can not resolved.");
    }

    public static ClassCannotResolvedException ThrowAutoFill(String clazz, Throwable e) throws ClassCannotResolvedException {
        throw new ClassCannotResolvedException("Class " + clazz + " can not resolved.", e);
    }
}

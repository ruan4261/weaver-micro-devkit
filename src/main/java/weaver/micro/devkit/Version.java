package weaver.micro.devkit;

public final class Version {

    private Version() {
    }

    public static String VERSION() {
        return String.valueOf(MAJOR_VERSION) + '.' + MINOR_VERSION + '.' + BUILD_VERSION;
    }

    public final static int MAJOR_VERSION = 1;

    public final static int MINOR_VERSION = 1;

    public final static int BUILD_VERSION = 6;

}

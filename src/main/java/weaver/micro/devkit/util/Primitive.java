package weaver.micro.devkit.util;

/**
 * <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3">
 * Descriptors Grammar Term.
 * </a>
 *
 * @author ruan4261
 */
public enum Primitive {

    BYTE('B', byte.class),
    SHORT('S', short.class),
    INT('I', int.class),
    FLOAT('F', float.class),
    DOUBLE('D', double.class),
    LONG('J', long.class),
    CHAR('C', char.class),
    BOOLEAN('Z', boolean.class);

    final Class<?> primitiveClass;
    final char baseTypeCharacter;

    Primitive(char baseTypeCharacter, Class<?> clazz) {
        this.baseTypeCharacter = baseTypeCharacter;
        this.primitiveClass = clazz;
    }

    /**
     * @param ch must be uppercase
     */
    public static Primitive getPrimitiveByBaseTypeCharacter(char ch) {
        switch (ch) {
            case 'B':
                return BYTE;
            case 'S':
                return SHORT;
            case 'I':
                return INT;
            case 'F':
                return FLOAT;
            case 'D':
                return DOUBLE;
            case 'J':
                return LONG;
            case 'C':
                return CHAR;
            case 'Z':
                return BOOLEAN;
            default:
                return null;
        }
    }

    public static Primitive getPrimitiveByClass(Class<?> clazz) {
        if (byte.class.equals(clazz)) {
            return BYTE;
        } else if (short.class.equals(clazz)) {
            return SHORT;
        } else if (int.class.equals(clazz)) {
            return INT;
        } else if (float.class.equals(clazz)) {
            return FLOAT;
        } else if (double.class.equals(clazz)) {
            return DOUBLE;
        } else if (long.class.equals(clazz)) {
            return LONG;
        } else if (char.class.equals(clazz)) {
            return CHAR;
        } else if (boolean.class.equals(clazz)) {
            return BOOLEAN;
        }
        return null;
    }

    public Class<?> getPrimitiveClass() {
        return this.primitiveClass;
    }

    public char getBaseTypeCharacter() {
        return this.baseTypeCharacter;
    }

}

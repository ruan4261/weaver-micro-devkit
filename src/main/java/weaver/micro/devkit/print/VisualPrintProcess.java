package weaver.micro.devkit.print;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.util.ReflectUtil;
import weaver.micro.devkit.util.StringUtils;

import java.io.Flushable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * <h2>MinimumType case:</h2>
 * 1) a primitive type<br>
 * 2) a wrapper class of primitive type<br>
 * 3) a CharSequence class<br>
 * 4) a Number class<br>
 * MinimumType format is its <code>toString()</code> function.
 *
 * <br><br>
 * <h2>Display format:</h2>
 * <h3>Null</h3>
 * <pre>
 * └── {FieldName} : null
 * </pre>
 *
 * <h3>MinimumType</h3>
 * <pre>
 * └── {FieldName} : [NativeInfo] -> {this.toString()}
 * </pre>
 *
 * <h3>Object</h3>
 * <pre>
 * └── {FieldName} : [NativeInfo]
 *     ├── its attribute A
 *     ├── its attribute B
 *     ├── and more
 *     ├── (RELEVANT SCOPE) Super Class A(may be interface)
 *     │   └── {attributes in instance scope of A}
 *     └── (RELEVANT SCOPE) Super Class B(may be interface)
 *         └── {attributes in instance scope of B}
 * </pre>
 *
 * <h3>Map</h3>
 * <pre>
 * └── {FieldName} : [NativeInfo] >> {size}
 *     ├── (1)[java.util.Map$Entry@xxxx]
 *     │   ├── Key : {print4Internal(key)}
 *     │   └── Value : {print4Internal(value)}
 *     ├── (2)[java.util.Map$Entry@xxxx]
 *     │   ├── Key : {print4Internal(key)}
 *     │   └── Value : {print4Internal(value)}
 *     └── (order number)[NativeInfo of Map.Entry]
 *         ├── Key : {print4Internal(key)}
 *         └── Value : {print4Internal(value)}
 * </pre>
 *
 * <h3>Collection And Array</h3>
 * <pre>
 * └── {FieldName} : [NativeInfo] >> {size}
 *     ├── 1 : {print4Internal(ele))}
 *     ├── 2 : {print4Internal(ele))}
 *     └── order : {print4Internal(ele))}
 * </pre>
 *
 *
 * <h3>special: Repeated objects(may be Object, Map, Collection, Array)</h3>
 * <pre>
 * └── {FieldName} : (REPEATED)[NativeInfo]
 * </pre>
 *
 * <hr/>
 * <h1>Non-thread safe</h1>
 * It can be reused, but cannot be run under multithreading.
 *
 * @author ruan4261
 */
public class VisualPrintProcess {

    //private final static char prefix_scope = '│';
    private final static String prefix_branch = "├──";
    private final static String prefix_last = "└──";
    //private final static char TAB = '\t';
    private final static char BLANK = ' ';
    private final static char LINE_FEED = '\n';

    /* Instance */

    private final Appendable out;
    private final Flushable flushCtrl;
    String dynamicPrefix;// only TAB and prefix_scope

    /* Null and minimum type will not be added to the collections that for deduplication */

    private final Set<Object> dedupSet = new HashSet<Object>();

    public VisualPrintProcess(Writer out) {
        Assert.notNull(out);
        this.out = out;
        this.flushCtrl = out;
    }

    public VisualPrintProcess(PrintStream out) {
        Assert.notNull(out);
        this.out = out;
        this.flushCtrl = out;
    }

    /**
     * Entrance
     */
    public void print(Object o) throws IOException, IllegalAccessException {
        this.dynamicPrefix = "";
        this.printPrefix(true);
        try {
            this.print4Internal(o, true);
        } catch (StackOverflowError e) {
            this.stackoverflow(e);
        }
        this.end();
    }

    private void end() throws IOException {
        this.flushCtrl.flush();
        this.dedupSet.clear();
    }

    private void stackoverflow(Throwable t) {
        throw new IllegalArgumentException(
                "The object depth is too deep to print visually, which causes a stack overflow!",
                t);
    }

    /**
     * Only routing and check repetition.
     * The types that need to check repetition:
     * {@link ObjectType#Obj},
     * {@link ObjectType#Array},
     * {@link ObjectType#Map},
     * {@link ObjectType#Collection}
     */
    private void print4Internal(Object o, boolean isLastItem) throws IOException, IllegalAccessException {
        ObjectType type = ObjectType.whichType(o);

        // check repetition
        boolean needCheckRepetition = type.isNeedCheckRepetition();
        if (needCheckRepetition && this.checkRepeatedObjectAndAdd(o)) {
            this.printRepeatedObject(o);
            return;
        }

        switch (type) {
            case Minimum:
                this.printMinimum(o);
                break;
            case NULL:
                this.printNULL();
                break;
            case Obj:
                this.printObj(o, isLastItem);
                break;
            case Array:
                this.printArray(o, isLastItem);
                break;
            case Map:
                this.printMap((Map<?, ?>) o, isLastItem);
                break;
            case Collection:
                this.printCollection(((Collection<?>) o), isLastItem);
                break;
        }

        // exit dedup collection
        if (needCheckRepetition)
            this.popRepeatedObject(o);
    }

    private void printMinimum(Object o) throws IOException {
        this.printNativeInfo(o);
        this.out.append(" -> \"");
        this.out.append(escape(o.toString()));
        this.out.append('"');
    }

    private void printObj(Object o, boolean isLastItem) throws IOException, IllegalAccessException {
        // [0] is itself, if it not be primitive type, [length - 1] is Object
        Class<?>[] classes = ReflectUtil.getAllSuper(o.getClass());
        Class<?> self = classes[0];
        // exclude java.lang.Object
        int len = classes[classes.length - 1] == Object.class ?
                classes.length - 1 : classes.length;
        // print its own class
        this.printNativeInfo(o);

        // print its own attributes
        Field[] fields = ReflectUtil.queryFields(self, 0, false);
        this.expandDynamicPrefix(isLastItem);
        this.printObjFields(fields, o, len == 1);
        for (int i = 1; i < len; i++) {
            boolean eleIsLastItem = i + 1 == len;
            this.printLineFeed();
            this.printPrefix(eleIsLastItem);
            this.printObjWithSpecifiedScope(classes[i], o, eleIsLastItem);
        }
        this.reduceDynamicPrefix();
    }

    /**
     * Only called by {@link #printObj(Object, boolean)}.
     */
    private void printObjWithSpecifiedScope(Class<?> clazz, Object o, boolean isLastItem)
            throws IOException, IllegalAccessException {
        // print class info
        this.out.append("(RELEVANT SCOPE) ");
        this.out.append(clazz.toString());

        // print fields
        Field[] fields = ReflectUtil.queryFields(clazz, 0, false);
        this.expandDynamicPrefix(isLastItem);
        this.printObjFields(fields, o, isLastItem);
        this.reduceDynamicPrefix();
    }

    private void printObjFields(Field[] fields, Object o, boolean isLastItem) throws IllegalAccessException, IOException {
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (!f.isAccessible())
                f.setAccessible(true);
            Object v = f.get(o);

            // print
            boolean isLastField = isLastItem && i == fields.length - 1;
            this.printLineFeed();
            this.printPrefix(isLastField);
            this.printFieldName(f);
            this.print4Internal(v, isLastField);
        }
    }

    private void printMap(Map<?, ?> m, boolean isLastItem) throws IOException, IllegalAccessException {
        this.printNativeInfo(m);
        int size = m.size();
        this.printSize(size);

        if (size == 0)
            return;

        this.expandDynamicPrefix(isLastItem);
        Set<? extends Map.Entry<?, ?>> entrySet = m.entrySet();
        Iterator<? extends Map.Entry<?, ?>> it = entrySet.iterator();

        int order = 1;
        boolean entryIsLast;
        do {
            Map.Entry<?, ?> entry = it.next();
            entryIsLast = !it.hasNext();
            this.printLineFeed();
            this.printPrefix(entryIsLast);
            this.printMapEntry(entry, entryIsLast, order++);
        } while (!entryIsLast);

        this.reduceDynamicPrefix();
    }

    private void printMapEntry(Map.Entry<?, ?> entry, boolean isLastItem, int order) throws IOException, IllegalAccessException {
        this.out.append('(');
        this.out.append(String.valueOf(order));
        this.out.append(')');
        this.printNativeInfo(entry);
        this.expandDynamicPrefix(isLastItem);
        Object key = entry.getKey();
        Object value = entry.getValue();
        this.printLineFeed();
        this.printPrefix(false);
        this.out.append("Key : ");
        this.print4Internal(key, false);
        this.printLineFeed();
        this.printPrefix(true);
        this.out.append("Value : ");
        this.print4Internal(value, true);
        this.reduceDynamicPrefix();
    }

    private void printCollection(Collection<?> collection, boolean isLastItem) throws IOException, IllegalAccessException {
        this.printNativeInfo(collection);
        int size = collection.size();
        this.printSize(size);

        if (size == 0)
            return;

        this.expandDynamicPrefix(isLastItem);
        Iterator<?> it = collection.iterator();

        int order = 1;
        boolean eleIsLast;
        do {
            Object ele = it.next();
            eleIsLast = !it.hasNext();
            this.printLineFeed();
            this.printPrefix(eleIsLast);
            this.printCollectionOrder(order++);
            this.print4Internal(ele, eleIsLast);
        } while (!eleIsLast);

        this.reduceDynamicPrefix();
    }

    private void printArray(Object o, boolean isLastItem) throws IOException, IllegalAccessException {
        this.printNativeInfo(o);
        int len = Array.getLength(o);
        this.printSize(len);

        if (len == 0)
            return;

        this.expandDynamicPrefix(isLastItem);
        for (int i = 0; i < len; i++) {
            Object ele = Array.get(o, i);
            boolean eleIsLastItem = i + 1 == len;
            this.printLineFeed();
            this.printPrefix(eleIsLastItem);
            this.printCollectionOrder(i + 1);
            this.print4Internal(ele, eleIsLastItem);
        }
        this.reduceDynamicPrefix();
    }

    private void printNULL() throws IOException {
        this.out.append("null");
    }

    private void printLineFeed() throws IOException {
        this.out.append(LINE_FEED);
    }

    private void printPrefix(boolean isLastItem) throws IOException {
        this.out.append(this.dynamicPrefix);
        this.out.append(isLastItem ? prefix_last : prefix_branch);
        this.out.append(BLANK);
    }

    private void printFieldName(Field field) throws IOException {
        this.out.append(field.getName());
        this.out.append(" : ");
    }

    /**
     * It should called after {@link #checkRepeatedObjectAndAdd(Object)} returns true.
     */
    private void printRepeatedObject(Object o) throws IOException {
        this.out.append("(REPEAT)");
        this.printNativeInfo(o);
    }

    private void printNativeInfo(Object o) throws IOException {
        this.out.append('[');
        this.out.append(StringUtils.toStringNative(o));
        this.out.append(']');
    }

    private void printSize(int size) throws IOException {
        this.out.append(" >> ");
        this.out.append(String.valueOf(size));
    }

    private void printCollectionOrder(int order) throws IOException {
        this.out.append(String.valueOf(order));
        this.out.append(" : ");
    }

    private void expandDynamicPrefix(boolean isLastItem) {
        this.dynamicPrefix += isLastItem ? "    " : "│   ";
    }

    /**
     * Release {@link #expandDynamicPrefix(boolean)}.
     */
    private void reduceDynamicPrefix() {
        int len = this.dynamicPrefix.length();
        if (len < 5) {
            this.dynamicPrefix = "";
        } else {
            this.dynamicPrefix = this.dynamicPrefix.substring(0, len - 4);
        }
    }

    private boolean checkRepeatedObjectAndAdd(Object o) {
        if (o == null)
            return false;

        if (this.dedupSet.contains(o))
            return true;

        this.dedupSet.add(o);
        return false;// next times returns true
    }

    private void popRepeatedObject(Object o) {
        this.dedupSet.remove(o);
    }

    private static String escape(String str) {
        return StringUtils.escapeString(str);
    }

}

package weaver.micro.devkit.print;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.annotation.NotNull;
import weaver.micro.devkit.util.ArrayIterator;
import weaver.micro.devkit.util.ReflectUtils;
import weaver.micro.devkit.util.StringUtils;

import java.io.Flushable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * <h2>Default MinimumType case:</h2>
 * 1) A primitive type<br>
 * 2) A wrapper class of primitive type<br>
 * 3) CharSequence type<br>
 * 4) Number type<br>
 * 5) An object which type is Class<br>
 * 6) An enum<br>
 * 7) An annotation<br>
 * MinimumType is formatted using is its <code>toString()</code> function by default.
 *
 * <br><br>
 * <h2>Display format: (Order by priority)</h2>
 * <h3>Null</h3>
 * <pre>
 * └── {FieldName} : null
 * </pre>
 *
 * <h3>MinimumType With Annotation</h3>
 * <pre>
 * └── {FieldName} : [NativeInfo] @-> {custom serialization function}
 * </pre>
 *
 * <h3>MinimumType With Modal</h3>
 * <pre>
 * └── {FieldName} : [NativeInfo] @-> {custom serialization function}
 * </pre>
 *
 * <h3>MinimumType(Default)</h3>
 * <pre>
 * └── {FieldName} : [NativeInfo] -> {this.toString()}
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
 * <h3>special: Repeated objects(may be Object, Map, Collection, Array, etc.)</h3>
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
    private DepthMonitor depthMonitor;
    private String dynamicPrefix;// only TAB and prefix_scope

    /* Null and minimum type will not be added to the collections that for deduplication */

    private final Set<Object> dejavu = new HashSet<Object>();

    /**
     * If true, any object will only be printed once at most,
     * otherwise, any object will not be printed repeatedly
     * within the subtree of its attributes.
     *
     * <hr/>
     * Getter/Setter see:
     * {@link #setGlobalDedup(boolean)},
     * {@link #isGlobalDedup()}
     */
    private boolean globalDedup = true;

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
     * Preset set of minimum type.
     */
    private MinimumTypeModel minimumTypeModel;

    public MinimumTypeModel getMinimumTypeModel() {
        return this.minimumTypeModel;
    }

    public void setMinimumTypeModel(MinimumTypeModel minimumTypeModel) {
        this.minimumTypeModel = minimumTypeModel;
    }

    public void removeMinimumTypeModel() {
        this.minimumTypeModel = null;
    }

    /**
     * Entrance
     */
    public void print(Object o) throws IOException {
        this.dynamicPrefix = "";
        this.depthMonitor = new DepthMonitor(o);
        this.printPrefix(true);
        try {
            this.print4Internal(o, true, null);
        } catch (StackOverflowError e) {
            this.resolveStackoverflow(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            this.end();
        }
    }

    private void end() throws IOException {
        this.depthMonitor = null;
        this.dejavu.clear();
        this.flushCtrl.flush();
    }

    private void resolveStackoverflow(StackOverflowError t) {
        this.depthMonitor.resolveStackOverflow(t);
    }

    /**
     * Only routing and check repetition.
     * The types that need to check repetition:
     * {@link ObjectType#Obj},
     * {@link ObjectType#Array},
     * {@link ObjectType#Map},
     * {@link ObjectType#Collection}
     *
     * @param o          printed object
     * @param isLastItem used to construct prefix string
     * @param field      has value if the object is a field in another object
     */
    private void print4Internal(Object o, boolean isLastItem, Field field)
            throws IOException, IllegalAccessException, InvocationTargetException {
        ObjectType type = ObjectType.whichType(o, field, this.minimumTypeModel);

        // check repetition
        boolean needCheckRepetition = type.isNeedCheckRepetition();
        if (needCheckRepetition && this.checkRepeatedObjectAndAdd(o)) {
            this.printRepeatedObject(o);
            return;
        }

        this.depthMonitor.increase();
        switch (type) {
            case NULL:
                this.printNULL();
                break;
            case MinimumWithAnnotation:
                // the annotation instance must be from class of the object
                this.printMinimumWithAnnotation(o, field);
                break;
            case MinimumWithModel:
                this.printMinimumWithModal(o, this.minimumTypeModel);
                break;
            case Minimum:
                this.printMinimum(o);
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
        this.depthMonitor.decrease();

        // exit dedup collection
        if (needCheckRepetition && !this.globalDedup)
            this.popRepeatedObject(o);
    }

    private void printMinimum(Object o)
            throws IOException {
        this.printNativeInfo(o);
        this.out.append(" -> ");
        this.out.append(escape(o.toString()));
    }

    private void printMinimumWithAnnotation(Object o, Field f)
            throws IOException, InvocationTargetException, IllegalAccessException {
        MinimumType type = null;
        if (f != null) {
            type = f.getAnnotation(MinimumType.class);
        }
        if (type == null) {
            type = o.getClass().getAnnotation(MinimumType.class);
        }

        this.printMinimumWithAnnotation(o, type);
    }

    private void printMinimumWithAnnotation(Object o, @NotNull MinimumType type)
            throws IOException, InvocationTargetException, IllegalAccessException {
        // get serialization method
        Method calledMethod;
        try {
            calledMethod = VPUtils.getMethod(type, o);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
        calledMethod.setAccessible(true);

        // construct parameters list
        int paramsLen = type.parametersList().length;
        int callIndex = type.callIndex();
        Object returnedValue;
        Object called;
        Object[] params = new Object[paramsLen];
        if (callIndex == 0) {
            // called itself
            called = o;
        } else {
            called = null;
            params[callIndex - 1] = o;
        }

        // serialize
        returnedValue = calledMethod.invoke(called, params);
        String output = returnedValue == null ? "null" : returnedValue.toString();

        this.printNativeInfo(o);
        this.out.append(" @-> ");
        this.out.append(escape(output));
    }

    private void printMinimumWithModal(Object o, MinimumTypeModel model)
            throws IllegalAccessException, IOException, InvocationTargetException {
        MinimumType type = Assert.notNull(model.get(o.getClass()));
        this.printMinimumWithAnnotation(o, type);
    }

    private void printObj(Object o, boolean isLastItem)
            throws IOException, IllegalAccessException, InvocationTargetException {
        // [0] is itself, if it not be primitive type, [length - 1] is Object
        Class<?>[] classes = ReflectUtils.getAllSuper(o.getClass());
        Class<?> self = classes[0];
        // exclude java.lang.Object
        int len = classes[classes.length - 1] == Object.class ?
                classes.length - 1 : classes.length;
        // print its own class
        this.printNativeInfo(o);

        // attributes
        Field[] fields = ReflectUtils.queryFields(self, 0, false);

        if (fields.length == 0 && len == 1)
            return;

        this.expandDynamicPrefix(isLastItem);
        // print its own attributes
        this.printObjFields(fields, o, len == 1);

        // print relevant class scope
        this.depthMonitor.increase();
        for (int i = 1; i < len; i++) {
            boolean eleIsLastItem = i + 1 == len;
            this.printLineFeed();
            this.printPrefix(eleIsLastItem);
            this.printObjWithSpecifiedScope(classes[i], o, eleIsLastItem);
        }
        this.depthMonitor.decrease();
        this.reduceDynamicPrefix();
    }

    /**
     * Only called by {@link #printObj(Object, boolean)}.
     */
    private void printObjWithSpecifiedScope(Class<?> clazz, Object o, boolean isLastItem)
            throws IOException, IllegalAccessException, InvocationTargetException {
        // print class info
        this.out.append("(RELEVANT SCOPE) ");
        this.out.append(clazz.toString());

        // print fields
        Field[] fields = ReflectUtils.queryFields(clazz, 0, false);
        this.expandDynamicPrefix(isLastItem);
        this.printObjFields(fields, o, true);
        this.reduceDynamicPrefix();
    }

    private void printObjFields(Field[] fields, Object o, boolean tailClose)
            throws IllegalAccessException, IOException, InvocationTargetException {
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!field.isAccessible())
                field.setAccessible(true);
            Object v = field.get(o);

            // print
            boolean isLastField = tailClose && i == fields.length - 1;
            this.printLineFeed();
            this.printPrefix(isLastField);
            this.printFieldName(field);
            this.print4Internal(v, isLastField, field);
        }
    }

    private void printMap(Map<?, ?> m, boolean isLastItem)
            throws IOException, IllegalAccessException, InvocationTargetException {
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

    private void printMapEntry(Map.Entry<?, ?> entry, boolean isLastItem, int order)
            throws IOException, IllegalAccessException, InvocationTargetException {
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
        this.print4Internal(key, false, null);
        this.printLineFeed();
        this.printPrefix(true);
        this.out.append("Value : ");
        this.print4Internal(value, true, null);
        this.reduceDynamicPrefix();
    }

    private void printCollection(Collection<?> collection, boolean isLastItem)
            throws IOException, IllegalAccessException, InvocationTargetException {
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
            this.print4Internal(ele, eleIsLast, null);
        } while (!eleIsLast);

        this.reduceDynamicPrefix();
    }

    private void printArray(Object o, boolean isLastItem)
            throws IOException, IllegalAccessException, InvocationTargetException {
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
            this.print4Internal(ele, eleIsLastItem, null);
        }
        this.reduceDynamicPrefix();
    }

    @Deprecated
    private void printArrayOfPrimitiveType(Object o) throws IOException {
        // it is impossible that primitive type element may be null
        String arrayBody = StringUtils.toString(ArrayIterator.of(o));
        this.printNativeInfo(o);
        int len = Array.getLength(o);
        this.printSize(len);

        this.out.append(" -> ");
        this.out.append('[');
        this.out.append(arrayBody);
        this.out.append(']');
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
        this.out.append("(REPEATED)");
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

        if (this.dejavu.contains(o))
            return true;

        this.dejavu.add(o);
        return false;// next times returns true
    }

    private void popRepeatedObject(Object o) {
        this.dejavu.remove(o);
    }

    public boolean isGlobalDedup() {
        return this.globalDedup;
    }

    public void setGlobalDedup(boolean globalDedup) {
        this.globalDedup = globalDedup;
    }

    private static String escape(String str) {
        return StringUtils.escapeString(str);
    }

}

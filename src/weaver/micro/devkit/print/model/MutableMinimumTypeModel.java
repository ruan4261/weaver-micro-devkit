package weaver.micro.devkit.print.model;

import weaver.micro.devkit.print.MinimumType;
import weaver.micro.devkit.print.MinimumTypeModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class MutableMinimumTypeModel implements MinimumTypeModel {

    private final ConcurrentHashMap<Class<?>, MinimumType> collection;

    protected MutableMinimumTypeModel() {
        this.collection = new ConcurrentHashMap<Class<?>, MinimumType>();
    }

    protected MutableMinimumTypeModel(Map<Class<?>, MinimumType> preset) {
        this.collection = new ConcurrentHashMap<Class<?>, MinimumType>(preset);
    }

    /**
     * It indicates that the collection will never be used,
     * the methods that declared by this class should be overrided.
     */
    protected MutableMinimumTypeModel(Void ignored) {
        this.collection = null;
    }

    public MinimumType put(Class<?> type, MinimumType minimumType) {
        return this.collection.put(type, minimumType);
    }

    public void putAll(Map<Class<?>, MinimumType> mapping) {
        this.collection.putAll(mapping);
    }

    @Override
    public boolean isMinimumType(Class<?> type) {
        return this.collection.get(type) != null;
    }

    @Override
    public MinimumType get(Class<?> type) {
        return this.collection.get(type);
    }

    public int size() {
        return this.collection.size();
    }

    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    public MinimumType remove(Class<?> type) {
        return this.collection.remove(type);
    }

    public void clear() {
        this.collection.clear();
    }

    /* External static API */

    public static MutableMinimumTypeModel ofEmpty() {
        return new MutableMinimumTypeModel();
    }

    public static MutableMinimumTypeModel ofCustom(Map<Class<?>, MinimumType> custom) {
        return new MutableMinimumTypeModel(custom);
    }

}
package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.io.Serializable;
import java.util.*;

/**
 * @author ruan4261
 */
public final class Collections {

    private Collections() {
    }

    /**
     * 这是一个特殊类型的Map，请勿向下转型<hr>
     * 调用get等同于调用getOrDefault，其默认值在类实例化时设置
     * getOrDefault仍可以自定义使用
     * 您可以使用反射调用类的{@code setDefault(Object)}方法修改默认值
     */
    public static <K, V> Map<K, V> defaultValueMap(Map<K, V> m, V defaultVal) {
        return new NotNullMap<K, V>(m, defaultVal);
    }

    /**
     * @since 1.1.1
     */
    public static <K, V> Map<K, V> immutableMap(Map<K, V> m) {
        return new ImmutableMap<K, V>(m);
    }

    /**
     * @since 1.1.2
     */
    public static <K, V> Map<K, V> immutableMap(MapConstructor<K, V> constructor) {
        return new ImmutableMap<K, V>(constructor);
    }

    /**
     * @since 1.1.1
     */
    public static <E> Set<E> immutableSet(Set<E> set) {
        return new ImmutableSet<E>(set);
    }

    /**
     * @since 1.1.1
     */
    public static <E> Collection<E> immutableCollection(Collection<E> collection) {
        return new ImmutableCollection<E>(collection);
    }

    /**
     * @since 1.1.1
     */
    public static <E> Iterator<E> immutableIterator(Iterator<E> iterator) {
        return new ImmutableIterator<E>(iterator);
    }

    /**
     * 将get替换为getOrDefault
     */
    private static class NotNullMap<K, V> implements Map<K, V>, Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<K, V> m;
        private V defaultVal;

        NotNullMap(Map<K, V> m, V defaultVal) {
            Assert.notNull(m);
            Assert.notNull(defaultVal);
            this.m = m;
            this.defaultVal = defaultVal;
        }

        @SuppressWarnings("unchecked")
        public void setDefault(Object val) {
            this.defaultVal = (V) val;
        }

        @Override
        public int size() {
            return this.m.size();
        }

        @Override
        public boolean isEmpty() {
            return this.m.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return this.m.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.m.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return this.getOrDefault(key, this.defaultVal);
        }

        public V getOrDefault(Object key, V defaultValue) {
            V v;
            return (((v = this.m.get(key)) != null) || containsKey(key))
                    ? v
                    : defaultValue;
        }

        @Override
        public V put(K key, V value) {
            return this.m.put(key, value);
        }

        @Override
        public V remove(Object key) {
            return this.m.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            this.m.putAll(m);
        }

        @Override
        public void clear() {
            this.m.clear();
        }

        @Override
        public Set<K> keySet() {
            return this.m.keySet();
        }

        @Override
        public Collection<V> values() {
            return this.m.values();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return this.m.entrySet();
        }

    }

    /**
     * 禁止修改内容的映射表
     *
     * @since 1.1.1
     */
    private static class ImmutableMap<K, V> implements Map<K, V> {
        private final Map<K, V> m;

        /**
         * @since 1.1.2
         */
        private final Ref<Map<K, V>> ref;

        /**
         * @since 1.1.1
         */
        public ImmutableMap(Map<K, V> m) {
            this.m = m;
            this.ref = new Ref<Map<K, V>>();
        }

        /**
         * @since 1.1.2
         */
        public ImmutableMap(MapConstructor<K, V> constructor) {
            this.m = new HashMap<K, V>();
            this.ref = new Ref<Map<K, V>>(this.m);

            class ImmutableMapConstructionFiller implements MapFiller<K, V> {

                @Override
                public void put(K key, V value) {
                    ref.get().put(key, value);
                }

            }

            MapFiller<K, V> filler = new ImmutableMapConstructionFiller();

            constructor.construct(filler);
            this.ref.remove();
        }

        @Override
        public int size() {
            return this.m.size();
        }

        @Override
        public boolean isEmpty() {
            return this.m.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return this.m.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.m.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return this.m.get(key);
        }

        @Override
        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<K> keySet() {
            return immutableSet(this.m.keySet());
        }

        @Override
        public Collection<V> values() {
            return immutableCollection(this.m.values());
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            Set<Entry<K, V>> ori = this.m.entrySet();
            Set<Entry<K, V>> dest = new HashSet<Entry<K, V>>();
            for (Entry<K, V> entry : ori) {
                dest.add(new ImmutableEntry<K, V>(entry));
            }
            return immutableSet(dest);
        }

        public V getOrDefault(Object key, V defaultValue) {
            V v;
            return (((v = get(key)) != null) || containsKey(key))
                    ? v
                    : defaultValue;
        }

        public V putIfAbsent(K key, V value) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }

        public V replace(K key, V value) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * @since 1.1.2
     */
    private static class Ref<T> {
        private T ref;

        public Ref() {
        }

        public Ref(T obj) {
            this.ref = obj;
        }

        public T get() {
            if (this.ref == null)
                throw new UnsupportedOperationException();

            return this.ref;
        }

        public void set(T obj) {
            this.ref = obj;
        }

        public void remove() {
            this.ref = null;
        }

    }

    /**
     * @since 1.1.2
     */
    private static class ImmutableEntry<K, V> implements Map.Entry<K, V> {
        private final Map.Entry<K, V> entry;

        public ImmutableEntry(Map.Entry<K, V> entry) {
            this.entry = entry;
        }

        @Override
        public K getKey() {
            return this.entry.getKey();
        }

        @Override
        public V getValue() {
            return this.entry.getValue();
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * @since 1.1.2
     */
    public interface MapFiller<K, V> {

        void put(K key, V value);

    }

    /**
     * @since 1.1.2
     */
    public interface MapConstructor<K, V> {

        void construct(MapFiller<K, V> filler);

    }

    /**
     * @since 1.1.1
     */
    private static class ImmutableSet<E> extends ImmutableCollection<E> implements Set<E> {

        public ImmutableSet(Collection<E> collection) {
            super(collection);
        }

    }

    /**
     * @since 1.1.1
     */
    private static class ImmutableCollection<E> implements Collection<E> {
        private final Collection<E> collection;

        public ImmutableCollection(Collection<E> collection) {
            this.collection = collection;
        }

        @Override
        public int size() {
            return this.collection.size();
        }

        @Override
        public boolean isEmpty() {
            return this.collection.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return this.collection.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return immutableIterator(this.collection.iterator());
        }

        @Override
        public Object[] toArray() {
            return this.collection.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return this.collection.toArray(a);
        }

        @Override
        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return this.collection.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * @since 1.1.1
     */
    private static class ImmutableIterator<E> implements Iterator<E> {
        private final Iterator<E> iterator;

        public ImmutableIterator(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public E next() {
            return this.iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}

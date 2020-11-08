package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author ruan4261
 */
public class Collections {

    private Collections() {
    }

    /**
     * 这是一个特殊类型的Map，请勿向下转型
     * 调用get等同于调用getOrDefault，默认值在类实例化时设置
     * getOrDefault仍可以使用
     * 您可以使用反射调用类的{@code setDefault(Object)}方法修改默认值
     */
    public static <K, V> Map<K, V> defaultValueMap(Map<K, V> m, V defaultVal) {
        return new NotNullMap<K, V>(m, defaultVal);
    }

    private static final class NotNullMap<K, V> implements Map<K, V>, Serializable {
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
}

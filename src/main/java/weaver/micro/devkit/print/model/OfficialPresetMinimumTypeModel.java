package weaver.micro.devkit.print.model;

import weaver.micro.devkit.print.MinimumType;
import weaver.micro.devkit.print.MinimumTypeBuilder;
import weaver.micro.devkit.util.Collections;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public final class OfficialPresetMinimumTypeModel extends MutableMinimumTypeModel {

    private final boolean immutable;

    private OfficialPresetMinimumTypeModel(boolean immutable) {
        super(defaultSettings);
        this.immutable = immutable;
    }

    public static final Map<Class<?>, MinimumType> defaultSettings = Collections.immutableMap(
            new Collections.MapConstructor<Class<?>, MinimumType>() {

                @Override
                public void construct(Collections.MapFiller<Class<?>, MinimumType> filler) {
                    MinimumTypeBuilder builder = MinimumTypeBuilder.of();
                    MinimumType defaultSettings = builder.buildByDefault();

                    // thread, its extended class will not extend this setting
                    filler.put(Thread.class, defaultSettings);

                    // reflection
                    //filler.put(Class.class, defaultSettings);
                    filler.put(Field.class, defaultSettings);
                    filler.put(Method.class, defaultSettings);
                    filler.put(Constructor.class, defaultSettings);

                    // java reference
                    MinimumType used2ref = builder.setSerializationClass(OfficialPresetMinimumTypeModel.class)
                            .setSerializationMethod("reference2string")
                            .setParametersList(new Class[]{Reference.class})
                            .setCallIndex(1)
                            .build();
                    filler.put(WeakReference.class, used2ref);
                    filler.put(SoftReference.class, used2ref);
                    filler.put(PhantomReference.class, used2ref);

                    // One-dimensional array
                    builder.setSerializationClass(Arrays.class)
                            .setSerializationMethod("toString")
                            .setCallIndex(1);
                    filler.put(int[].class,
                            builder.setParametersList(new Class[]{int[].class})
                                    .build());
                    filler.put(double[].class,
                            builder.setParametersList(new Class[]{double[].class})
                                    .build());
                    filler.put(float[].class,
                            builder.setParametersList(new Class[]{float[].class})
                                    .build());
                    filler.put(byte[].class,
                            builder.setParametersList(new Class[]{byte[].class})
                                    .build());
                    filler.put(char[].class,
                            builder.setParametersList(new Class[]{char[].class})
                                    .build());
                    filler.put(boolean[].class,
                            builder.setParametersList(new Class[]{boolean[].class})
                                    .build());
                    filler.put(short[].class,
                            builder.setParametersList(new Class[]{short[].class})
                                    .build());
                    filler.put(long[].class,
                            builder.setParametersList(new Class[]{long[].class})
                                    .build());
                }

            });

    /* Preset serialization methods */

    public static <T> T reference2string(Reference<T> ref) {
        return ref.get();
    }

    public static OfficialPresetMinimumTypeModel mutable() {
        return new OfficialPresetMinimumTypeModel(false);
    }

    public static OfficialPresetMinimumTypeModel immutable() {
        return new OfficialPresetMinimumTypeModel(true);
    }

    public boolean isImmutable() {
        return this.immutable;
    }

    public void checkWhetherMutable() {
        if (this.immutable)
            throw new UnsupportedOperationException();
    }

    @Override
    public MinimumType put(Class<?> type, MinimumType minimumType) {
        this.checkWhetherMutable();
        return super.put(type, minimumType);
    }

    @Override
    public void putAll(Map<Class<?>, MinimumType> mapping) {
        this.checkWhetherMutable();
        super.putAll(mapping);
    }

    @Override
    public MinimumType remove(Class<?> type) {
        this.checkWhetherMutable();
        return super.remove(type);
    }

    @Override
    public void clear() {
        this.checkWhetherMutable();
        super.clear();
    }

}

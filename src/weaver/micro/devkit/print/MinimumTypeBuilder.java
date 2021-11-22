package weaver.micro.devkit.print;

import weaver.micro.devkit.util.AnnotationUtils;

import java.util.HashMap;
import java.util.Map;

public class MinimumTypeBuilder {

    private MinimumTypeBuilder() {
    }

    public static MinimumTypeBuilder of() {
        return new MinimumTypeBuilder();
    }

    private final Map<String, Object> memberValues = new HashMap<String, Object>(8);

    public MinimumTypeBuilder setDefault() {
        MinimumType defaultSettings = this.buildByDefault();
        return this.setSerializationClass(defaultSettings.serializationClass())
                .setSerializationMethod(defaultSettings.serializationMethod())
                .setParametersList(defaultSettings.parametersList())
                .setCallIndex(defaultSettings.callIndex());
    }

    public MinimumTypeBuilder setSerializationClass(Class<?> type) {
        this.memberValues.put("serializationClass", type);
        return this;
    }

    public MinimumTypeBuilder setSerializationMethod(String method) {
        this.memberValues.put("serializationMethod", method);
        return this;
    }

    public MinimumTypeBuilder setParametersList(Class<?>[] parameterList) {
        this.memberValues.put("parametersList", parameterList);
        return this;
    }

    public MinimumTypeBuilder setCallIndex(int callIndex) {
        this.memberValues.put("callIndex", callIndex);
        return this;
    }

    public MinimumType build() {
        return AnnotationUtils.getAnnotationInstance(MinimumType.class, this.memberValues);
    }

    public MinimumType buildByDefault() {
        return AnnotationUtils.getAnnotationInstance(MinimumType.class);
    }

}

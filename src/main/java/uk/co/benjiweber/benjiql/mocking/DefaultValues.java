package uk.co.benjiweber.benjiql.mocking;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class DefaultValues {

    private static final Map<Class<?>, Object> defaultValues = ImmutableMap.<Class<?>,Object>builder()
        .put(String.class, "string")
        .put(Integer.class,0)
        .put(Float.class, 0f)
        .put(Double.class, 0d)
        .put(Long.class, 0L)
        .put(Character.class, 'c')
        .put(Byte.class, (byte)0)
        .put(int.class, 0)
        .put(float.class,0f)
        .put(double.class,0d)
        .put(long.class, 0L)
        .put(char.class, 'c')
        .put(byte.class, (byte)0)
        .build();

    @SuppressWarnings("unchecked")
    public static <T> T getDefault(Class<T> cls) {
        return (T) defaultValues.get(cls);
    }
}

package uk.co.benjiweber.benjiql.mocking;

import java.util.Map;

import static java.util.Map.entry;

public class DefaultValues {

    private static final Map<Class<?>, Object> defaultValues = Map.ofEntries(
        entry(String.class, "string"),
        entry(Integer.class,0),
        entry(Float.class, 0f),
        entry(Double.class, 0d),
        entry(Long.class, 0L),
        entry(Character.class, 'c'),
        entry(Byte.class, (byte)0),
        entry(int.class, 0),
        entry(float.class,0f),
        entry(double.class,0d),
        entry(long.class, 0L),
        entry(char.class, 'c'),
        entry(byte.class, (byte)0)
    );

    @SuppressWarnings("unchecked")
    public static <T> T getDefault(Class<T> cls) {
        return (T) defaultValues.get(cls);
    }
}

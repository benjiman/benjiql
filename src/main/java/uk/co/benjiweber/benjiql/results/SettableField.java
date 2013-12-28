package uk.co.benjiweber.benjiql.results;

import java.util.function.BiConsumer;

public class SettableField<T,U> {
    public final String fieldName;
    public final BiConsumer<T,U> setter;

    public SettableField(String fieldName, BiConsumer<T,U> setter) {
        this.fieldName = fieldName;
        this.setter = setter;
    }
}

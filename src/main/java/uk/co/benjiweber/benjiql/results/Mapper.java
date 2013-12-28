package uk.co.benjiweber.benjiql.results;

import uk.co.benjiweber.benjiql.util.Conventions;
import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Mapper<T> {
    private final T t;
    private final Set<SettableField> properties = new HashSet<>();
    private final Recorder<T> recorder;

    public Mapper(Supplier<T> factory) {
        this.t = factory.get();
        recorder = (Recorder<T>)RecordingObject.create(t.getClass());
    }

    public static <T> Mapper<T> mapper(Supplier<T> factory) {
        return new Mapper<T>(factory);
    }

    public <U extends Serializable> Mapper<T> set(BiConsumer<T,U> setter) {
        this.properties.add(new SettableField<T,U>(getName(setter), setter));
        return this;
    }

    private <U extends Serializable> String getName(BiConsumer<T, U> setter) {
        setter.accept(recorder.getObject(), null);
        return Conventions.toDbName(recorder.getCurrentPropertyName());
    }

    public T map(ResultSet resultSet) {
        properties.forEach(property -> {
            try {
                property.setter.accept(t, resultSet.getObject(property.fieldName));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return t;
    }


}

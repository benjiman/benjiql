package uk.co.benjiweber.benjiql.results;

import uk.co.benjiweber.benjiql.util.Conventions;
import uk.co.benjiweber.benjiql.util.Exceptions;

import java.io.Serializable;
import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.co.benjiweber.benjiql.util.Exceptions.unchecked;

public class RecordMapper<T extends Record> implements Mapper<T> {
    private final List<RecordComponent> properties;
    private final Class<T> recordType;

    public RecordMapper(Class<T> recordType) {
        this.recordType = recordType;
        this.properties = List.of(recordType.getRecordComponents());
    }

    public T map(ResultSet resultSet) {
        Set<Map.Entry<?,?>> constructorParams = new HashSet<>();

        Object[] propertyValues = properties.stream()
                .map(property -> unchecked(
                        () -> resultSet.getObject(
                                Conventions.toDbName(property.getName()))
                    )
                )
                .toArray();
        Class<?>[] propertyTypes = properties.stream().map(property -> property.getType()).collect(Collectors.toList()).toArray(new Class<?>[0]);

        return Exceptions.unchecked(() -> recordType.getConstructor(propertyTypes).newInstance(propertyValues));
    }


}

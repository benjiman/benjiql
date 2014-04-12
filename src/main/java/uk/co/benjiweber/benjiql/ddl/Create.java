package uk.co.benjiweber.benjiql.ddl;

import com.google.common.base.Joiner;
import uk.co.benjiweber.benjiql.util.Conventions;
import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Create<T> {
    private final Class<T> cls;

    private final Set<FieldNameType> fieldNames = new LinkedHashSet<>();
    private final Recorder<T> recorder;

    public Create(Class<T> cls) {
        this.cls = cls;
        this.recorder = RecordingObject.create(cls);
    }

    public static <T> Create<T> create(Class<T> cls) {
        return new Create<T>(cls);
    }

    public static <T,U> JoinTables<T,U> join(Class<T> leftTable, Class<U> rightTable) {
        return new JoinTables<T, U>(leftTable, rightTable);
    }


    public static <T,U> CreateJoin<T,U> create(JoinTables<T,U> joinTables) {
        return new CreateJoin<T, U>(joinTables.leftTable, joinTables.rightTable);
    }

    public <U extends Serializable> Create<T> field(Function<T,U> getter) {
        U result = getter.apply(recorder.getObject());
        String fieldName = recorder.getCurrentPropertyName();
        fieldNames.add(new FieldNameType(fieldName, result.getClass()));
        return this;
    }

    public String toSql() {
        return "CREATE TABLE IF NOT EXISTS " + Conventions.toDbName(cls.getSimpleName()) + " ( " +
                Joiner.on(", ").join(fieldNames.stream().map(FieldNameType::toString).collect(Collectors.toList()))
                        + " ); ";
    }

    public void execute(Supplier<Connection> connectionFactory) throws SQLException {
        try(Connection connection = connectionFactory.get()) {
            connection.prepareStatement(toSql()).executeUpdate();
        }
    }

    static class FieldNameType<T> {
        String fieldName;
        Class<T> type;

        FieldNameType(String fieldName, Class<T> type) {
            this.fieldName = fieldName;
            this.type = type;
        }

        @Override public String toString() {
            return Conventions.toDbName(fieldName) + " " + Conventions.toDbType(type);
        }
    }
}

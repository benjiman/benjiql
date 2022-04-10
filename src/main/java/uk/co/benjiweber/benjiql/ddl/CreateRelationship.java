package uk.co.benjiweber.benjiql.ddl;

import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.ddl.Create.FieldNameType;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;
import uk.co.benjiweber.benjiql.util.Conventions;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateRelationship<T,U> {
    private final Class<T> left;
    private final Class<U> right;

    private final Set<FieldNameType> leftFieldNames = new LinkedHashSet<>();
    private final Set<FieldNameType> rightFieldNames = new LinkedHashSet<>();
    private final Recorder<T> leftRecorder;
    private final Recorder<U> rightRecorder;

    public CreateRelationship(Class<T> leftCls, Class<U> rightCls) {
        this.left = leftCls;
        this.right = rightCls;
        this.leftRecorder = RecordingObject.create(leftCls);
        this.rightRecorder = RecordingObject.create(rightCls);
    }

    public static <T> Create<T> create(Class<T> cls) {
        return new Create<T>(cls);
    }

    public <V extends Serializable> CreateRelationship<T,U> fieldLeft(Function<T,V> getter) {
        V result = getter.apply(leftRecorder.getObject());
        String fieldName = leftRecorder.getCurrentPropertyName();
        leftFieldNames.add(new FieldNameType(Conventions.toDbName(left.getSimpleName()) + "_" + fieldName, result.getClass()));
        return this;
    }

    public <V extends Serializable> CreateRelationship<T,U> fieldRight(Function<U,V> getter) {
        V result = getter.apply(rightRecorder.getObject());
        String fieldName = rightRecorder.getCurrentPropertyName();
        rightFieldNames.add(new FieldNameType(Conventions.toDbName(right.getSimpleName()) + "_" + fieldName, result.getClass()));
        return this;
    }

    public String toSql() {
        return "CREATE TABLE IF NOT EXISTS " + Conventions.toDbName(left.getSimpleName() + right.getSimpleName())  + " ( " +
                Stream.concat(leftFieldNames.stream(), rightFieldNames.stream()).map(FieldNameType::toString).collect(Collectors.joining(", ")) +
                " ); ";
    }

    public void execute(Supplier<Connection> connectionFactory) throws SQLException {
        try(Connection connection = connectionFactory.get()) {
            connection.prepareStatement(toSql()).executeUpdate();
        }
    }


}

package uk.co.benjiweber.benjiql.update;

import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;
import uk.co.benjiweber.benjiql.util.Conventions;
import uk.co.benjiweber.benjiql.util.ExecutionException;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InsertRelationship<T,U>  {
    private final T leftValue;
    private final U rightValue;

    final Recorder<T> leftRecorder;
    final Recorder<U> rightRecorder;
    final List<FieldNameValue> leftFieldNames = new ArrayList<>();
    final List<FieldNameValue> rightFieldNames = new ArrayList<>();

    public InsertRelationship(T leftValue, U rightValue) {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.leftRecorder = (Recorder<T>) RecordingObject.create(leftValue.getClass());
        this.rightRecorder = (Recorder<U>) RecordingObject.create(rightValue.getClass());
    }

    public <V extends Serializable> InsertRelationship<T,U> valueLeft(Function<T,V> getter) {
        V result = getter.apply(leftRecorder.getObject());
        String fieldName = leftRecorder.getCurrentPropertyName();
        leftFieldNames.add(new FieldNameValue(Conventions.toDbName(leftValue.getClass().getSimpleName()) + "_" + fieldName, getter.apply(leftValue)));
        return this;
    }

    public <V extends Serializable> InsertRelationship<T,U> valueRight(Function<U,V> getter) {
        V result = getter.apply(rightRecorder.getObject());
        String fieldName = rightRecorder.getCurrentPropertyName();
        rightFieldNames.add(new FieldNameValue(Conventions.toDbName(rightValue.getClass().getSimpleName()) + "_" + fieldName, getter.apply(rightValue)));
        return this;
    }

    public String toSql() {
        List<FieldNameValue> setFieldNames = Stream.concat(leftFieldNames.stream(), rightFieldNames.stream()).collect(Collectors.toList());

        return "INSERT INTO " + Conventions.toDbName(leftValue.getClass().getSimpleName() + rightValue.getClass().getSimpleName())
                + " (" + setFieldNames.stream().map(fnv -> fnv.fieldName).collect(Collectors.joining(", ")) + ") "
                + "VALUES ( " + setFieldNames.stream().map(fnv -> "?").collect(Collectors.joining(", ")) + " )";

    }

    public void execute(Supplier<Connection> connectionFactory) {
        try {
            try (Connection connection = connectionFactory.get()) {
                PreparedStatement statement = connection.prepareStatement(toSql());
                for (int i = 0; i < leftFieldNames.size(); i++) {
                    Conventions.JdbcSetter setter = Conventions.getSetter(leftFieldNames.get(i).value.getClass());
                    setter.apply(statement, i + 1, leftFieldNames.get(i).value);
                }
                for (int i = 0; i < rightFieldNames.size(); i++) {
                    Conventions.JdbcSetter setter = Conventions.getSetter(rightFieldNames.get(i).value.getClass());
                    setter.apply(statement, leftFieldNames.size() + i + 1, rightFieldNames.get(i).value);
                }
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ExecutionException(e);
        }
    }
}

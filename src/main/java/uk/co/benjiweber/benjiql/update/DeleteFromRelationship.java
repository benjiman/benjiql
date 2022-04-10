package uk.co.benjiweber.benjiql.update;

import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;
import uk.co.benjiweber.benjiql.util.Conventions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeleteFromRelationship<T, U> {
    final Class<T> left;
    final Class<U> right;
    final Recorder<T> leftRecorder;
    final Recorder<U> rightRecorder;
    final List<FieldNameValue> leftFieldNames = new ArrayList<>();
    final List<FieldNameValue> rightFieldNames = new ArrayList<>();

    public DeleteFromRelationship(Class<T> left, Class<U> right) {
        this.left = left;
        this.right = right;
        this.leftRecorder = RecordingObject.create(left);
        this.rightRecorder = RecordingObject.create(right);
    }

    public String toSql() {
        List<FieldNameValue> whereFieldNames = Stream.concat(leftFieldNames.stream(), rightFieldNames.stream()).collect(Collectors.toList());
        return "DELETE FROM " + Conventions.toDbName(left, right) +
                (whereFieldNames.size() < 1 ? "" : " WHERE " + whereFieldNames.stream().map(fnv -> fnv.fieldName + " " + fnv.operator + " ?").collect(Collectors.joining(" AND ")));
    }

    public void execute(Supplier<Connection> connectionFactory) throws SQLException {
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
    }

    public <V> DeleteJoinComparison<T,U,V> andLeft(Function<T,V> getter) {
        return whereLeft(getter);
    }

    public <V> DeleteJoinComparison<T,U,V> whereLeft(Function<T,V> getter) {
        getter.apply(leftRecorder.getObject());
        String fieldName = Conventions.toDbName(leftRecorder.getCurrentPropertyName());
        return new DeleteJoinComparison<T,U,V>() {
            public DeleteFromRelationship<T,U> equalTo(V value) {
                leftFieldNames.add(new FieldNameValue(Conventions.toJoinTableName(left,fieldName), value, "="));
                return DeleteFromRelationship.this;
            }

            public DeleteFromRelationship<T,U> notEqualTo(V value) {
                leftFieldNames.add(new FieldNameValue(Conventions.toJoinTableName(left,fieldName), value, "!="));
                return DeleteFromRelationship.this;
            }

            public DeleteFromRelationship<T,U> like(V value) {
                leftFieldNames.add(new FieldNameValue(Conventions.toJoinTableName(left,fieldName), value, "LIKE"));
                return DeleteFromRelationship.this;
            }
        };
    }

    public <V> DeleteJoinComparison<T,U,V> andRight(Function<U,V> getter) {
        return whereRight(getter);
    }

    public <V> DeleteJoinComparison<T,U,V> whereRight(Function<U,V> getter) {
        getter.apply(rightRecorder.getObject());
        String fieldName = Conventions.toDbName(rightRecorder.getCurrentPropertyName());
        return new DeleteJoinComparison<T,U,V>() {
            public DeleteFromRelationship<T,U> equalTo(V value) {
                rightFieldNames.add(new FieldNameValue(Conventions.toJoinTableName(right,fieldName), value, "="));
                return DeleteFromRelationship.this;
            }

            public DeleteFromRelationship<T,U> notEqualTo(V value) {
                rightFieldNames.add(new FieldNameValue(Conventions.toJoinTableName(right,fieldName), value, "!="));
                return DeleteFromRelationship.this;
            }

            public DeleteFromRelationship<T,U> like(V value) {
                rightFieldNames.add(new FieldNameValue(Conventions.toJoinTableName(right,fieldName), value, "LIKE"));
                return DeleteFromRelationship.this;
            }
        };
    }

    public interface DeleteJoinComparison<T,U,V> {
        public DeleteFromRelationship<T,U> equalTo(V value);
        public DeleteFromRelationship<T,U> notEqualTo(V value);
        public DeleteFromRelationship<T,U> like(V value);
    }


}

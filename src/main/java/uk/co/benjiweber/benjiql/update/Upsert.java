package uk.co.benjiweber.benjiql.update;

import uk.co.benjiweber.benjiql.util.Conventions;
import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Upsert<T> {

    final T value;
    final Recorder<T> recorder;
    final List<FieldNameValue> setFieldNames = new ArrayList<>();
    final List<FieldNameValue> whereFieldNames = new ArrayList<>();

    public static class Insert<T> extends Upsert<T> {
        public Insert(T value) {
            super(value);
        }

        @Override
        public String toSql() {
            return "INSERT INTO " + Conventions.toDbName(value.getClass().getSimpleName())
                    + " (" + setFieldNames.stream().map(fnv -> fnv.fieldName).collect(Collectors.joining(", ")) + ") "
                    + "VALUES ( " + setFieldNames.stream().map(fnv -> "?").collect(Collectors.joining(", ")) + " )";
        }
    }

    public static class Update<T> extends Upsert<T> {
        public Update(T value) {
            super(value);
        }

        @Override
        public String toSql() {
            return "UPDATE " + Conventions.toDbName(value.getClass().getSimpleName())
                    + " SET " + setFieldNames.stream().map(fnv -> fnv.fieldName + " = ?").collect(Collectors.joining(", "))
                    + ((whereFieldNames.size() < 1) ? "" : " WHERE " + whereFieldNames.stream().map(fnv -> fnv.fieldName + " " + fnv.operator + " ?").collect(Collectors.joining(" AND ")));
        }
    }

    @SuppressWarnings("unchecked")
    public Upsert(T value) {
        this.value = value;
        this.recorder = (Recorder<T>) RecordingObject.create(value.getClass());
    }

    public static <T> Insert<T> insert(T value) {
        return new Insert<T>(value);
    }

    public static <T,U> InsertRelationship<T,U> insert(T leftValue, U rightValue) {
        return new InsertRelationship<T, U>(leftValue, rightValue);
    }

    public static <T> Update<T> update(T value) {
        return new Update<T>(value);
    }

    public <U extends Serializable> Upsert<T> value(Function<T,U> getter) {
        U result = getter.apply(recorder.getObject());
        String fieldName = recorder.getCurrentPropertyName();
        setFieldNames.add(new FieldNameValue(fieldName, getter.apply(value)));
        return this;
    }

    public abstract String toSql();

    public void execute(Supplier<Connection> connectionFactory) throws SQLException {
        try (Connection connection = connectionFactory.get()) {
            PreparedStatement statement = connection.prepareStatement(toSql());
            for (int i = 0; i < setFieldNames.size(); i++) {
                Conventions.JdbcSetter setter = Conventions.getSetter(setFieldNames.get(i).value.getClass());
                setter.apply(statement, i + 1, setFieldNames.get(i).value);
            }
            for (int i = 0; i < whereFieldNames.size(); i++) {
                Conventions.JdbcSetter setter = Conventions.getSetter(whereFieldNames.get(i).value.getClass());
                setter.apply(statement, i + 1 + setFieldNames.size(), whereFieldNames.get(i).value);
            }
            statement.executeUpdate();
        }
    }

    public <U> UpdateComparison<T,U> and(Function<T,U> getter) {
        return where(getter);
    }

    public <U> UpdateComparison<T,U> where(Function<T,U> getter) {
        getter.apply(recorder.getObject());
        String fieldName = Conventions.toDbName(recorder.getCurrentPropertyName());
        return new UpdateComparison<T,U>(){
            public Upsert<T> equalTo(U value) {
                whereFieldNames.add(new FieldNameValue(fieldName, value, "="));
                return Upsert.this;
            }
            public Upsert<T> notEqualTo(U value) {
                whereFieldNames.add(new FieldNameValue(fieldName, value, "!="));
                return Upsert.this;
            }
            public Upsert<T> like(U value) {
                whereFieldNames.add(new FieldNameValue(fieldName, value, "LIKE"));
                return Upsert.this;
            }
        };
    }

    public interface UpdateComparison<T,U> {
        public Upsert<T> equalTo(U value);
        public Upsert<T> notEqualTo(U value);
        public Upsert<T> like(U value);
    }
}


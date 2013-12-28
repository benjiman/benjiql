package uk.co.benjiweber.benjiql.query;

import com.google.common.base.Joiner;
import uk.co.benjiweber.benjiql.util.Conventions;
import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;
import uk.co.benjiweber.benjiql.results.Mapper;
import uk.co.benjiweber.benjiql.update.FieldNameValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Select<T> {
    private Class<T> cls;
    private final Recorder<T> recorder;
    private final List<FieldNameValue> whereFieldNames = new ArrayList<>();

    public Select(Class<T> cls) {
        this.cls = cls;
        this.recorder = RecordingObject.create(cls);
    }

    public static <T> Select<T> from(Class<T> cls) {
        return new Select<>(cls);
    }

    public <U> SelectComparison<T,U> and(Function<T,U> getter) {
        return where(getter);
    }

    public <U> SelectComparison<T,U> where(Function<T,U> getter) {
        getter.apply(recorder.getObject());
        String fieldName = Conventions.toDbName(recorder.getCurrentPropertyName());
        return new SelectComparison<T, U>() {
            public Select<T> equalTo(U value) {
                whereFieldNames.add(new FieldNameValue<>(fieldName, value, "="));
                return Select.this;
            }

            public Select<T> notEqualTo(U value) {
                whereFieldNames.add(new FieldNameValue<>(fieldName, value, "!="));
                return Select.this;
            }

            public Select<T> like(U value) {
                whereFieldNames.add(new FieldNameValue<>(fieldName, value, "LIKE"));
                return Select.this;
            }
        };
    }

    public String toSql() {
        return "SELECT * FROM " + Conventions.toDbName(cls.getSimpleName()) +
           (whereFieldNames.size() < 1 ? "" : " WHERE " + Joiner.on(" AND ").join(whereFieldNames.stream().map(fnv -> fnv.fieldName + " " + fnv.operator + " ?").collect(Collectors.toList())));
    }

    public Optional<T> select(Mapper<T> mapper, Supplier<Connection> connectionFactory) throws SQLException {
        try (Connection conn = connectionFactory.get()) {
            ResultSet resultSet = runQuery(conn);
            return resultSet.next() ? Optional.of(mapper.map(resultSet)) : Optional.<T>empty();
        }
    }

    public List<T> list(Mapper<T> mapper, Supplier<Connection> connectionFactory) throws SQLException {
        try (Connection conn = connectionFactory.get()) {
            ResultSet resultSet = runQuery(conn);
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapper.map(resultSet));
            }
            return results;
        }
    }

    private ResultSet runQuery(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(toSql());
        for (int i = 0; i < whereFieldNames.size(); i++) {
            Conventions.JdbcSetter setter = Conventions.getSetter(whereFieldNames.get(i).value.getClass());
            setter.apply(statement, i + 1, whereFieldNames.get(i).value);
        }
        return statement.executeQuery();
    }

    public interface SelectComparison<T,U> {
        Select<T> equalTo(U value);
        Select<T> notEqualTo(U value);
        Select<T> like(U value);
    }
}

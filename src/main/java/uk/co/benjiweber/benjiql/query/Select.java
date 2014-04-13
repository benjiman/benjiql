package uk.co.benjiweber.benjiql.query;

import uk.co.benjiweber.benjiql.ddl.JoinTables;
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

import static uk.co.benjiweber.benjiql.util.Exceptions.unchecked;

public class Select<T> implements QueryChain {
    private Class<T> cls;
    private final Optional<Join> join;
    final Recorder<T> recorder;
    private final List<FieldNameValue> whereFieldNames = new ArrayList<>();

    public Select(Class<T> cls) {
        this.cls = cls;
        this.recorder = RecordingObject.create(cls);
        this.join = Optional.empty();
    }

    public Select(Class<T> cls, Join join) {
        this.cls = cls;
        this.join = Optional.of(join);
        this.recorder = RecordingObject.create(cls);
    }

    public static <T> Select<T> from(Class<T> cls) {
        return new Select<>(cls);
    }

    public String tableName() {
        return Conventions.toDbName(cls);
    }

    @Override
    public String fieldName(String fieldName) {
        return fieldName;
    }

    public <U> RelationshipJoinSpecifier<T,U> join(JoinTables<T,U> relationship) {
        return new RelationshipJoin<>(this, relationship);
    }

    public <U> Join<T,U> join(Class<U> table) {
        return new Join<T, U>(this, table);
    }

    public <U> SelectComparison<T,U> and(Function<T,U> getter) {
        return where(getter);
    }

    public <U> SelectComparison<T,U> where(Function<T,U> getter) {
        getter.apply(recorder.getObject());
        String fieldName = Conventions.toDbName(recorder.getCurrentPropertyName());
        return new SelectComparison<T, U>() {
            public Select<T> equalTo(U value) {
                whereFieldNames.add(new FieldNameValue<>(tableName() + "." + fieldName, value, "="));
                return Select.this;
            }

            public Select<T> notEqualTo(U value) {
                whereFieldNames.add(new FieldNameValue<>(tableName() + "." + fieldName, value, "!="));
                return Select.this;
            }

            public Select<T> like(U value) {
                whereFieldNames.add(new FieldNameValue<>(tableName() + "." + fieldName, value, "LIKE"));
                return Select.this;
            }
        };
    }

    public String toSql() {
        return "SELECT * FROM " + fromClause() + whereClause().map(sql -> " WHERE " + sql).orElse("");
    }

    public Optional<String> whereClause() {
        return
            whereFieldNames.stream()
                .map(FieldNameValue::toSQL)
                .reduce((a,b) -> a + " AND " + b)
                .map( sql ->
                    join.map(Join::whereClause)
                        .orElse(Optional.<String>empty())
                        .map(s ->  s + " AND ")
                        .orElse("")
                    + sql
                );
    }

    @Override
    public String fromClause() {
        return join.map(Join::fromClause).orElse(Conventions.toDbName(cls.getSimpleName()));
    }

    @Override
    public Recorder<T> recorder() {
        return recorder;
    }

    public <U> Optional<U> select(Mapper<U> mapper, Supplier<Connection> connectionFactory) throws SQLException {
        try (Connection conn = connectionFactory.get()) {
            ResultSet resultSet = runQuery(conn);
            return resultSet.next() ? Optional.of(mapper.map(resultSet)) : Optional.<U>empty();
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
        setPlaceholders(statement);
        return statement.executeQuery();
    }

    public int setPlaceholders(PreparedStatement statement) throws SQLException {
        int start = join.map(j -> unchecked(() -> j.setPlaceholders(statement))).orElse(0);
        for (int i = 0; i < whereFieldNames.size(); i++) {
            Conventions.JdbcSetter setter = Conventions.getSetter(whereFieldNames.get(i).value.getClass());
            setter.apply(statement, start + i + 1, whereFieldNames.get(i).value);
        }
        return whereFieldNames.size() + start;
    }

    public interface SelectComparison<T,U> {
        Select<T> equalTo(U value);
        Select<T> notEqualTo(U value);
        Select<T> like(U value);
    }
}

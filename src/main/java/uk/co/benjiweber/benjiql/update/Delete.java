package uk.co.benjiweber.benjiql.update;

import com.google.common.base.Joiner;
import uk.co.benjiweber.benjiql.ddl.JoinTables;
import uk.co.benjiweber.benjiql.util.Conventions;
import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Delete<T> {
    final Class<T> cls;
    final Recorder<T> recorder;
    final List<FieldNameValue> whereFieldNames = new ArrayList<>();

    public Delete(Class<T> cls) {
        this.cls = cls;
        this.recorder = RecordingObject.create(cls);
    }

    public static <T> Delete<T> delete(Class<T> cls) {
        return new Delete<T>(cls);
    }

    public static <T,U> DeleteJoin<T,U> delete(JoinTables<T, U> join) {
        return new DeleteJoin<T,U>(join.leftTable, join.rightTable);
    }

    public <U> DeleteComparison<T,U> and(Function<T,U> getter) {
        return where(getter);
    }

    public <U> DeleteComparison<T,U> where(Function<T,U> getter) {
        getter.apply(recorder.getObject());
        String fieldName = Conventions.toDbName(recorder.getCurrentPropertyName());
        return new DeleteComparison<T, U>() {
            public Delete<T> equalTo(U value) {
                whereFieldNames.add(new FieldNameValue(fieldName, value, "="));
                return Delete.this;
            }

            public Delete<T> notEqualTo(U value) {
                whereFieldNames.add(new FieldNameValue(fieldName, value, "!="));
                return Delete.this;
            }

            public Delete<T> like(U value) {
                whereFieldNames.add(new FieldNameValue(fieldName, value, "LIKE"));
                return Delete.this;
            }
        };
    }

    public String toSql() {
        return "DELETE FROM " + Conventions.toDbName(cls.getSimpleName()) +
            (whereFieldNames.size() < 1 ? "" : " WHERE " + Joiner.on(" AND ").join(whereFieldNames.stream().map(fnv -> fnv.fieldName + " " + fnv.operator + " ?").collect(Collectors.toList())));
    }

    public void execute(Supplier<Connection> connectionFactory) throws SQLException {
        try (Connection connection = connectionFactory.get()) {
            PreparedStatement statement = connection.prepareStatement(toSql());
            for (int i = 0; i < whereFieldNames.size(); i++) {
                Conventions.JdbcSetter setter = Conventions.getSetter(whereFieldNames.get(i).value.getClass());
                setter.apply(statement, i + 1, whereFieldNames.get(i).value);
            }
            statement.executeUpdate();
        }
    }

    public interface DeleteComparison<T,U> {
        public Delete<T> equalTo(U value);
        public Delete<T> notEqualTo(U value);
        public Delete<T> like(U value);
    }

}

package uk.co.benjiweber.benjiql.update;

import com.google.common.base.Joiner;
import uk.co.benjiweber.benjiql.ddl.JoinTables;
import uk.co.benjiweber.benjiql.util.Conventions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DeleteJoin<T, U> {
    final Class<T> left;
    final Class<U> right;

    public DeleteJoin(Class<T> left, Class<U> right) {
        this.left = left;
        this.right = right;
    }

    public String toSql() {
        return "DELETE FROM " + Conventions.toDbName(left.getSimpleName() + right.getSimpleName());
    }

    public void execute(Supplier<Connection> connectionFactory) throws SQLException {
        try (Connection connection = connectionFactory.get()) {
            PreparedStatement statement = connection.prepareStatement(toSql());
            statement.executeUpdate();
        }
    }
}

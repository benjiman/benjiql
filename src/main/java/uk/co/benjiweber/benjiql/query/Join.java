package uk.co.benjiweber.benjiql.query;

import uk.co.benjiweber.benjiql.util.Conventions;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Join<T,U> implements JoinSpecifier<T,U> {

    private final QueryChain<T> from;
    private final Class<U> to;
    private List<JoinCondition> joinConditions = new ArrayList<>();

    public Join(QueryChain<T> from, Class<U> to) {
        this.from = from;
        this.to = to;
    }

    public <V extends Serializable> Select<U> using(Function<T, V> p1) {
        Select<U> toSelect = new Select<>(to, this);
        p1.apply(from.recorder().getObject());
        String fieldName = Conventions.toDbName(from.recorder().getCurrentPropertyName());
        joinConditions.add(new JoinCondition(from.tableName() + "." + from.fieldName(fieldName), toSelect.tableName() + "." + fieldName));
        return toSelect;
    }

    public <V extends Serializable, W extends Serializable> Select<U> using(Function<T, V> p1, Function<T,W> p2) {
        Select<U> toSelect = using(p1);
        p2.apply(from.recorder().getObject());
        String fieldName = Conventions.toDbName(from.recorder().getCurrentPropertyName());
        joinConditions.add(new JoinCondition(from.tableName() + "." + from.fieldName(fieldName), toSelect.tableName() + "." + fieldName));
        return toSelect;
    }

    public Optional<String> whereClause() {
        return from.whereClause();
    }

    public String fromClause() {
        return from.fromClause() +
                " JOIN " +
                Conventions.toDbName(to) +
                " ON " +
                joinConditions.stream()
                    .map(JoinCondition::toSQL)
                    .reduce((a, b) -> a + " AND " + b)
                    .orElse("");
    }

    public int setPlaceholders(PreparedStatement stmt) throws SQLException {
        return from.setPlaceholders(stmt);
    }


}

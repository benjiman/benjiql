package uk.co.benjiweber.benjiql.query;

import uk.co.benjiweber.benjiql.ddl.JoinTables;
import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;
import uk.co.benjiweber.benjiql.util.Conventions;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RelationshipJoin<T,U> implements RelationshipJoinSpecifier<T,U>, QueryChain<U> {
    private final QueryChain<T> from;
    private final JoinTables<T, U> to;
    private List<JoinCondition> joinConditions = new ArrayList<>();
    private final Recorder<U> recorder;

    public RelationshipJoin(QueryChain<T> from, JoinTables<T, U> to) {
        this.from = from;
        this.to = to;
        this.recorder = RecordingObject.create(to.rightTable);
    }

    @Override
    public <V extends Serializable> CanOnlyJoin<U> using(Function<T, V> p1) {
        p1.apply(from.recorder().getObject());
        String fieldName = Conventions.toDbName(from.recorder().getCurrentPropertyName());
        joinConditions.add(new JoinCondition(from.tableName() + "." + fieldName, to.getName() + "." + Conventions.toJoinTableName(to.leftTable, fieldName)));


        return new CanOnlyJoin<U>() {
            public <W> JoinSpecifier<U, W> join(Class<W> table) {
                return new Join<>(RelationshipJoin.this, table);
            }

            @Override
            public <W> RelationshipJoinSpecifier<U, W> join(JoinTables<U, W> table) {
                return new RelationshipJoin<>(RelationshipJoin.this,  table);
            }
        };
    }

    @Override
    public <V extends Serializable, W extends Serializable> CanOnlyJoin<U> using(Function<T, V> p1, Function<T, W> p2) {
        CanOnlyJoin<U> result = using(p1);
        p2.apply(from.recorder().getObject());
        String fieldName = Conventions.toDbName(from.recorder().getCurrentPropertyName());
        joinConditions.add(new JoinCondition(from.tableName() + "." + fieldName, to.getName() + "." + Conventions.toJoinTableName(to.leftTable, fieldName)));
        return result;
    }

    @Override
    public String fromClause() {
        return from.fromClause() +
               " JOIN " +
                to.getName() +
                " ON " +
                joinConditions.stream()
                    .map(JoinCondition::toSQL)
                    .reduce((a, b) -> a + " AND " + b)
                    .orElse("");
    }

    @Override
    public Recorder<U> recorder() {
        return recorder;
    }

    @Override
    public String tableName() {
        return to.getName();
    }

    @Override
    public String fieldName(String fieldName) {
        return Conventions.toJoinTableName(to.rightTable, fieldName);
    }

    @Override
    public Optional<String> whereClause() {
        return from.whereClause();
    }

    @Override
    public int setPlaceholders(PreparedStatement statement) throws SQLException {
        return from.setPlaceholders(statement);
    }
}

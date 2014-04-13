package uk.co.benjiweber.benjiql.query;

import uk.co.benjiweber.benjiql.ddl.JoinTables;
import uk.co.benjiweber.benjiql.util.Conventions;

import java.io.Serializable;
import java.util.function.Function;

public interface RelationshipJoinSpecifier<T,U> {

    <V extends Serializable> CanOnlyJoin<U> using(Function<T, V> p1);
    <V extends Serializable, W extends Serializable> CanOnlyJoin<U> using(Function<T, V> p1, Function<T,W> p2);

    public interface CanOnlyJoin<V>  {
        public <W> JoinSpecifier<V,W> join(Class<W> table);
        public <W> RelationshipJoinSpecifier<V,W> join(JoinTables<V,W> table);
    }

}

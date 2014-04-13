package uk.co.benjiweber.benjiql.query;

import java.io.Serializable;
import java.util.function.Function;

public interface JoinSpecifier<T,U> {
    <V extends Serializable> Select<U> using(Function<T, V> p1);
    <V extends Serializable, W extends Serializable> Select<U> using(Function<T, V> p1, Function<T,W> p2);
}

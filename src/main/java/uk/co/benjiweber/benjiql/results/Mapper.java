package uk.co.benjiweber.benjiql.results;

import java.sql.ResultSet;

public interface Mapper<T> {
    T map(ResultSet resultSet);
}


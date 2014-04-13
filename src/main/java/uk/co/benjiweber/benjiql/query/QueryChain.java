package uk.co.benjiweber.benjiql.query;

import uk.co.benjiweber.benjiql.mocking.Recorder;
import uk.co.benjiweber.benjiql.mocking.RecordingObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public interface QueryChain<T> {

    String fromClause();

    Recorder<T> recorder();

    String tableName();

    String fieldName(String fieldName);

    Optional<String> whereClause();

    public int setPlaceholders(PreparedStatement statement) throws SQLException;
}

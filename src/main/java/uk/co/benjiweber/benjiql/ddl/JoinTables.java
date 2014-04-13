package uk.co.benjiweber.benjiql.ddl;

import uk.co.benjiweber.benjiql.util.Conventions;

public class JoinTables<T,U> {
    public final Class<T> leftTable;
    public final Class<U> rightTable;
    private final boolean inverted;

    public JoinTables(Class<T> leftTable, Class<U> rightTable) {
        this(leftTable, rightTable, false);
    }

    public JoinTables(Class<T> leftTable, Class<U> rightTable, boolean inverted) {
        this.leftTable = leftTable;
        this.rightTable = rightTable;
        this.inverted = inverted;
    }

    public JoinTables<U, T> invert() {
        return new JoinTables<U, T>(rightTable, leftTable, true);
    }

    public String getName() {
        return inverted ? Conventions.toDbName(rightTable, leftTable) : Conventions.toDbName(leftTable, rightTable);
    }
}

package uk.co.benjiweber.benjiql.ddl;

public class JoinTables<T,U> {
    public final Class<T> leftTable;
    public final Class<U> rightTable;

    public JoinTables(Class<T> leftTable, Class<U> rightTable) {
        this.leftTable = leftTable;
        this.rightTable = rightTable;
    }
}

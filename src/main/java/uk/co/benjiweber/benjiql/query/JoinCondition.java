package uk.co.benjiweber.benjiql.query;

public class JoinCondition {
    public final String leftFieldName;
    public final String rightFieldName;
    public final String operator;

    public JoinCondition(String leftFieldName, String rightFieldName) {
        this(leftFieldName, rightFieldName, "=");
    }

    public JoinCondition(String leftFieldName, String rightFieldName, String operator) {
        this.leftFieldName = leftFieldName;
        this.rightFieldName = rightFieldName;
        this.operator = operator;
    }

    public String toSQL() {
        return leftFieldName + " " + operator + " " + rightFieldName;
    }

}

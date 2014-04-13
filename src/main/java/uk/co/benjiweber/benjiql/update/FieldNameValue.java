package uk.co.benjiweber.benjiql.update;

public class FieldNameValue<T> {
    public final String fieldName;
    public final T value;
    public final String operator;

    public FieldNameValue(String fieldName, T value) {
        this(fieldName, value, "=");
    }

    public FieldNameValue(String fieldName, T value, String operator) {
        this.fieldName = fieldName;
        this.value = value;
        this.operator = operator;
    }

    public String toSQL() {
        return fieldName + " " + operator + " ?";
    }
}

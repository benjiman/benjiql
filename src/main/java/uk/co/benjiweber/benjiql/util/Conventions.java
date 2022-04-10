package uk.co.benjiweber.benjiql.util;


import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import static java.util.Map.entry;
import java.util.Map;

public class Conventions {

    private static final Map<Class<?>, String> dbTypes = Map.ofEntries(
        entry(String.class, "text"),
        entry(Integer.class, "integer"),
        entry(Float.class, "double"),
        entry(Double.class, "double"),
        entry(Long.class, "bigint"),
        entry(Character.class, "integer"),
        entry(Byte.class, "integer"),
        entry(int.class, "integer"),
        entry(float.class, "double"),
        entry(double.class, "double"),
        entry(long.class, "bigint"),
        entry(char.class, "integer"),
        entry(byte.class, "integer")
    );

    private static final Map<Class<?>, JDBCType> jdbcTypes = Map.ofEntries(
        entry(String.class, JDBCType.VARCHAR),
        entry(Integer.class, JDBCType.INTEGER),
        entry(Float.class, JDBCType.FLOAT),
        entry(Double.class, JDBCType.DOUBLE),
        entry(Long.class, JDBCType.BIGINT),
        entry(Character.class, JDBCType.CHAR),
        entry(Byte.class, JDBCType.TINYINT),
        entry(int.class, JDBCType.INTEGER),
        entry(float.class, JDBCType.FLOAT),
        entry(double.class, JDBCType.DOUBLE),
        entry(long.class, JDBCType.BIGINT),
        entry(char.class, JDBCType.CHAR),
        entry(byte.class, JDBCType.TINYINT)
    );

    public interface JdbcSetter<T> {
        public void apply(PreparedStatement stmt, int index, T t) throws SQLException;
    }
    private static final Map<Class<?>, JdbcSetter> jdbcSetters = Map.ofEntries(
        entry(String.class, (JdbcSetter<String>)PreparedStatement::setString),
        entry(Integer.class, (JdbcSetter<Integer>)PreparedStatement::setInt),
        entry(Float.class, (JdbcSetter<Float>)PreparedStatement::setFloat),
        entry(Double.class, (JdbcSetter<Double>) PreparedStatement::setDouble),
        entry(Long.class, (JdbcSetter<Long>)PreparedStatement::setLong),
        entry(Character.class, (JdbcSetter<Integer>)PreparedStatement::setInt),
        entry(Byte.class, (JdbcSetter<Integer>)PreparedStatement::setInt),
        entry(int.class, (JdbcSetter<Integer>)PreparedStatement::setInt),
        entry(float.class, (JdbcSetter<Float>)PreparedStatement::setFloat),
        entry(double.class, (JdbcSetter<Double>)PreparedStatement::setDouble),
        entry(long.class, (JdbcSetter<Long>)PreparedStatement::setLong),
        entry(char.class, (JdbcSetter<Integer>)PreparedStatement::setInt),
        entry(byte.class, (JdbcSetter<Integer>)PreparedStatement::setInt)
    );

    public static String toDbName(String name) {
        return uncapitalize(toSnakeCase(banishGetterSetters(name)));
    }

    public static String toDbName(Class<?>... clses) {
        return Arrays.asList(clses)
            .stream()
            .map(Class::getSimpleName)
            .map(Conventions::toDbName)
            .reduce((a,b) -> a + "_" + b)
            .orElse("");
    }

    public static String toJoinTableName(Class<?> cls, String name) {
        return toDbName(cls.getSimpleName()) + "_" + toDbName(name);
    }

    private static String banishGetterSetters(String name) {
        return name.replaceAll("^(get|set)", "");
    }

    public static String uncapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String toSnakeCase(String s) {
        return s.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase();
    }

    public static <T> String toDbType(Class<T> cls) {
        return dbTypes.getOrDefault(cls, "text");
    }

    public static <T> JdbcSetter<T> getSetter(Class<T> cls) {
        return jdbcSetters.get(cls);
    }
}

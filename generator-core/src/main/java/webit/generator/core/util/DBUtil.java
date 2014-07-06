// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import webit.generator.core.Config;

public class DBUtil {

    private final static Map<Integer, String> TYPES = new HashMap<Integer, String>();
    private static Connection connection;

    static {
        TYPES.put(Types.TINYINT, "java.lang.Byte");
        TYPES.put(Types.SMALLINT, "java.lang.Short");
        TYPES.put(Types.INTEGER, "java.lang.Integer");
        TYPES.put(Types.BIGINT, "java.lang.Long");
        TYPES.put(Types.REAL, "java.lang.Float");
        TYPES.put(Types.FLOAT, "java.lang.Double");
        TYPES.put(Types.DOUBLE, "java.lang.Double");
        TYPES.put(Types.DECIMAL, "java.math.BigDecimal");
        TYPES.put(Types.NUMERIC, "java.math.BigDecimal");
        TYPES.put(Types.BIT, "java.lang.Boolean");
        TYPES.put(Types.BOOLEAN, "java.lang.Boolean");
        TYPES.put(Types.CHAR, "java.lang.String");
        TYPES.put(Types.VARCHAR, "java.lang.String");
        TYPES.put(Types.LONGVARCHAR, "java.lang.String");
        TYPES.put(Types.NVARCHAR, "java.lang.String");
        TYPES.put(Types.BINARY, "byte[]");
        TYPES.put(Types.VARBINARY, "byte[]");
        TYPES.put(Types.LONGVARBINARY, "byte[]");
        TYPES.put(Types.DATE, "java.sql.Date");
        TYPES.put(Types.TIME, "java.sql.Time");
        TYPES.put(Types.TIMESTAMP, "java.sql.Timestamp");
        TYPES.put(Types.CLOB, "java.sql.Clob");
        TYPES.put(Types.BLOB, "java.sql.Blob");
        TYPES.put(Types.ARRAY, "java.sql.Array");
        TYPES.put(Types.REF, "java.sql.Ref");
        TYPES.put(Types.STRUCT, "java.lang.Object");
        TYPES.put(Types.JAVA_OBJECT, "java.lang.Object");
    }

    public static boolean isStringType(final int sqlType) {
        return sqlType == Types.CHAR
                || sqlType == Types.VARCHAR
                || sqlType == Types.LONGVARCHAR
                || sqlType == Types.NCHAR
                || sqlType == Types.NVARCHAR;
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                final String driver = Config.getRequiredString("db.driver");
                try {
                    Class.forName(driver);
                    connection = DriverManager.getConnection(Config.getRequiredString("db.url"), Config.getRequiredString("db.username"), Config.getString("db.password"));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("not found jdbc driver class:[" + driver + "]", e);
                }
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getJavaType(final int sqlType, final int size, final int decimalDigits) {
        if (decimalDigits == 0
                && (sqlType == Types.DECIMAL || sqlType == Types.NUMERIC)) {
            if (size == 1) {
                return "java.lang.Boolean";
            } else if (size < 3) {
                return "java.lang.Byte";
            } else if (size < 5) {
                return "java.lang.Short";
            } else if (size < 10) {
                return "java.lang.Integer";
            } else if (size < 19) {
                return "java.lang.Long";
            } else {
                return "java.math.BigDecimal";
            }
        }
        final String result;
        return (result = TYPES.get(sqlType)) != null
                ? result
                : "java.lang.Object";
    }

    public static String getDBType() {
        final String driver = Config.getRequiredString("db.driver").toLowerCase();
        if (driver.indexOf("mysql") >= 0) {
            return "mysql";
        }
        if (driver.indexOf("oracle") >= 0) {
            return "oracle";
        }
        if (driver.indexOf("com.microsoft.sqlserver.jdbc.sqlserverdriver") >= 0) {
            return "sqlserver2005";
        }
        if (driver.indexOf("microsoft") >= 0 || driver.indexOf("jtds") >= 0) {
            return "sqlserver";
        }
        if (driver.indexOf("postgresql") >= 0) {
            return "postgresql";
        }
        if (driver.indexOf("sybase") >= 0) {
            return "sybase";
        }
        if (driver.indexOf("db2") >= 0) {
            return "db2";
        }
        if (driver.indexOf("hsqldb") >= 0) {
            return "hsqldb";
        }
        if (driver.indexOf("derby") >= 0) {
            return "derby";
        }
        if (driver.indexOf("h2") >= 0) {
            return "h2";
        }
        return "unkown";
    }
}

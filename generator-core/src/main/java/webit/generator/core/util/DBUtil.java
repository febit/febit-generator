// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.sql.Types;
import webit.generator.core.Config;

public class DBUtil {

    public static boolean isStringType(final int sqlType) {
        return sqlType == Types.CHAR
                || sqlType == Types.VARCHAR
                || sqlType == Types.LONGVARCHAR
                || sqlType == Types.NCHAR
                || sqlType == Types.NVARCHAR;
    }

    public static String getJavaType(final int sqlType, final int size, final int decimalDigits) {

        switch (sqlType) {
            case Types.TINYINT:
                if (size == 1) {
                    return "java.lang.Boolean";
                }
                return "java.lang.Byte";
            case Types.SMALLINT:
                return "java.lang.Short";
            case Types.INTEGER:
                return "java.lang.Integer";
            case Types.BIGINT:
                return "java.lang.Long";
            case Types.REAL:
                return "java.lang.Float";

            case Types.FLOAT:
            case Types.DOUBLE:
                return "java.lang.Double";

            case Types.DECIMAL:
            case Types.NUMERIC:
                if (decimalDigits == 0) {
                    if (size == 1) {
                        return "java.lang.Boolean";
                    } else if (size <= 2) {
                        return "java.lang.Byte";
                    } else if (size <= 5) {
                        return "java.lang.Short";
                    } else if (size <= 11) {
                        return "java.lang.Integer";
                    } else if (size <= 20) {
                        return "java.lang.Long";
                    } else {
                        return "java.math.BigInteger";
                    }
                }
                return "java.math.BigDecimal";

            case Types.BIT:
            case Types.BOOLEAN:
                return "java.lang.Boolean";

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
                return "java.lang.String";

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "byte[]";

            case Types.DATE:
                return "java.sql.Date";
            case Types.TIME:
                return "java.sql.Time";
            case Types.TIMESTAMP:
                return "java.sql.Timestamp";
            case Types.CLOB:
                return "java.sql.Clob";
            case Types.BLOB:
                return "java.sql.Blob";
            case Types.ARRAY:
                return "java.sql.Array";
            case Types.REF:
                return "java.sql.Ref";

            case Types.STRUCT:
            case Types.JAVA_OBJECT:
            default:
                return "java.lang.Object";
        }
    }

    public static String getDBType() {
        final String driver = Config.getRequiredString("db.driver").toLowerCase();
        if (driver.contains("mysql")) {
            return "mysql";
        }
        if (driver.contains("oracle")) {
            return "oracle";
        }
        if (driver.contains("com.microsoft.sqlserver.jdbc.sqlserverdriver")) {
            return "sqlserver2005";
        }
        if (driver.contains("microsoft") || driver.contains("jtds")) {
            return "sqlserver";
        }
        if (driver.contains("postgresql")) {
            return "postgresql";
        }
        if (driver.contains("sybase")) {
            return "sybase";
        }
        if (driver.contains("db2")) {
            return "db2";
        }
        if (driver.contains("hsqldb")) {
            return "hsqldb";
        }
        if (driver.contains("derby")) {
            return "derby";
        }
        if (driver.contains("h2")) {
            return "h2";
        }
        return "unkown";
    }
}

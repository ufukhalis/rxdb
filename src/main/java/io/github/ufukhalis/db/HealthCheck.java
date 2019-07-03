package io.github.ufukhalis.db;

public enum HealthCheck {

    ORACLE("select 1 from dual"), //
    HSQLDB("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"), //
    H2("select 1"), //
    SQL_SERVER("select 1"), //
    MYSQL("select 1"), //
    POSTGRES("select 1"), //
    SQLITE("select 1"), //
    DB2("select 1 from sysibm.sysdummy1"), //
    DERBY("SELECT 1 FROM SYSIBM.SYSDUMMY1"), //
    INFORMIX("select count(*) from systables"), //
    OTHER("select 1");

    private final String sql;

    HealthCheck(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}

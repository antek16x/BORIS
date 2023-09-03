package org.boris.config;

import java.util.Properties;

public class JpaProperties {

    private static final String ENV_HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String ENV_HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
    private static final String ENV_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    private static final String ENV_HIBERNATE_FORMAT_SQL = "hibernate.format_sql";
    private static final String ENV_HIBERNATE_GENERATE_DDL = "hibernate.generate-ddl";

    public static Properties jpaProperties(
            String formatSQL,
            String showSQL,
            String hbm2DDLAuto,
            String dialect,
            String generateDDL
    ) {
        Properties extraProperties = new Properties();
        extraProperties.put(ENV_HIBERNATE_FORMAT_SQL, formatSQL);
        extraProperties.put(ENV_HIBERNATE_SHOW_SQL, showSQL);
        extraProperties.put(ENV_HIBERNATE_HBM2DDL_AUTO, hbm2DDLAuto);
        extraProperties.put(ENV_HIBERNATE_DIALECT, dialect);
        extraProperties.put(ENV_HIBERNATE_GENERATE_DDL, generateDDL);

        return extraProperties;
    }
}

package com.hugin_munin.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static HikariDataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            try {
                // Cargar variables de entorno
                Dotenv dotenv = Dotenv.configure()
                        .ignoreIfMissing() // No fallar si no existe .env
                        .load();

                // Obtener valores con defaults
                String host = getEnvValue(dotenv, "DB_HOST", "localhost");
                String dbName = getEnvValue(dotenv, "DB_SCHEMA", "HUGIN_MUNIN");
                String user = getEnvValue(dotenv, "DB_USER", "root");
                String password = getEnvValue(dotenv, "DB_PSWD", "");

                String jdbcUrl = String.format("jdbc:mysql://%s:3306/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", host, dbName);

                System.out.println("🔗 Conectando a: " + jdbcUrl);
                System.out.println("👤 Usuario: " + user);

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(user);
                config.setPassword(password);
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");

                // Configuraciones adicionales para evitar problemas
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);

                dataSource = new HikariDataSource(config);

                // Probar la conexión
                try (Connection testConn = dataSource.getConnection()) {
                    System.out.println("✅ Conexión a base de datos exitosa");
                }

            } catch (Exception e) {
                System.err.println("❌ Error al configurar la base de datos: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("No se pudo configurar la conexión a la base de datos", e);
            }
        }
        return dataSource;
    }

    private static String getEnvValue(Dotenv dotenv, String key, String defaultValue) {
        String value = dotenv.get(key);
        if (value == null || value.trim().isEmpty()) {
            System.out.println("⚠️  Variable " + key + " no encontrada, usando valor por defecto: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            System.out.println("🔌 Cerrando " + dataSource.getJdbcUrl());
            dataSource.close();
        }
    }
}
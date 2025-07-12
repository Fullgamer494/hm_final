package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.Especie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Repositorio para gestionar especies
 * Maneja todas las operaciones CRUD para la entidad Especie
 */
public class EspecieRepository {

    /**
     * Buscar todas las especies
     */
    public List<Especie> findAllSpecies() throws SQLException {
        List<Especie> especies = new ArrayList<>();
        String query = "SELECT * FROM especie ORDER BY genero ASC, especie ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Especie especie = mapResultSetToEspecie(rs);
                especies.add(especie);
            }
        }
        return especies;
    }

    /**
     * Buscar especie por ID
     */
    public Optional<Especie> findById(Integer id) throws SQLException {
        String query = "SELECT * FROM especie WHERE id_especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEspecie(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Buscar especies por nombre científico (búsqueda parcial)
     */
    public List<Especie> findSpeciesByScientificName(String scientificName) throws SQLException {
        List<Especie> especies = new ArrayList<>();
        String query = "SELECT * FROM especie WHERE CONCAT(genero, ' ', especie) LIKE ? ORDER BY genero, especie";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + scientificName + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    especies.add(mapResultSetToEspecie(rs));
                }
            }
        }

        return especies;
    }

    /**
     * Buscar especies por género
     */
    public List<Especie> findByGenero(String genero) throws SQLException {
        List<Especie> especies = new ArrayList<>();
        String query = "SELECT * FROM especie WHERE genero LIKE ? ORDER BY especie ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + genero + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    especies.add(mapResultSetToEspecie(rs));
                }
            }
        }
        return especies;
    }

    /**
     * Buscar especie exacta por género y especie
     */
    public Optional<Especie> findByGeneroAndEspecie(String genero, String especie) throws SQLException {
        String query = "SELECT * FROM especie WHERE genero = ? AND especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, genero.trim());
            stmt.setString(2, especie.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEspecie(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Guardar nueva especie
     */
    public Especie saveSpecie(Especie especie) throws SQLException {
        String query = "INSERT INTO especie (genero, especie) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, especie.getGenero());
            stmt.setString(2, especie.getEspecie());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Error al crear especie, no se insertaron filas");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    especie.setId_especie(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear especie, no se obtuvo el ID");
                }
            }
        }

        return especie;
    }

    /**
     * Actualizar especie existente
     */
    public boolean update(Especie especie) throws SQLException {
        String query = "UPDATE especie SET genero = ?, especie = ? WHERE id_especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, especie.getGenero());
            stmt.setString(2, especie.getEspecie());
            stmt.setInt(3, especie.getId_especie());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Eliminar especie por ID
     */
    public boolean deleteById(Integer id) throws SQLException {
        String query = "DELETE FROM especie WHERE id_especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Verificar si existe especie por ID
     */
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM especie WHERE id_especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Verificar si existe especie por género y especie
     */
    public boolean existsByGeneroAndEspecie(String genero, String especie) throws SQLException {
        String query = "SELECT COUNT(*) FROM especie WHERE genero = ? AND especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, genero.trim());
            stmt.setString(2, especie.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Verificar si la especie está siendo usada por especímenes
     */
    public boolean isSpecieInUse(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM especimen WHERE id_especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Contar total de especies
     */
    public int countTotal() throws SQLException {
        String query = "SELECT COUNT(*) FROM especie";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Contar géneros únicos
     */
    public int countUniqueGeneros() throws SQLException {
        String query = "SELECT COUNT(DISTINCT genero) FROM especie";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Obtener todos los géneros únicos
     */
    public List<String> findAllGeneros() throws SQLException {
        List<String> generos = new ArrayList<>();
        String query = "SELECT DISTINCT genero FROM especie ORDER BY genero ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                generos.add(rs.getString("genero"));
            }
        }
        return generos;
    }

    /**
     * Obtener géneros más comunes con conteo
     */
    public List<Map<String, Object>> getMostCommonGeneros(int limit) throws SQLException {
        List<Map<String, Object>> generos = new ArrayList<>();
        String query = """
            SELECT genero, COUNT(*) as cantidad_especies
            FROM especie 
            GROUP BY genero 
            ORDER BY cantidad_especies DESC, genero ASC 
            LIMIT ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> genero = new HashMap<>();
                    genero.put("genero", rs.getString("genero"));
                    genero.put("cantidad_especies", rs.getInt("cantidad_especies"));
                    generos.add(genero);
                }
            }
        }
        return generos;
    }

    /**
     * Buscar especies con información de conteo de especímenes
     */
    public List<EspecieConEstadisticas> findAllWithSpecimenCount() throws SQLException {
        List<EspecieConEstadisticas> especies = new ArrayList<>();
        String query = """
            SELECT e.id_especie, e.genero, e.especie,
                   COUNT(esp.id_especimen) as total_especimenes,
                   COUNT(CASE WHEN esp.activo = TRUE THEN 1 END) as especimenes_activos
            FROM especie e
            LEFT JOIN especimen esp ON e.id_especie = esp.id_especie
            GROUP BY e.id_especie, e.genero, e.especie
            ORDER BY e.genero ASC, e.especie ASC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EspecieConEstadisticas especie = new EspecieConEstadisticas(
                        rs.getInt("id_especie"),
                        rs.getString("genero"),
                        rs.getString("especie"),
                        rs.getInt("total_especimenes"),
                        rs.getInt("especimenes_activos")
                );
                especies.add(especie);
            }
        }
        return especies;
    }

    /**
     * Buscar especies más utilizadas (con más especímenes)
     */
    public List<EspecieConEstadisticas> findMostUsedSpecies(int limit) throws SQLException {
        List<EspecieConEstadisticas> especies = new ArrayList<>();
        String query = """
            SELECT e.id_especie, e.genero, e.especie,
                   COUNT(esp.id_especimen) as total_especimenes,
                   COUNT(CASE WHEN esp.activo = TRUE THEN 1 END) as especimenes_activos
            FROM especie e
            LEFT JOIN especimen esp ON e.id_especie = esp.id_especie
            GROUP BY e.id_especie, e.genero, e.especie
            HAVING total_especimenes > 0
            ORDER BY total_especimenes DESC
            LIMIT ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EspecieConEstadisticas especie = new EspecieConEstadisticas(
                            rs.getInt("id_especie"),
                            rs.getString("genero"),
                            rs.getString("especie"),
                            rs.getInt("total_especimenes"),
                            rs.getInt("especimenes_activos")
                    );
                    especies.add(especie);
                }
            }
        }
        return especies;
    }

    /**
     * Buscar especies sin especímenes
     */
    public List<Especie> findSpeciesWithoutSpecimens() throws SQLException {
        List<Especie> especies = new ArrayList<>();
        String query = """
            SELECT e.* FROM especie e
            LEFT JOIN especimen esp ON e.id_especie = esp.id_especie
            WHERE esp.id_especimen IS NULL
            ORDER BY e.genero ASC, e.especie ASC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                especies.add(mapResultSetToEspecie(rs));
            }
        }
        return especies;
    }

    /**
     * Mapear ResultSet a objeto Especie
     */
    private Especie mapResultSetToEspecie(ResultSet rs) throws SQLException {
        Especie especie = new Especie();
        especie.setId_especie(rs.getInt("id_especie"));
        especie.setGenero(rs.getString("genero"));
        especie.setEspecie(rs.getString("especie"));
        return especie;
    }

    /**
     * Clase auxiliar para especies con estadísticas
     */
    public static class EspecieConEstadisticas {
        private Integer idEspecie;
        private String genero;
        private String especie;
        private Integer totalEspecimenes;
        private Integer especimenesActivos;

        public EspecieConEstadisticas(Integer idEspecie, String genero, String especie,
                                      Integer totalEspecimenes, Integer especimenesActivos) {
            this.idEspecie = idEspecie;
            this.genero = genero;
            this.especie = especie;
            this.totalEspecimenes = totalEspecimenes;
            this.especimenesActivos = especimenesActivos;
        }

        // Getters y setters
        public Integer getIdEspecie() { return idEspecie; }
        public void setIdEspecie(Integer idEspecie) { this.idEspecie = idEspecie; }

        public String getGenero() { return genero; }
        public void setGenero(String genero) { this.genero = genero; }

        public String getEspecie() { return especie; }
        public void setEspecie(String especie) { this.especie = especie; }

        public Integer getTotalEspecimenes() { return totalEspecimenes; }
        public void setTotalEspecimenes(Integer totalEspecimenes) { this.totalEspecimenes = totalEspecimenes; }

        public Integer getEspecimenesActivos() { return especimenesActivos; }
        public void setEspecimenesActivos(Integer especimenesActivos) { this.especimenesActivos = especimenesActivos; }
    }
}
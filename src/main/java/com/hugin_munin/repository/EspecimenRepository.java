package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.Especimen;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar especímenes
 * Maneja todas las operaciones CRUD para la entidad Especimen
 */
public class EspecimenRepository {

    /**
     * Buscar todos los especímenes
     */
    public List<Especimen> findAllSpecimen() throws SQLException {
        List<Especimen> especimenes = new ArrayList<>();
        String query = "SELECT * FROM especimen ORDER BY id_especimen ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Especimen especimen = mapResultSetToEspecimen(rs);
                especimenes.add(especimen);
            }
        }
        return especimenes;
    }

    /**
     * Buscar especímenes activos
     */
    public List<Especimen> findActiveSpecimens() throws SQLException {
        List<Especimen> especimenes = new ArrayList<>();
        String query = "SELECT * FROM especimen WHERE activo = TRUE ORDER BY nombre_especimen ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                especimenes.add(mapResultSetToEspecimen(rs));
            }
        }
        return especimenes;
    }

    /**
     * Buscar especimen por ID
     */
    public Optional<Especimen> findById(Integer id) throws SQLException {
        String query = "SELECT * FROM especimen WHERE id_especimen = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEspecimen(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Buscar especimen por número de inventario
     */
    public Optional<Especimen> findByInventoryNumber(String numInventario) throws SQLException {
        String query = "SELECT * FROM especimen WHERE num_inventario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, numInventario.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEspecimen(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Buscar especímenes por nombre (búsqueda parcial)
     */
    public List<Especimen> findByNameContaining(String nombre) throws SQLException {
        List<Especimen> especimenes = new ArrayList<>();
        String query = "SELECT * FROM especimen WHERE nombre_especimen LIKE ? ORDER BY nombre_especimen ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + nombre + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    especimenes.add(mapResultSetToEspecimen(rs));
                }
            }
        }
        return especimenes;
    }

    /**
     * Buscar especímenes por especie
     */
    public List<Especimen> findByEspecie(Integer idEspecie) throws SQLException {
        List<Especimen> especimenes = new ArrayList<>();
        String query = "SELECT * FROM especimen WHERE id_especie = ? ORDER BY nombre_especimen ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idEspecie);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    especimenes.add(mapResultSetToEspecimen(rs));
                }
            }
        }
        return especimenes;
    }

    /**
     * Guardar nuevo especimen
     */
    public Especimen saveSpecimen(Especimen especimen) throws SQLException {
        String query = "INSERT INTO especimen (num_inventario, id_especie, nombre_especimen, activo) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, especimen.getNum_inventario());
            stmt.setInt(2, especimen.getId_especie());
            stmt.setString(3, especimen.getNombre_especimen());
            stmt.setBoolean(4, especimen.isActivo());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Error al crear especimen, no se insertaron filas");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    especimen.setId_especimen(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear especimen, no se obtuvo el ID");
                }
            }
        }

        return especimen;
    }

    /**
     * Actualizar especimen existente
     */
    public boolean update(Especimen especimen) throws SQLException {
        String query = "UPDATE especimen SET num_inventario = ?, id_especie = ?, nombre_especimen = ?, activo = ? WHERE id_especimen = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, especimen.getNum_inventario());
            stmt.setInt(2, especimen.getId_especie());
            stmt.setString(3, especimen.getNombre_especimen());
            stmt.setBoolean(4, especimen.isActivo());
            stmt.setInt(5, especimen.getId_especimen());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Eliminar especimen por ID
     */
    public boolean deleteById(Integer id) throws SQLException {
        String query = "DELETE FROM especimen WHERE id_especimen = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Activar especimen por ID
     */
    public boolean activateById(Integer id) throws SQLException {
        String query = "UPDATE especimen SET activo = TRUE WHERE id_especimen = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Desactivar especimen por ID
     */
    public boolean deactivateById(Integer id) throws SQLException {
        String query = "UPDATE especimen SET activo = FALSE WHERE id_especimen = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Verificar si existe especimen por ID
     */
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM especimen WHERE id_especimen = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Verificar si existe especimen por número de inventario
     */
    public boolean existsByIN(String numInventario) throws SQLException {
        String query = "SELECT COUNT(*) FROM especimen WHERE num_inventario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, numInventario.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Verificar si el especimen está siendo usado en registros
     */
    public boolean isSpecimenInUse(Integer id) throws SQLException {
        String query = """
            SELECT COUNT(*) FROM (
                SELECT id_especimen FROM registro_alta WHERE id_especimen = ?
                UNION
                SELECT id_especimen FROM registro_baja WHERE id_especimen = ?
            ) AS usage_check
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.setInt(2, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Contar total de especímenes
     */
    public int countTotal() throws SQLException {
        String query = "SELECT COUNT(*) FROM especimen";

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
     * Contar especímenes activos
     */
    public int countActive() throws SQLException {
        String query = "SELECT COUNT(*) FROM especimen WHERE activo = TRUE";

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
     * Contar especímenes inactivos
     */
    public int countInactive() throws SQLException {
        String query = "SELECT COUNT(*) FROM especimen WHERE activo = FALSE";

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
     * Contar especímenes por especie
     */
    public int countByEspecie(Integer idEspecie) throws SQLException {
        String query = "SELECT COUNT(*) FROM especimen WHERE id_especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idEspecie);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Obtener especímenes más recientes
     */
    public List<Especimen> findMostRecent(int limit) throws SQLException {
        List<Especimen> especimenes = new ArrayList<>();
        String query = "SELECT * FROM especimen ORDER BY id_especimen DESC LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    especimenes.add(mapResultSetToEspecimen(rs));
                }
            }
        }
        return especimenes;
    }

    /**
     * Buscar especímenes con joins completos (para mostrar información de especie)
     */
    public List<Especimen> findAllWithSpecieInfo() throws SQLException {
        List<Especimen> especimenes = new ArrayList<>();
        String query = """
            SELECT esp.*, e.genero, e.especie 
            FROM especimen esp
            LEFT JOIN especie e ON esp.id_especie = e.id_especie
            ORDER BY esp.id_especimen ASC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Especimen especimen = mapResultSetToEspecimen(rs);

                // Agregar información de especie si está disponible
                if (rs.getString("genero") != null && rs.getString("especie") != null) {
                    com.hugin_munin.model.Especie especie = new com.hugin_munin.model.Especie();
                    especie.setId_especie(rs.getInt("id_especie"));
                    especie.setGenero(rs.getString("genero"));
                    especie.setEspecie(rs.getString("especie"));
                    especimen.setEspecie(especie);
                }

                especimenes.add(especimen);
            }
        }
        return especimenes;
    }

    /**
     * Buscar especimen por ID con información de especie
     */
    public Optional<Especimen> findByIdWithSpecieInfo(Integer id) throws SQLException {
        String query = """
            SELECT esp.*, e.genero, e.especie 
            FROM especimen esp
            LEFT JOIN especie e ON esp.id_especie = e.id_especie
            WHERE esp.id_especimen = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Especimen especimen = mapResultSetToEspecimen(rs);

                    // Agregar información de especie si está disponible
                    if (rs.getString("genero") != null && rs.getString("especie") != null) {
                        com.hugin_munin.model.Especie especie = new com.hugin_munin.model.Especie();
                        especie.setId_especie(rs.getInt("id_especie"));
                        especie.setGenero(rs.getString("genero"));
                        especie.setEspecie(rs.getString("especie"));
                        especimen.setEspecie(especie);
                    }

                    return Optional.of(especimen);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Obtener estadísticas de especímenes por especie
     */
    public List<EspecimenEstadistica> getEstadisticasPorEspecie() throws SQLException {
        String query = """
            SELECT e.id_especie, e.genero, e.especie, 
                   COUNT(esp.id_especimen) as total_especimenes,
                   COUNT(CASE WHEN esp.activo = TRUE THEN 1 END) as especimenes_activos,
                   COUNT(CASE WHEN esp.activo = FALSE THEN 1 END) as especimenes_inactivos
            FROM especie e
            LEFT JOIN especimen esp ON e.id_especie = esp.id_especie
            GROUP BY e.id_especie, e.genero, e.especie
            ORDER BY total_especimenes DESC
            """;

        List<EspecimenEstadistica> estadisticas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EspecimenEstadistica estadistica = new EspecimenEstadistica(
                        rs.getInt("id_especie"),
                        rs.getString("genero"),
                        rs.getString("especie"),
                        rs.getInt("total_especimenes"),
                        rs.getInt("especimenes_activos"),
                        rs.getInt("especimenes_inactivos")
                );
                estadisticas.add(estadistica);
            }
        }

        return estadisticas;
    }

    /**
     * Mapear ResultSet a objeto Especimen
     */
    private Especimen mapResultSetToEspecimen(ResultSet rs) throws SQLException {
        Especimen especimen = new Especimen();
        especimen.setId_especimen(rs.getInt("id_especimen"));
        especimen.setNum_inventario(rs.getString("num_inventario"));
        especimen.setId_especie(rs.getInt("id_especie"));
        especimen.setNombre_especimen(rs.getString("nombre_especimen"));
        especimen.setActivo(rs.getBoolean("activo"));

        return especimen;
    }

    /**
     * Clase auxiliar para estadísticas de especímenes por especie
     */
    public static class EspecimenEstadistica {
        private Integer idEspecie;
        private String genero;
        private String especie;
        private Integer totalEspecimenes;
        private Integer especimenesActivos;
        private Integer especimenesInactivos;

        public EspecimenEstadistica(Integer idEspecie, String genero, String especie,
                                    Integer totalEspecimenes, Integer especimenesActivos,
                                    Integer especimenesInactivos) {
            this.idEspecie = idEspecie;
            this.genero = genero;
            this.especie = especie;
            this.totalEspecimenes = totalEspecimenes;
            this.especimenesActivos = especimenesActivos;
            this.especimenesInactivos = especimenesInactivos;
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

        public Integer getEspecimenesInactivos() { return especimenesInactivos; }
        public void setEspecimenesInactivos(Integer especimenesInactivos) { this.especimenesInactivos = especimenesInactivos; }
    }
}
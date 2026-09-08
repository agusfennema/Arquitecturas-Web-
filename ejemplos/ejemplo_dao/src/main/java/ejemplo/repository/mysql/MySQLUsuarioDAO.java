package ejemplo.repository.mysql;

import ejemplo.dao.UsuarioDAO;
import ejemplo.entity.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLUsuarioDAO implements UsuarioDAO {

    private final Connection cn;

    public MySQLUsuarioDAO(Connection cn) {
        this.cn = cn;
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        final String sql = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(100) NOT NULL," +
                "email VARCHAR(120) NOT NULL UNIQUE," +
                "edad INT NULL" +
                ")";
        try (Statement st = cn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creando tabla 'usuarios'", e);
        }
    }

    @Override
    public Usuario findById(Long id) {
        final String sql = "SELECT id, nombre, email, edad FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findById", e);
        }
    }

    @Override
    public List<Usuario> findAll() {
        final String sql = "SELECT id, nombre, email, edad FROM usuarios";
        List<Usuario> out = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error en findAll", e);
        }
        return out;
    }

    @Override
    public void create(Usuario u) {
        final String sql = "INSERT INTO usuarios (nombre, email, edad) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            if (u.getEdad() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, u.getEdad());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en create", e);
        }
    }

    @Override
    public void update(Usuario u) {
        final String sql = "UPDATE usuarios SET nombre = ?, email = ?, edad = ? WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            if (u.getEdad() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, u.getEdad());
            ps.setLong(4, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en update", e);
        }
    }

    @Override
    public void delete(Long id) {
        final String sql = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en delete", e);
        }
    }

    @Override
    public void deleteAll() {
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("DELETE FROM usuarios");
            st.execute("ALTER TABLE usuarios AUTO_INCREMENT = 1");
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando 'usuarios'", e);
        }
    }


    // ---- mapper privado ----
    private Usuario map(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        int edad = rs.getInt("edad");
        u.setEdad(rs.wasNull() ? null : edad);
        return u;
    }
}


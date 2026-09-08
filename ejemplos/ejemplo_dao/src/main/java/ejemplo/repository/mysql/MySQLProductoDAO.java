package ejemplo.repository.mysql;

import ejemplo.dao.ProductoDAO;
import ejemplo.entity.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLProductoDAO implements ProductoDAO {

    private final Connection cn;

    public MySQLProductoDAO(Connection cn) {
        this.cn = cn;
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        final String sql = "CREATE TABLE IF NOT EXISTS productos (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(120) NOT NULL," +
                "stock INT," +
                "precio FLOAT" + // <- FLOAT en MySQL
                ")";
        try (Statement st = cn.createStatement()) { st.execute(sql); }
        catch (SQLException e) { throw new RuntimeException("Error creando tabla 'productos'", e); }
    }

    @Override public Producto findById(Long id) {
        final String sql = "SELECT id, nombre, stock, precio FROM productos WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new RuntimeException("Error en findById", e); }
    }

    @Override public List<Producto> findAll() {
        final String sql = "SELECT id, nombre, stock, precio FROM productos";
        List<Producto> out = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException("Error en findAll", e); }
        return out;
    }

    @Override public void create(Producto p) {
        final String sql = "INSERT INTO productos (nombre, stock, precio) VALUES (?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            if (p.getStock() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, p.getStock());
            if (p.getPrecio() == null) ps.setNull(3, Types.FLOAT); else ps.setFloat(3, p.getPrecio());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) p.setId(keys.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException("Error en create(producto)", e); }
    }

    @Override public void update(Producto p) {
        final String sql = "UPDATE productos SET nombre=?, stock=?, precio=? WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            if (p.getStock() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, p.getStock());
            if (p.getPrecio() == null) ps.setNull(3, Types.FLOAT); else ps.setFloat(3, p.getPrecio());
            ps.setLong(4, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error en update(producto)", e); }
    }

    @Override public void delete(Long id) {
        final String sql = "DELETE FROM productos WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error en delete(producto)", e); }
    }

    @Override
    public void deleteAll() {
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("DELETE FROM productos");
            st.execute("ALTER TABLE productos AUTO_INCREMENT = 1");
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando 'productos'", e);
        }
    }



    private Producto map(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getLong("id"));
        p.setNombre(rs.getString("nombre"));
        int stock = rs.getInt("stock");
        p.setStock(rs.wasNull() ? null : stock);

        float f = rs.getFloat("precio");                  // getFloat devuelve primitivo
        p.setPrecio(rs.wasNull() ? null : Float.valueOf(f)); // convertimos a wrapper si no es null

        return p;
    }

}

package ejemplo.repository.mysql;

import ejemplo.dao.DetallePedidoDAO;
import ejemplo.entity.DetallePedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDetallePedidoDAO implements DetallePedidoDAO {

    private final Connection cn;

    public MySQLDetallePedidoDAO(Connection cn) {
        this.cn = cn;
        crearTablasSiNoExisten();
    }

    private void crearTablasSiNoExisten() {
        // Asegura tablas referenciadas y crea detalles_pedido
        final String sqlPedidos = "CREATE TABLE IF NOT EXISTS pedidos (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "usuario_id BIGINT NOT NULL," +
                "estado VARCHAR(20) NOT NULL," +
                "total FLOAT DEFAULT 0," +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "INDEX idx_pedidos_usuario (usuario_id)," +
                "CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)" +
                ")";
        final String sqlProductos = "CREATE TABLE IF NOT EXISTS productos (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(120) NOT NULL," +
                "stock INT," +
                "precio FLOAT" +
                ")";
        final String sql = "CREATE TABLE IF NOT EXISTS detalles_pedido (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "pedido_id BIGINT NOT NULL," +
                "producto_id BIGINT NOT NULL," +
                "cantidad INT NOT NULL," +
                "precio_unitario FLOAT NOT NULL," +
                "subtotal FLOAT NOT NULL," +
                "INDEX idx_detalle_pedido (pedido_id)," +
                "CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE," +
                "CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos(id)" +
                ")";
        try (Statement st = cn.createStatement()) {
            st.execute(sqlProductos);
            st.execute(sqlPedidos);
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creando tabla 'detalles_pedido'", e);
        }
    }

    @Override
    public DetallePedido findById(Long id) {
        final String sql = "SELECT id, pedido_id, producto_id, cantidad, precio_unitario, subtotal FROM detalles_pedido WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findById(detalle)", e);
        }
    }

    @Override
    public List<DetallePedido> findByPedido(Long pedidoId) {
        final String sql = "SELECT id, pedido_id, producto_id, cantidad, precio_unitario, subtotal FROM detalles_pedido WHERE pedido_id=?";
        List<DetallePedido> out = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findByPedido(detalle)", e);
        }
        return out;
    }

    @Override
    public void create(DetallePedido d) {
        // Calcula subtotal si viene null
        if (d.getSubtotal() == null && d.getCantidad() != null && d.getPrecioUnitario() != null) {
            d.setSubtotal(d.getCantidad() * d.getPrecioUnitario());
        }
        final String sql = "INSERT INTO detalles_pedido (pedido_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, d.getPedidoId());
            ps.setLong(2, d.getProductoId());
            ps.setInt(3, d.getCantidad());
            ps.setFloat(4, d.getPrecioUnitario());
            ps.setFloat(5, d.getSubtotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) d.setId(keys.getLong(1)); }
        } catch (SQLException e) {
            throw new RuntimeException("Error en create(detalle)", e);
        }
    }

    @Override
    public void update(DetallePedido d) {
        if (d.getSubtotal() == null && d.getCantidad() != null && d.getPrecioUnitario() != null) {
            d.setSubtotal(d.getCantidad() * d.getPrecioUnitario());
        }
        final String sql = "UPDATE detalles_pedido SET pedido_id=?, producto_id=?, cantidad=?, precio_unitario=?, subtotal=? WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, d.getPedidoId());
            ps.setLong(2, d.getProductoId());
            ps.setInt(3, d.getCantidad());
            ps.setFloat(4, d.getPrecioUnitario());
            ps.setFloat(5, d.getSubtotal());
            ps.setLong(6, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en update(detalle)", e);
        }
    }

    @Override
    public void delete(Long id) {
        final String sql = "DELETE FROM detalles_pedido WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en delete(detalle)", e);
        }
    }

    @Override
    public void deleteByPedido(Long pedidoId) {
        final String sql = "DELETE FROM detalles_pedido WHERE pedido_id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, pedidoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en deleteByPedido(detalle)", e);
        }
    }

    @Override
    public void deleteAll() {
        try (Statement st = cn.createStatement()) {
            // Hijo primero
            st.executeUpdate("DELETE FROM detalles_pedido");
            st.executeUpdate("ALTER TABLE detalles_pedido AUTO_INCREMENT = 1"); // opcional
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando 'detalles_pedido'", e);
        }
    }

    private DetallePedido map(ResultSet rs) throws SQLException {
        DetallePedido d = new DetallePedido();
        d.setId(rs.getLong("id"));
        d.setPedidoId(rs.getLong("pedido_id"));
        d.setProductoId(rs.getLong("producto_id"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setPrecioUnitario(rs.getFloat("precio_unitario"));
        d.setSubtotal(rs.getFloat("subtotal"));
        return d;
    }
}


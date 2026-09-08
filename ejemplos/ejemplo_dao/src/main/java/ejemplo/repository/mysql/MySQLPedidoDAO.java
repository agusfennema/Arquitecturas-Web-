package ejemplo.repository.mysql;

import ejemplo.dao.PedidoDAO;
import ejemplo.dto.TopProducto;
import ejemplo.entity.Pedido;
import ejemplo.entity.PedidoEstado;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



public class MySQLPedidoDAO implements PedidoDAO {

    private final Connection cn;

    public MySQLPedidoDAO(Connection cn) {
        this.cn = cn;
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        // Asegura tabla usuarios (FK) y crea pedidos
        final String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(100) NOT NULL," +
                "email VARCHAR(120) NOT NULL UNIQUE," +
                "edad INT NULL" +
                ")";
        final String sql = "CREATE TABLE IF NOT EXISTS pedidos (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "usuario_id BIGINT NOT NULL," +
                "estado VARCHAR(20) NOT NULL," +
                "total FLOAT DEFAULT 0," +
                "INDEX idx_pedidos_usuario (usuario_id)," +
                "CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)" +
                ")";
        try (Statement st = cn.createStatement()) {
            st.execute(sqlUsuarios);
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creando tabla 'pedidos'", e);
        }
    }

    @Override
    public Pedido findById(Long id) {
        final String sql = "SELECT id, usuario_id, estado, total FROM pedidos WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findById(pedido)", e);
        }
    }

    @Override
    public List<Pedido> findAll() {
        final String sql = "SELECT id, usuario_id, estado, total FROM pedidos";
        List<Pedido> out = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error en findAll(pedido)", e);
        }
        return out;
    }

    @Override
    public List<Pedido> findByUsuario(Long usuarioId) {
        final String sql = "SELECT id, usuario_id, estado, total FROM pedidos WHERE usuario_id=?";
        List<Pedido> out = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findByUsuario(pedido)", e);
        }
        return out;
    }

    @Override
    public void create(Pedido p) {
        final String sql = "INSERT INTO pedidos (usuario_id, estado, total) VALUES (?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getUsuarioId());
            ps.setString(2, (p.getEstado() != null ? p.getEstado() : PedidoEstado.BORRADOR).name());
            if (p.getTotal() == null) ps.setNull(3, Types.FLOAT); else ps.setFloat(3, p.getTotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) p.setId(keys.getLong(1)); }
        } catch (SQLException e) {
            throw new RuntimeException("Error en create(pedido)", e);
        }
    }

    @Override
    public void update(Pedido p) {
        final String sql = "UPDATE pedidos SET usuario_id=?, estado=?, total=? WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, p.getUsuarioId());
            ps.setString(2, p.getEstado().name());
            if (p.getTotal() == null) ps.setNull(3, Types.FLOAT); else ps.setFloat(3, p.getTotal());
            ps.setLong(4, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en update(pedido)", e);
        }
    }

    @Override
    public void updateEstado(Long pedidoId, PedidoEstado nuevoEstado) {
        final String sql = "UPDATE pedidos SET estado=? WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setLong(2, pedidoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en updateEstado(pedido)", e);
        }
    }

    @Override
    public void updateTotal(Long pedidoId, Float nuevoTotal) {
        final String sql = "UPDATE pedidos SET total=? WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (nuevoTotal == null) ps.setNull(1, Types.FLOAT); else ps.setFloat(1, nuevoTotal);
            ps.setLong(2, pedidoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en updateTotal(pedido)", e);
        }
    }

    @Override
    public void delete(Long id) {
        final String sql = "DELETE FROM pedidos WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en delete(pedido)", e);
        }
    }

    @Override
    public void deleteAll() {
        try (Statement st = cn.createStatement()) {
            // Padre: NUNCA TRUNCATE con FKs. Usá DELETE y luego reseteo del AI si querés.
            st.executeUpdate("DELETE FROM pedidos");
            st.executeUpdate("ALTER TABLE pedidos AUTO_INCREMENT = 1"); // opcional
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando 'pedidos'", e);
        }
    }


    private Pedido map(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getLong("id"));
        p.setUsuarioId(rs.getLong("usuario_id"));
        String est = rs.getString("estado");
        p.setEstado(est == null ? PedidoEstado.BORRADOR : PedidoEstado.valueOf(est));
        float t = rs.getFloat("total");
        p.setTotal(rs.wasNull() ? null : t);
        return p;
    }



    @Override
    public Optional<TopProducto> findTopProductByUnits() {   // <--- TIPADO + @Override
        String sql = """
            SELECT p.nombre AS producto,
                   SUM(d.cantidad)                     AS unidades_vendidas,
                   SUM(d.cantidad * d.precio_unitario) AS ingreso_total
            FROM detalles_pedido d
            JOIN productos p ON p.id = d.producto_id
            GROUP BY d.producto_id
            ORDER BY unidades_vendidas DESC
            LIMIT 1
        """;
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return Optional.of(new TopProducto(
                        rs.getString("producto"),
                        rs.getLong("unidades_vendidas"),
                        rs.getBigDecimal("ingreso_total")
                ));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando top product", e);
        }
    }

}


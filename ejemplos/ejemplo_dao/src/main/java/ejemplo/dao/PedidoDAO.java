package ejemplo.dao;

import ejemplo.entity.Pedido;
import ejemplo.entity.PedidoEstado;
import ejemplo.dto.TopProducto;

import java.util.List;
import java.util.Optional;

public interface PedidoDAO {
    Pedido findById(Long id);
    List<Pedido> findAll();
    List<Pedido> findByUsuario(Long usuarioId);

    void create(Pedido p);
    void update(Pedido p);
    void updateEstado(Long pedidoId, PedidoEstado nuevoEstado);
    void updateTotal(Long pedidoId, Float nuevoTotal);
    void delete(Long id);

    void deleteAll();

    Optional<TopProducto> findTopProductByUnits();
}

package ejemplo.dao;

import ejemplo.entity.DetallePedido;

import java.util.List;

public interface DetallePedidoDAO {
    DetallePedido findById(Long id);
    List<DetallePedido> findByPedido(Long pedidoId);

    void create(DetallePedido d);
    void update(DetallePedido d);
    void delete(Long id);
    void deleteByPedido(Long pedidoId);

    void deleteAll();
}

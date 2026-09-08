package ejemplo.utils;

import ejemplo.dao.DetallePedidoDAO;
import ejemplo.dao.PedidoDAO;
import ejemplo.dao.ProductoDAO;
import ejemplo.dao.UsuarioDAO;
import ejemplo.factory.DAOFactory;

public class BorrarDatos {

    private final UsuarioDAO usuarioDAO;
    private final ProductoDAO productoDAO;
    private final PedidoDAO pedidoDAO;
    private final DetallePedidoDAO detallePedidoDAO;

    public BorrarDatos() {
        DAOFactory f = DAOFactory.getInstance(); // toma db.type o default MYSQL
        this.usuarioDAO       = f.createUsuarioDAO();
        this.productoDAO      = f.createProductoDAO();
        this.pedidoDAO        = f.createPedidoDAO();
        this.detallePedidoDAO = f.createDetallePedidoDAO();
    }

    public void run() {
        // Orden recomendado por FKs:
        // 1) Detalles (FK a pedidos y productos)
        // 2) Pedidos   (padre de detalles; además tiene FK a usuarios)
        // 3) Productos (referenciados por detalles)
        // 4) Usuarios  (padre de pedidos)
        try {
            detallePedidoDAO.deleteAll();
            pedidoDAO.deleteAll();
            productoDAO.deleteAll();
            usuarioDAO.deleteAll();
            System.out.println("Borrado completo de usuarios, productos, pedidos y detalles.");
        } catch (Exception e) {
            throw new RuntimeException("Error durante el borrado masivo.", e);
        }
    }
}

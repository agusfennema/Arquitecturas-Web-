package ejemplo.repository.mysql;

import java.sql.Connection;

import ejemplo.dao.DetallePedidoDAO;
import ejemplo.dao.PedidoDAO;
import ejemplo.dao.ProductoDAO;
import ejemplo.dao.UsuarioDAO;
import ejemplo.factory.DAOFactory;

public class MySQLDAOFactory extends DAOFactory { // <— EXTENDS DAOFactory

    /**
     * Implementacion MySQL del Factory Method de la conexion.
     * Toda la dependencia con MySQL (driver, URL, usuario, password) queda
     * encerrada en MySQLConnectionManager y solo esta clase lo conoce.
     */
    @Override
    protected Connection getConnection() {
        return MySQLConnectionManager.getInstance().getConnection();
    }

    /** Cierre especifico de MySQL: delega en su propio gestor de conexiones. */
    @Override
    protected void doShutdown() {
        MySQLConnectionManager.getInstance().shutdown();
    }

    @Override
    public UsuarioDAO createUsuarioDAO() {
        // Devuelve la implementación concreta MySQL de UsuarioDAO
        return new MySQLUsuarioDAO(getConnection());
    }


    @Override
    public ProductoDAO createProductoDAO() {
        // Devuelve la implementación concreta MySQL de ProductoDAO
        return new MySQLProductoDAO(getConnection());
    }

    @Override
    public PedidoDAO createPedidoDAO() {
        return new MySQLPedidoDAO(getConnection());
    }

    @Override
    public DetallePedidoDAO createDetallePedidoDAO() {
        return new MySQLDetallePedidoDAO(getConnection());
    }
}

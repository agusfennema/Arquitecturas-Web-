package ejemplo;

import ejemplo.dao.DetallePedidoDAO;
import ejemplo.dao.PedidoDAO;
import ejemplo.utils.BorrarDatos;
import ejemplo.utils.CargarDatosIniciales;
import ejemplo.dao.ProductoDAO;
import ejemplo.dao.UsuarioDAO;
import ejemplo.entity.Usuario;
import ejemplo.factory.DAOFactory;
import ejemplo.factory.DBType;

public class Main {

    /**
     * ======== ÚNICO PUNTO DE SWITCHEO ENTRE MOTORES ========
     *
     * Cambiá DBType.DERBY por DBType.MYSQL (o al revés) y TODA la aplicación
     * pasa a usar la otra base. No hay que tocar ninguna otra línea:
     * ni Main, ni BorrarDatos, ni CargarDatosIniciales, ni los DAO.
     *
     * Eso es lo que compra el Abstract Factory.
     */
    private static final DBType MOTOR = DBType.DERBY;

    public static void main(String[] args) {

        /*
         * ESTO TIENE QUE SER LO PRIMERO DE TODO.
         *
         * DAOFactory es un Singleton: el PRIMER getInstance() del proceso decide el motor
         * y los siguientes devuelven esa misma fábrica, ignorando lo que se les pase.
         * Como new BorrarDatos() ya pide la fábrica, si dejáramos el switcheo más abajo
         * llegaría tarde y la aplicación seguiría corriendo contra el motor por defecto.
         *
         * Publicamos el motor elegido como system property para que TODOS los que llamen
         * a DAOFactory.getInstance() sin parámetro resuelvan lo mismo.
         *
         * Si además se pasa -Ddb.type=MYSQL por línea de comandos, ese valor gana:
         * permite switchear sin recompilar.
         */
        System.setProperty("db.type", System.getProperty("db.type", MOTOR.name()));
        System.out.println("=== Motor de base de datos: " + System.getProperty("db.type") + " ===");

        new BorrarDatos().run();
        System.out.println("Listo.");

        new CargarDatosIniciales().run();
        System.out.println("Carga inicial finalizada.");


        DAOFactory f = DAOFactory.getInstance(); // resuelve segun db.type, ya fijado arriba

        UsuarioDAO usuarioDAO = f.createUsuarioDAO();
        ProductoDAO productoDAO=f.createProductoDAO();
        PedidoDAO pedidoDAO = f.createPedidoDAO();
        DetallePedidoDAO detallePedidoDAO = f.createDetallePedidoDAO();



        Usuario uno = usuarioDAO.findById(1L);
        System.out.println("findById: " + uno);

        uno.setEdad(37);
        usuarioDAO.update(uno);
        System.out.println("Actualizado: " + usuarioDAO.findById(uno.getId()));

        System.out.println("Todos los usuarios: " + usuarioDAO.findAll());

        System.out.println("Todos los productos: " + productoDAO.findAll());

        System.out.println("Todos los pedidos: " + pedidoDAO.findAll());

//        usuarioDAO.delete(uno.getId());
//        System.out.println("Borrado OK. Todos: " + usuarioDAO.findAll());

        pedidoDAO.findTopProductByUnits().ifPresentOrElse(
                top -> System.out.println("Producto más pedido: " + top.producto()
                        + " | Unidades: " + top.unidades()
                        + " | Total recaudado: $" + top.ingreso()),
                () -> System.out.println("No hay pedidos registrados.")
        );

        // Cierre de la base. Polimorfico: Main no sabe que motor hay debajo.
        // Con Derby esto es OBLIGATORIO (si no, la base queda en estado inconsistente).
        f.shutdown();

    }
}

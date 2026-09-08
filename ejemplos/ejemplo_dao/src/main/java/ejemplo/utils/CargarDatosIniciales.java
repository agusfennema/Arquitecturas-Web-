package ejemplo.utils;

import ejemplo.dao.DetallePedidoDAO;
import ejemplo.dao.PedidoDAO;
import ejemplo.dao.ProductoDAO;
import ejemplo.dao.UsuarioDAO;
import ejemplo.entity.DetallePedido;
import ejemplo.entity.Pedido;
import ejemplo.entity.PedidoEstado;
import ejemplo.entity.Producto;
import ejemplo.entity.Usuario;
import ejemplo.factory.DAOFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CargarDatosIniciales {

    private final UsuarioDAO usuarioDAO;
    private final ProductoDAO productoDAO;
    private final PedidoDAO pedidoDAO;
    private final DetallePedidoDAO detallePedidoDAO;

    public CargarDatosIniciales() {
        // Sin parámetro: toma el motor de la system property db.type, que Main fija al arrancar.
        // Así esta clase no queda atada a ningún motor concreto.
        DAOFactory f = DAOFactory.getInstance();
        this.usuarioDAO       = f.createUsuarioDAO();
        this.productoDAO      = f.createProductoDAO();
        this.pedidoDAO        = f.createPedidoDAO();
        this.detallePedidoDAO = f.createDetallePedidoDAO();
    }

    public void run() {
        cargarUsuarios("/data/usuarios.csv");      // nombre,email,edad
        cargarProductos("/data/productos.csv");    // nombre,stock,precio (float)
        cargarPedidos("/data/pedidos.csv");        // usuarioId,estado,total
        // Detalles: además de insertar, recalculamos totales por seguridad
        cargarDetallesYRecalcularTotales("/data/detalles_pedido.csv"); // pedidoId,productoId,cantidad,precioUnitario,subtotal
    }

    private void cargarUsuarios(String resourcePath) {
        try (InputStream is = mustGetResource(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line; boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.isBlank()) continue;
                String[] p = line.split(",", -1);

                String nombre = p[0].trim();
                String email  = p[1].trim();
                Integer edad  = p[2].trim().isEmpty() ? null : Integer.parseInt(p[2].trim());

                usuarioDAO.create(new Usuario(null, nombre, email, edad));
            }
            System.out.println("Usuarios cargados OK.");
        } catch (Exception e) {
            throw new RuntimeException("Error cargando usuarios desde " + resourcePath, e);
        }
    }

    private void cargarProductos(String resourcePath) {
        try (InputStream is = mustGetResource(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line; boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.isBlank()) continue;
                String[] p = line.split(",", -1);

                String nombre = p[0].trim();
                Integer stock = p[1].trim().isEmpty() ? null : Integer.parseInt(p[1].trim());
                Float precio  = p[2].trim().isEmpty() ? null : Float.parseFloat(p[2].trim());

                Producto prod = new Producto(null, nombre, stock, precio);
                productoDAO.create(prod);
            }
            System.out.println("Productos cargados OK.");
        } catch (Exception e) {
            throw new RuntimeException("Error cargando productos desde " + resourcePath, e);
        }
    }

    private void cargarPedidos(String resourcePath) {
        try (InputStream is = mustGetResource(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line; boolean first = true;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.isBlank()) continue;

                String[] p = line.split(",", -1);

                Long usuarioId      = Long.parseLong(p[0].trim());
                String estadoStr    = p[1].trim().toUpperCase();
                Float total         = p[2].trim().isEmpty() ? null : Float.parseFloat(p[2].trim());

                Pedido pedido = new Pedido(null, usuarioId, PedidoEstado.valueOf(estadoStr), total);
                pedidoDAO.create(pedido);
                count++;
            }
            System.out.println("Pedidos cargados OK. Total filas: " + count);
        } catch (Exception e) {
            throw new RuntimeException("Error cargando pedidos desde " + resourcePath, e);
        }
    }

    /**
     * Inserta detalles, acumula subtotales por pedido y luego recalcula el total de cada pedido en DB.
     */
    private void cargarDetallesYRecalcularTotales(String resourcePath) {
        Map<Long, Float> totalesPorPedido = new HashMap<>();
        try (InputStream is = mustGetResource(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line; boolean first = true;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.isBlank()) continue;

                String[] p = line.split(",", -1);

                Long pedidoId       = Long.parseLong(p[0].trim());
                Long productoId     = Long.parseLong(p[1].trim());
                Integer cantidad    = Integer.parseInt(p[2].trim());
                Float precioUnit    = Float.parseFloat(p[3].trim());
                Float subtotal      = Float.parseFloat(p[4].trim());

                DetallePedido d = new DetallePedido(null, pedidoId, productoId, cantidad, precioUnit, subtotal);
                detallePedidoDAO.create(d);

                // acumula totales
                totalesPorPedido.merge(pedidoId, subtotal, Float::sum);
                count++;
            }

            // Recalcular y actualizar total por pedido (blinda coherencia)
            for (Map.Entry<Long, Float> e : totalesPorPedido.entrySet()) {
                pedidoDAO.updateTotal(e.getKey(), e.getValue());
            }
            System.out.println("Detalles cargados OK. Total filas: " + count + ". Totales de pedido recalculados.");
        } catch (Exception e) {
            throw new RuntimeException("Error cargando detalles desde " + resourcePath, e);
        }
    }

    // --- util ---
    private InputStream mustGetResource(String path) {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) throw new IllegalArgumentException("Recurso no encontrado: " + path);
        return is;
    }
}

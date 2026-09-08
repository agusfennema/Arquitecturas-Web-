package ejemplo.repository.derby;

import ejemplo.factory.ConnectionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Equivalente Derby de ejemplo.factory.ConnectionManager.
 *
 * Diferencias con MySQL (esto es lo que justifica tener DOS managers):
 *  - Driver embebido: org.apache.derby.jdbc.EmbeddedDriver (no hay servidor que levantar).
 *  - URL con ";create=true" en vez de "?createDatabaseIfNotExist=true".
 *  - No hay usuario ni password: la base es una carpeta en disco.
 *  - Requiere un shutdown explicito al terminar, o la base queda marcada como sucia.
 */
public final class DerbyConnectionManager implements ConnectionManager {

    private static volatile DerbyConnectionManager instance;
    private Connection connection;

    // --- Configuración de conexión ---
    // La base se crea como una CARPETA llamada "derby_dao_DB" en el directorio de trabajo del proyecto.
    private static final String URL = "jdbc:derby:derby_dao_DB;create=true";

    // --- Constructor privado ---
    private DerbyConnectionManager() {
        try {
            // Registrar el driver embebido de Derby
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

            // Establecer la conexión (Derby embebido no lleva user/password)
            this.connection = DriverManager.getConnection(URL);
            System.out.println("Conexión establecida correctamente con Derby (embebido).");

        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver de Derby. ¿Agregaste la dependencia al pom.xml?");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos Derby.");
            e.printStackTrace();
        }
    }

    // --- Singleton Thread-Safe (mismo DCL que ConnectionManager) ---
    public static DerbyConnectionManager getInstance() {
        if (instance == null) {                                 // 1er chequeo
            synchronized (DerbyConnectionManager.class) {        // Bloque sincronizado
                if (instance == null) {                         // 2do chequeo
                    instance = new DerbyConnectionManager();
                }
            }
        }
        return instance;
    }

    // --- Retornar la conexión ---
    @Override
    public Connection getConnection() {
        return connection;
    }

    /**
     * Derby embebido NECESITA este apagado explícito antes de terminar el programa.
     * Si no se llama, la base queda en estado inconsistente y el próximo arranque puede fallar.
     * Derby siempre lanza una SQLException al apagarse correctamente (SQLState "XJ015"):
     * eso NO es un error, es la forma que tiene de avisar que el shutdown salió bien.
     *
     * Compará con MySQLConnectionManager.shutdown(): mismo contrato, procedimiento distinto.
     * Por eso shutdown() forma parte de la interfaz ConnectionManager.
     */
    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión con Derby: " + e.getMessage());
        }
        connection = null;

        try {
            // Apaga el motor embebido completo (no solo esta base)
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if ("XJ015".equals(e.getSQLState())) {
                System.out.println("Derby se apagó correctamente.");
            } else {
                System.err.println("Error al apagar Derby: " + e.getMessage());
            }
        }

        synchronized (DerbyConnectionManager.class) {
            instance = null;
        }
    }

}

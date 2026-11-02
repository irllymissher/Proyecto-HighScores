package server.db;
/**
 *
 * @author MAZ
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
//
public final class DBComponent  {

  static private final String CLASS_NAME = DBComponent.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
   
  // Singleton: solo se va a instanciar un objeto de clase DBComponent
  static private DBComponent SINGLETON = null;

  // Conexión con la BBDD: queda abierta hasta la parada 
  private final Connection connection;
  // Generador random compartido por clases que operan con BBDD.
  private final SecureRandom rg;

  //////////////////////////////////////////////////////
  // Método que arranca infraestructura de componente //
  //////////////////////////////////////////////////////
  static public void start () throws Exception {
    
    // 
    if (SINGLETON != null)
      return;

    // Instancición de singleton
    final Object lock = new  Object();
    synchronized (lock) {
    
      if (SINGLETON == null) {       
        SINGLETON = new DBComponent();
        LOGGER.info("conexión abierta con BBDD");
      }

    }
    
  }

  //////////////////////////////////////////////////////
  // Método que detiene infraestructura de componente //
  //////////////////////////////////////////////////////
  static public void stop () {
    // Cierre de la conexión con la BBDD.
    if ((SINGLETON != null) && (SINGLETON.connection != null)) {
      try {
        SINGLETON.connection.close();
      } catch (final SQLException ex) {
        LOGGER.warning("problema al realizar deconexión con base de datos");      
        LOGGER.warning(ex.toString());
      }
    }
  }
  
  //////////////////////////////////////////////////////////////
  // Método que proporciona conexión a operaciones sobre BBDD //
  //////////////////////////////////////////////////////////////
  static Connection getConnection () { return SINGLETON.connection; }  
  
  //////////////////////////////////////////////////////////
  // Método que proporciona el generador random a emplear //
  //////////////////////////////////////////////////////////
  static SecureRandom getRandomGenerator () { return SINGLETON.rg; }    

  // Constructor privado
  private DBComponent () throws IOException, SQLException, NoSuchAlgorithmException {
    // Instanciación del generador random
    try {
      this.rg = SecureRandom.getInstance("NativePRNGNonBlocking");
    } catch (NoSuchAlgorithmException ex) {
      LOGGER.log(Level.SEVERE, "algoritmo para generador random no encontrado", ex);
      LOGGER.log(Level.SEVERE, "plataforma no compatible");
      throw ex;
    }
    // Usuario y contraseña de acceso a la BBDD
    final String user;
    final String password;
    final FileSystem fileSystem = FileSystems.getDefault();
    final Path pathToDatabasePassword = fileSystem.getPath("etc", "db.password");
    try (final Scanner is = new Scanner(pathToDatabasePassword.toFile())) {
      user = is.nextLine();
      password = is.nextLine();
    } catch (final FileNotFoundException ex) {
      LOGGER.warning("no se encuentra fichero con contraseña de base de datos");
      LOGGER.warning(ex.toString());      
      throw ex;
    }
    // Conexión con la BBDD; es única y se mantiene abierta durante toda la ejecución.
    // Una conexión es capaz de atender peticiones concurrentes (simultáneas).
    try {
      final Path pathToDB = fileSystem.getPath("data", "server", "database", "highscoresDB");
      DriverManager.setLoginTimeout(10);      
      final String urlDB = "jdbc:h2:file:" + pathToDB.toFile().getCanonicalFile();
      this.connection = DriverManager.getConnection(urlDB, user, password);
    } catch (final IOException ex) {
      LOGGER.warning("problema con ubicación de base de datos");
      LOGGER.warning(ex.toString());
      throw ex;
    } catch (final SQLTimeoutException ex) {
      LOGGER.warning("retraso al establecer conexión con base de datos");
      LOGGER.warning(ex.toString());
      throw ex;
    } catch (final SQLException ex) {
      LOGGER.warning("problema al establecer conexión con base de datos");
      LOGGER.warning(ex.toString());
      throw ex;
    }
  }

}

package server.handler;

/**
 *
 * @author MAZ
 */
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
//

public final class RequestComponent {

  static private final String CLASS_NAME = RequestComponent.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

  static private final int SERVER_PORT = 9876;
  static private final int MIN_NUM_THREADS = 2;
  static private final int MAX_NUM_THREADS = 10;

  static private final String PLAYER_ENDPOINT = "/player";
  static private final String SCORE_ENDPOINT = "/score";
  static private final String GAME_ENDPOINT = "/game";

  // Singleton: solo se va a instanciar un objeto de clase RequestComponent.
  static private RequestComponent COMPONENT = null;

  // Servidor HTTP: una vez arrancado, queda a la escucha hasta su detención.
  private final HttpServer httpServer;

  //////////////////////////////////////////////////////
  // Método que arranca infraestructura de componente //
  //////////////////////////////////////////////////////
  static public void start () throws Exception
  {
    start(SERVER_PORT, MIN_NUM_THREADS);
  }

  //////////////////////////////////////////////////////
  // Método que arranca infraestructura de componente //
  //////////////////////////////////////////////////////
  static public void start (final int port, final int nthreads) throws Exception
  {

    // 
    if (COMPONENT != null)
    {
      return;
    }

    if (nthreads <= 0)
    {
      throw new IllegalArgumentException("Número de hebras debe ser positivo");
    }
    else if (nthreads > MAX_NUM_THREADS)
    {
      throw new IllegalArgumentException("Número de hebras no debe ser mayor que " + MAX_NUM_THREADS);
    }

    // Instancición de singleton
    final Object lock = new Object();
    synchronized (lock)
    {

      if (COMPONENT == null)
      {
        COMPONENT = new RequestComponent(port, nthreads);
      }

    }

  }

  //////////////////////////////////////////////////////
  // Método que detiene infraestructura de componente //
  //////////////////////////////////////////////////////
  static public void stop ()
  {
    if (COMPONENT == null)
    {
      return;
    }
    COMPONENT.httpServer.stop(0);
    final ExecutorService executor = (ExecutorService) COMPONENT.httpServer.getExecutor();
    executor.shutdown();
    // Bucle que espera hasta terminar todas las tareas en curso y encoladas.
    // Evita que el proceso activador de la infraestructura finalice antes
    // de que se hayan atendido todas las tareas aceptadas.
    boolean isTerminated;
    do
    {
      try
      {
        // Consulta cada segundo si todavía quedan tareas activas.
        isTerminated = executor.awaitTermination(1000, MILLISECONDS);
      }
      catch (final InterruptedException ex)
      {
        LOGGER.log(Level.SEVERE, "{0}", ex);
        return;
      }
    } while (!isTerminated);
    COMPONENT = null;
    LOGGER.info("tareas pendientes completadas");
    LOGGER.info("infraestuctura de peticiones detenida");
  }

  // Constructor privado
  private RequestComponent (final int port, final int nthreads) throws IOException
  {
    try
    {
      // Arranque del servicio de escucha.
      final InetSocketAddress hostAddress
                              = new InetSocketAddress(InetAddress.getLocalHost(), port);
      this.httpServer = HttpServer.create(hostAddress, 0);
      // Servicio ejecutor con hebras de trabajo
      final ExecutorService executor = Executors.newFixedThreadPool(nthreads);
      httpServer.setExecutor(executor);
      // Creación de contextos y asociación de manejadores
      httpServer.createContext(PLAYER_ENDPOINT, new PlayerRequestHandler());
      httpServer.createContext(SCORE_ENDPOINT, new ScoreRequestHandler());
      httpServer.createContext(GAME_ENDPOINT, new GameRequestHandler());
      // Una vez configurado el servidor HTTP, se arranca.
      httpServer.start();
      LOGGER.log(Level.INFO, "servidor HTTP activo por puerto {0}", String.valueOf(SERVER_PORT));
    }
    catch (final IOException ex)
    {
      LOGGER.log(Level.SEVERE, "no se ha podido arrancar el servicio", ex);
      throw ex;
    }
  }

}

package client.request;
/**
 *
 * @author MAZ
 */
import static java.net.HttpURLConnection.HTTP_OK;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
//
public final class GameList {
  
  static private final String CLASS_NAME = GameList.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);   
  
  private final InetAddress serverIP;
  private final int port;  
  
  static private final String ENDPOINT = "/game";
  
  public GameList (final InetAddress serverIP, final int port) {
    this.serverIP = serverIP;
    this.port = port;
  }
  
  public List<String> request () {

    // URI de petición
    final URI uri;
    try {
      final String protocol = "http";
      final String userInfo = null;
      final String host = serverIP.getHostAddress();
      final String path = ENDPOINT;
      final String query = null;
      final String fragment = null;
      uri = new URI(protocol, userInfo, host, this.port, path, query, fragment);
    } catch (final URISyntaxException ex) {
      LOGGER.log(Level.WARNING, "URI de servicio mal formada");
      return new ArrayList<>();
    }

    try {
      
      // Cliente con el que realizar las peticiones
      final HttpClient httpClient = HttpClient.newBuilder()
              .version(HttpClient.Version.HTTP_2)
              .build();      
      
      // Instanciación de request (GET)
      final HttpRequest request = HttpRequest.newBuilder()
              .GET()
              .uri(uri)
              .build();

      final HttpResponse<Stream<String>> response =
              httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
      
      if (response.statusCode() == HTTP_OK) {

        // La respuesta es una lista de líneas, cada
        // una con el nombre de un juego diferente.
        final List<String> gameList = new ArrayList<>();
        try (Stream<String> games = response.body()) {
          games.forEach(game -> gameList.add(game));
        }
        return gameList;

      }

    } catch (final IOException ex) {
      LOGGER.log(Level.INFO, "problema de I/O en conexión con servicio HTTP", ex);
    } catch (final InterruptedException ex) {
      LOGGER.log(Level.INFO, "operación interrumpida", ex);
    }

    return new ArrayList<>();

  }  
  
}

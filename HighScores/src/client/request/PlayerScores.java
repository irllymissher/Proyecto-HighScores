package client.request;
/**
 *
 * @author MAZ
 */
import static java.net.HttpURLConnection.HTTP_OK;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
//
public final class PlayerScores {
  
  static private final String CLASS_NAME = PlayerScores.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);   
  
  private final InetAddress serverIP;
  private final int port;  
  
  static private final String ENDPOINT = "/score";
  static private final String QUERY = "playername=";
  
  public PlayerScores (final InetAddress serverIP, final int port) {
    this.serverIP = serverIP;
    this.port = port;
  }
  
  public Map<String, Integer> request (final String playername) {

    // URI de petición
    final URI uri;
    try {
      final String protocol = "http";
      final String userInfo = null;
      final String host = serverIP.getHostAddress();
      final String path = ENDPOINT;
      final String query = QUERY + playername;
      final String fragment = null;
      uri = new URI(protocol, userInfo, host, this.port, path, query, fragment);
    } catch (final URISyntaxException ex) {
      LOGGER.log(Level.WARNING, "URI de servicio mal formada");
      return new HashMap<>();
    }

    try {
      
      final HttpClient httpClient = HttpClient.newBuilder()
              .version(HttpClient.Version.HTTP_2)
              .build();
      
      // Instanciación de request (GET)
      final HttpRequest request = HttpRequest.newBuilder()
              .GET()
              .uri(uri)
              .build();

      final HttpResponse<InputStream> response =
              httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
      
      if (response.statusCode() == HTTP_OK) {
        
        final Map<String, Integer> scores = new LinkedHashMap<>();

        try (Scanner reader = new Scanner(response.body())) {
          
          while (reader.hasNext()) {
          
            reader.next("gamename"); // Si no existe, ocurre excepción NoSuchElementException
            reader.next(":");
            final String gamename = reader.nextLine().trim();

            reader.next("score"); // Si no existe, ocurre excepción NoSuchElementException
            reader.next(":");          
            final int score = reader.nextInt();
            reader.nextLine();

            scores.put(gamename, score);
          
          }
          
        } catch (final NoSuchElementException ex) {
          LOGGER.log(Level.INFO, "formato de respuesta inválido", ex);
          scores.clear();
        }
        
        return scores;

      }

    } catch (final IOException ex) {
      LOGGER.log(Level.INFO, "problema de I/O en conexión con servicio HTTP", ex);
    } catch (final InterruptedException ex) {
      LOGGER.log(Level.INFO, "operación interrumpida", ex);
    }

    return new HashMap<>();

  }
  
}
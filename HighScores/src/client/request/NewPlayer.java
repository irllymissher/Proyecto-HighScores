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
import java.util.logging.Level;
import java.util.logging.Logger;
//
public final class NewPlayer {
  
  static private final String CLASS_NAME = NewPlayer.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);   
  
  private final InetAddress serverIP;
  private final int port;  
  
  static private final String ENDPOINT = "/player";
  
  public NewPlayer (final InetAddress serverIP, final int port) {
    this.serverIP = serverIP;
    this.port = port;
  }
  
  public boolean request (final String playername) {

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
      return false;
    }

    try {
      
      final HttpClient httpClient = HttpClient.newBuilder()
              .version(HttpClient.Version.HTTP_2)
              .build();      
      
      // Instanciación de request (PUT)
      final String body = "playername : " + playername;
      final HttpRequest request = HttpRequest.newBuilder()
              .PUT(HttpRequest.BodyPublishers.ofString(body))
              .uri(uri)
              .build();

      final HttpResponse<InputStream> response =
              httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() == HTTP_OK) {

        try (DataInputStream dis = new DataInputStream(response.body())) {
          return dis.readBoolean();
        }

      }

    } catch (final IOException ex) {
      LOGGER.log(Level.INFO, "problema de I/O en conexión con servicio HTTP", ex);
    } catch (final InterruptedException ex) {
      LOGGER.log(Level.INFO, "operación interrumpida", ex);
    }

    return false;

  }
  
}

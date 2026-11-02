package client.request;
/**
 *
 * @author MAZ
 */
import java.io.DataInputStream;
import static java.net.HttpURLConnection.HTTP_OK;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
//
public final class NewScore {
  
  static private final String CLASS_NAME = NewScore.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);   
  
  private final InetAddress serverIP;
  private final int port;  
  
  static private final String ENDPOINT = "/score";
  
  public NewScore (final InetAddress serverIP, final int port) {
    this.serverIP = serverIP;
    this.port = port;
  }
  
  public boolean request (final String playername,
                          final String gamename,
                          final int score) {

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
      
      final InputStream is = getBodyStream(playername, gamename, score);
      
      // Instanciación de request (POST)
       final HttpRequest request = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofInputStream(() -> is))
              .uri(uri)
              .build();

        final HttpResponse<InputStream> response
                = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
              

        if (response.statusCode() == HTTP_OK) {

          try (DataInputStream _is = new DataInputStream(response.body())) {
            return _is.readBoolean();
          }

        }

    } catch (final IOException ex) {
      LOGGER.log(Level.INFO, "problema de I/O en conexión con servicio HTTP", ex);
    } catch (final InterruptedException ex) {
      LOGGER.log(Level.INFO, "operación interrumpida", ex);
    }

    return false;

  }
  
  // RECURSO TÉCNICO: como alternativa al empleo de objetos ByteArrayStream
  // para proporcionar el contenido del cuerpo de la petición, se pueden emplear
  // objetos PipedStream (un stream de entrada conectado a un stream de salida).
  // 1. Se escriben los datos sobre el stream de salida, que está enchufado
  //    al stream de entrada.
  // 2. El contenido del stream de entrada se proporciona al publicador de cuerpo
  //    de la petición.
  // 3. El publicador de cuerpo leerá el contenido escrito sobre el stream de salida.  
  private InputStream getBodyStream (final String playername,
                                     final String gamename,
                                     final int score) {

    try {
      // InputStream con datos de cuerpo de petición
      final PipedInputStream in = new PipedInputStream();
      final PipedOutputStream out = new PipedOutputStream(in);
      ForkJoinPool.commonPool().submit(() -> {
      try (OutputStreamWriter writer = new OutputStreamWriter(out)) {

          writer.write("playername : " + playername);
          writer.write("\n");
          writer.write("gamename : " + gamename);
          writer.write("\n");
          writer.write("score : " + score);
          writer.write("\n");
          
        } catch (final IOException ex) {
          LOGGER.log(Level.INFO, "problema de I/O en formación de cuerpo de petición", ex);
        }
      });
      return in;
    } catch (final IOException ex) {
      LOGGER.log(Level.INFO, "problema de I/O en formación de cuerpo de petición", ex);
      return null;
    }

  }
  
//  private InputStream getBodyStream (final String playername,
//                                     final String gamename,
//                                     final int score) {
//
//    // InputStream con datos de cuerpo de petición
//    final ByteArrayOutputStream os = new ByteArrayOutputStream();
//    try (OutputStreamWriter writer = new OutputStreamWriter(os)) {
//        writer.write("playername : " + playername);
//        writer.write("\n");
//        writer.write("gamename : " + gamename);
//        writer.write("\n");
//        writer.write("score : " + score);
//        writer.write("\n");
//    } catch (final IOException ex) {}
//    return new ByteArrayInputStream(os.toByteArray());
//  }  
  
}
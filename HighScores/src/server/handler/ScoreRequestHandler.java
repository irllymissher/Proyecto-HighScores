package server.handler;
/**
 *
 * @author MAZ
 */
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
//
import server.db.Scores;
//
public final class ScoreRequestHandler implements HttpHandler {
  
  static private final String CLASS_NAME = ScoreRequestHandler.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
  
  static private final int NO_CONTENT = -1;

  @Override    
  public void handle (final HttpExchange exchange) throws IOException {
  
    final String requestMethodName = exchange.getRequestMethod();
    LOGGER.info(requestMethodName);    
        
    if ("POST".compareTo(requestMethodName) == 0) {
      LOGGER.info("POST recibido");
      // Solicitud de inserción de nueva puntuación
      handlePostRequest(exchange);
    } else if ("GET".compareTo(requestMethodName) == 0) {
      LOGGER.info("GET recibido");
      // Solicitud de listado de puntuaciones
      handleGetRequest(exchange);
    } else {
      // Servicio no atiende otros tipos de requests.
      exchange.sendResponseHeaders(HTTP_BAD_REQUEST, NO_CONTENT);
    }

  }

  private void handlePostRequest (final HttpExchange exchange) {
    
    // TODO

  }
  
  private void handleGetRequest (final HttpExchange httpExchange) {
    
    final URI uri = httpExchange.getRequestURI();
    final String queryGroup = uri.getQuery();
    final String[] parts = queryGroup.split("=");
    
    LOGGER.info(queryGroup);
    
    try {
      if ("playername".compareTo(parts[0]) == 0)
        getPlayerScores(httpExchange, parts[1]);
      else if ("gamename".compareTo(parts[0]) == 0)
        getGameScores(httpExchange, parts[1]);
      else {
        httpExchange.sendResponseHeaders(HTTP_BAD_REQUEST, NO_CONTENT);
      }
    } catch (final IOException ex) {
      LOGGER.log(Level.WARNING, "problema de I/O en respuesta", ex);
    }
    
  }
  
  private void getPlayerScores (final HttpExchange exchange,
                                final String playername) {

    // TODO

  }
  
  private void getGameScores (final HttpExchange exchange,
                              final String gamename) {

    // TODO

  }    

}
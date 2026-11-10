package server.handler;
/**
 *
 * @author MAZ
 */
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
//
import server.db.Games;
//
public final class GameRequestHandler implements HttpHandler {

  static private final String CLASS_NAME = GameRequestHandler.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

  static private final int NO_CONTENT = -1;

  @Override
  public void handle (final HttpExchange exchange) throws IOException {

    final String requestMethodName = exchange.getRequestMethod();

    if ("PUT".compareTo(requestMethodName) == 0) {
      LOGGER.info("PUT recibido");
      // Inserción de nuevo juego
      handlePutRequest(exchange);
    } else if ("GET".compareTo(requestMethodName) == 0) {
      LOGGER.info("GET recibido");
      // Consulta de juegos
      handleGetRequest(exchange);
    } else {
      // Servicio no atiende otros tipos de requests.
      exchange.sendResponseHeaders(HTTP_BAD_REQUEST, NO_CONTENT);
    }

  }

  private void handlePutRequest (final HttpExchange exchange) {

    final Games g = new Games();
    final boolean resultadoDeAlta;
    
    try ( Scanner bodyScanner=
            new Scanner(
                    exchange.getRequestBody(), 
                    StandardCharsets.UTF_8.name()) ) 
    {
      
      final String lineaCuerpo= bodyScanner.nextLine();
      final String lineaNormalizada= Normalizer.normalize(
              lineaCuerpo, 
              Normalizer.Form.NFKC);

      try ( Scanner lineScanner=
              new Scanner(lineaNormalizada) ) 
      {
        final String nombreDespuesDeTRim;
        final String nombreAntesDeTrim;   
        
        lineScanner.next("gamename");
        lineScanner.next(":");
        
        nombreAntesDeTrim = lineScanner.nextLine();
        nombreDespuesDeTRim = nombreAntesDeTrim.trim();

        if ( nombreDespuesDeTRim.isEmpty() ) 
        {
          throw new NoSuchElementException("Nombre de juego vacio tras limpiar espacios");
        }

        resultadoDeAlta = g.newGame(nombreDespuesDeTRim);
      }
    }
    catch ( final NoSuchElementException e) 
    {
      logging(
              Level.WARNING, 
              e, exchange, 
              "Param incorrectos para PUT en juegos");
      responderSinContenido(exchange);
      return;
    }
    catch ( final SQLException e) 
    {
        logging(
                Level.WARNING, 
                e, exchange, 
                "falo en la bbd al dar de alta al juego");
        responderSinContenido(exchange);
        return;
    }
    try 
    {
        exchange.sendResponseHeaders(HTTP_OK, 0);
        try (OutputStream os= 
                exchange.getResponseBody();
             DataOutputStream dos = new DataOutputStream(os)) 
        {
            dos.writeBoolean(resultadoDeAlta);
        }
    } 
    catch( final IOException e )
    {
        logging(
                Level.WARNING, 
                e, exchange, 
                "Error de i/o al escribir respuesta en PUT");
    }
  }

  private void handleGetRequest (final HttpExchange exchange) {

    // TODO

  }

  private void responderSinContenido(final HttpExchange exchange) 
  {
    try 
    {
        exchange.sendResponseHeaders(HTTP_NO_CONTENT, NO_CONTENT);
    } catch (IOException e) 
    {
        logging(
                Level.WARNING, 
                e, exchange, 
                "No se pudo enviar el codigo 204 al cliente");
    }
  }
  private void logging (
          final Level level, 
          final Exception ex, 
          final HttpExchange exchange, 
          final String eventDescription) 
  {
      final String mensaje = String.format(
              "%s - %s - %s - %s", 
              exchange.getRemoteAddress().toString(), 
              exchange.getRequestMethod(), 
              exchange.getRequestURI().getPath(),   /* scores atiende dos get */
              eventDescription);
      
      LOGGER.log(
              level, 
              mensaje, 
              ex);
  }
  
}
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
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.NoSuchElementException;

//
import server.db.Players;
//
public final class PlayerRequestHandler implements HttpHandler {

  static private final String CLASS_NAME = PlayerRequestHandler.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

  static private final int NO_CONTENT = -1;

  @Override
  public void handle (final HttpExchange exchange) throws IOException {

    final String requestMethodName = exchange.getRequestMethod();
    if ("PUT".compareTo(requestMethodName) == 0) {
      LOGGER.info("PUT recibido");
      // Solicitud de inserción de nuevo jugador
      handlePutRequest(exchange);
    } else if ("GET".compareTo(requestMethodName) == 0) {
      LOGGER.info("GET recibido");
      // Solicitud de listado de jugadores
      handleGetRequest(exchange);
    } else {
      // Servicio no atiende otros tipos de requests.
      exchange.sendResponseHeaders(HTTP_BAD_REQUEST, NO_CONTENT);
    }

  }

  
  private void handlePutRequest (final HttpExchange exchange) throws SQLException {

    // TODO
    
    /**
     * 1) Lectura
     * 2) Procesar
     * 3) Respuesta
     */
    
    final Players p = new Players();
    final boolean resultadoDeAlta;
    
    try (Scanner bodyScanner= 
            new Scanner(
                    exchange.getRequestBody(), 
                    StandardCharsets.UTF_8.name())) 
    {
        final String lineaJson = bodyScanner.nextLine();
        final String lineaNormalizada = Normalizer.normalize(
                lineaJson, 
                Normalizer.Form.NFKC);

        try (Scanner lineScanner= 
                new Scanner(lineaNormalizada)) 
        {
            lineScanner.next("playername");
            lineScanner.next(":");
            final String nombre;
            nombre=lineScanner.next();

            if (lineScanner.hasNext()) 
            {
                throw new NoSuchElementException("Residuo");
            }
            resultadoDeAlta = p.newPlayer(nombre);
        }
    } 
    catch(NoSuchElementException e) 
    {
        logging(Level.WARNING, e, exchange, "Parametros incorrectos en PUT");
        responderSinContenido(exchange);
        return;
    }
    catch(SQLException e) 
    {
        logging(Level.SEVERE, e, exchange, "Fallo en bbdd al dar de alta al juegador nuevo");
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
    catch( IOException e )
    {
        logging(
                Level.SEVERE, 
                e, exchange, 
                "Error de entrada/salida al escribir respuesta en PUT");
    }
  }
  
  /**
   * El error 204 envia http no content y no content y esa llamada lanza IOException
   * Los metodos que tenemos que realizar vienen sin throws
   * Cada catch que responde el 204 necesita su propio bloque try-catch y para ahorrarnos
   * el lio podemos crear este metodo 
   * 
   * PARA REVISAR: el profesor @MAZ en el netbeans de SortingHandler realiza el catch
   * directamente porque su handler declara throws IOExcepcion, pero los metodos
   * de este proyecot no lo hacen
   * @param exchange 
   */
  private void responderSinContenido(final HttpExchange exchange) 
  {
    try 
    {
        exchange.sendResponseHeaders(HTTP_NO_CONTENT, NO_CONTENT);
    } catch (IOException e) 
    {
        logging(
                Level.SEVERE, 
                e, exchange, 
                "No se pudo enviar el codigo 204 al cliente");
    }
  }

  private void handleGetRequest (final HttpExchange exchange) {

    // TODO
    final Players p= new Players();
    try 
    {
        final List<String> listaDeJugadores= p.getPlayers();
        exchange.sendResponseHeaders(HTTP_OK,0);
        try(final OutputStream os=exchange.getResponseBody();
                final DataOutputStream dos = new DataOutputStream(os))
        {
            dos.writeInt(listaDeJugadores.size());
            for (final String j : listaDeJugadores) 
            {
                dos.writeUTF(j);
            }
        }
    } 
    catch (SQLException ex) 
    {
          logging(
                  Level.WARNING, 
                  ex, 
                  exchange, 
                  "Fallo en bbdd al obtener el listado de jugadores");
          responderSinContenido(exchange);
    } 
    catch( IOException ex )
    {
        Logger.getLogger(
                PlayerRequestHandler.class.getName()).log(
                        Level.SEVERE, 
                        null, 
                        ex);
    }

  }
  
  /**
   * Informacion buscada:
   *    > https://stackoverflow.com/questions/5950557/good-examples-using-java-util-logging
   *    > https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpExchange.html
   *    > https://stackoverflow.com/questions/7036765/how-to-i-format-my-log-message
   *    > https://docs.astral.sh/ruff/rules/logging-string-format/
  */
  private void logging (
          final Level level, 
          final Exception ex, 
          final HttpExchange exchange, 
          final String eventDescription) 
  {
      final String mensaje = String.format(
              "%s - %s - %s", 
              exchange.getRemoteAddress().toString(), 
              exchange.getRequestMethod(), 
              exchange.getRequestURI().getPath(),   /* scores atiende dos get */
              eventDescription);
      
      LOGGER.log(level, mensaje, ex);
  }

}
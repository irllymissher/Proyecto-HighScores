/**
 * Todos los cambios estan realizados estan en mi GitHub.
 *
 * Ahi se puede revisar el historial de decisiones y correciones que he ido
 * tomando a lo largo de este proyecto para que el servicio pudiera funcionar
 * como pide el enunciado de la practica
 *
 * He intentado ser coherente con la teoria vista en clase y con los ejemplos de
 * NetBeans que MAZ explicó, siguiente las formas estandar de programar y usar
 * los metodos que se usaron en los pertinentes lugares. En los comentarios de
 * cada metodo explico el porqué de cada decisión.
 *
 *
 * Fuentes internas:
 *  - Enunciado:
 *    p.4 (lectura del cuerpo)
 *    p.7 (registro y excepciones)
 *    p.8 (códigos de respuesta)
 *  - Tema 2b: 
 *    p.2 (rechazar entradas inválidas)
 *    p.6 (NFKC)
 *    p.7 (charset explícito)
 *  - SortingHandler (proyecto SortingService): estructura en 3 bloques
 *
 * Fuentes externas:
 *   - HttpHandler: https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpHandler.html
 */
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
  public void handle(final HttpExchange exchange) throws IOException {

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

  /**
   * Realiza el alta de un jugador nuevo en la base de datos.
   * 
   * Esta clase lee el cuerpo de la peticion realiada, comprueba
   * que el formato de los datos que se reciben son los esperados y
   * realiza el alta del jugador en la base de datos
   * 
   * Fuentes internas:
   * - Enunciado:
   *     p.2 (formato para playername y respuesta con writeBoolean)
   *     p.4 (Scanner, next(pattern), NFKC, NoSuchElementException,
   *          nombre de jugador sin espacios)
   *     p.7 (204 si no pasa la validacion)
   *     p.8 (204 si hay error interno)
   * - Tema 2b: 
   *    p.2 (rechazar, no arreglar)
   *    p.6 (NFKC)
   *    p.7 (charset)
   * - SortingHandler: sendResponseHeaders(HTTP_OK, 0)
   * 
   * Fuentes externas:
   * - HttpExchange (getRequestBody, sendResponseHeaders, getResponseBody):
   *    https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpExchange.html
   * - Scanner (next(String), hasNext()):
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Scanner.html
   * - NoSuchElementException:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/NoSuchElementException.html
   * - Normalizer:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/Normalizer.html
   * - Unicode TR15 (formas de normalización):
   *    https://www.unicode.org/reports/tr15
   * - DataOutputStream (writeBoolean):
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/DataOutputStream.html
   */
  private void handlePutRequest (final HttpExchange exchange)
  {
    final Players p = new Players();
    final boolean resultadoDeAlta;

    try ( Scanner bodyScanner
                  = new Scanner(
                          exchange.getRequestBody(),
                          StandardCharsets.UTF_8.name()))
    {

      final String lineaCuerpo = bodyScanner.nextLine();
      final String lineaNormalizada = Normalizer.normalize(
              lineaCuerpo,
              Normalizer.Form.NFKC);

      try ( Scanner lineScanner
                    = new Scanner(lineaNormalizada))
      {

        lineScanner.next("playername");
        lineScanner.next(":");
        final String nombre;
        
        // Obtengo el nombre del jugador de la peticion
        nombre = lineScanner.next();
        
        // Cualquier cosa que no sea acerca del jugador
        // la voy a rechazar
        if (lineScanner.hasNext())
        {
          throw new NoSuchElementException("Residuo");
        }
        resultadoDeAlta = p.newPlayer(nombre);
      }
    }
    catch (NoSuchElementException e)
    {
      logging(
              Level.WARNING,
              e, exchange,
              "Parametros incorrectos en PUT");
      responderSinContenido(exchange);
      return;
    }
    catch (SQLException e)
    {
      logging(
              Level.SEVERE,
              e, exchange,
              "Fallo en bbdd al dar de alta al juegador nuevo");
      responderSinContenido(exchange);
      return;
    }

    try
    {
      exchange.sendResponseHeaders(HTTP_OK, 0);
      try ( OutputStream os= exchange.getResponseBody();  
              DataOutputStream dos = new DataOutputStream(os))
      {
        dos.writeBoolean(resultadoDeAlta);
      }
    }
    catch (IOException e)
    {
      logging(
              Level.SEVERE,
              e, exchange,
              "Error de entrada/salida al escribir respuesta en PUT");
    }
  }

  /**
   * Este metodo manda un 204.
   *
   * He realizado este metodo porque sendRespondeHeader lanza un excepcion de IO
   * y, para todo aquello que se repite, he simplifcado y creado este mtodo en
   * vez de repetir el try-catch en cada sitio 
   */
  private void responderSinContenido (final HttpExchange exchange)
  {
    try
    {
      exchange.sendResponseHeaders(
              HTTP_NO_CONTENT, 
              NO_CONTENT);
    }
    catch (IOException e)
    {
      logging(
              Level.WARNING,
              e, exchange,
              "No se pudo enviar el codigo 204 al cliente");
    }
  }

  private void handleGetRequest (final HttpExchange exchange)
  {
    final Players p = new Players();
    try
    {
      final List<String> listaDeJugadores = p.getPlayers();
      exchange.sendResponseHeaders(HTTP_OK, 0);
      try (
              final OutputStream os = exchange.getResponseBody();
               final Writer w = new OutputStreamWriter(
                       os, StandardCharsets.UTF_8))
      {
        for (final String j : listaDeJugadores)
        {
          w.write(j);
          w.write("\n");
        }
      }
    }
    catch (final SQLException ex)
    {
      logging(
              Level.WARNING,
              ex,
              exchange,
              "Fallo en bbdd al obtener el listado de jugadores");
      responderSinContenido(exchange);
    }
    catch (final IOException ex)
    {
      logging(
              Level.SEVERE,
              ex,
              exchange,
              "Error de entrada/salida al escribir el listado de jugadores en GET");
    }
  }

  /**
   * Apunta en el registro log lo que ha pasado y cual fue la peticion - quien
   * la manda - con que metodo fue mandada - a que ruta
   *
   * Fuente consultada interna: 
   *  - Enunciado: 
   *    p.7 (la firma viene de ahí, y el registro de excepciones va en este nivel) 
   * - Fuentes externas:
   *  https://stackoverflow.com/questions/5950557/good-examples-using-java-util-logging
   *  https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpExchange.html
   *  https://stackoverflow.com/questions/7036765/how-to-i-format-my-log-message
   *  https://docs.astral.sh/ruff/rules/logging-string-format/
   *  https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/URI.html
   *  https://docs.oracle.com/en/java/javase/11/docs/api/java.logging/java/util/logging/Logger.html
   */
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
            exchange.getRequestURI().getPath(), /* scores atiende dos get */
            eventDescription);

    LOGGER.log(level, mensaje, ex);
  }

}

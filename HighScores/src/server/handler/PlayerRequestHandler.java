/**
 * El desarrollo y cambios realizados durante este proyecto estan
 * debidamente documentados en mi repo de GitHub, donde se puede
 * consultar el historial de commmits, las decisiones tomadas y las
 * correciones realiadas durante el proyecto. 
 * 
 * He intentado seguir la teoria vista en clase y los ejemplos
 * explicados por MAZ en NetBeans, procuré mantenr un estilo
 * coherente con lo trabajdo durante las sesiones.
 * 
 * En los comentarios de los metodos explico las decisiones de 
 * implementacion más relevantes a mi parecer y los motivos por
 * los que he decidido optar por ciertas soluciones.
 * 
 * Durante el desarrollo me he apoyado fuertemente en la documentacion
 * oficial de Java con el objetivo de consultar el funcionamiento
 * de las clases, metodos y excepciones utilizadas.
 *
 * REPO: https://github.com/irllymissher/Proyecto-HighScores.git
 *
 * ------------------
 * Fuentes internas:
 * ------------------
 *  - Enunciado:
 *    p.4 (lectura del cuerpo)
 *    p.7 (registro y excepciones)
 *    p.8 (codigos de respuesta)
 *  - Tema 2b:
 *    p.2 (rechazar entradas invalidas)
 *    p.6 (NFKC)
 *    p.7 (charset explicito)
 *    p.4 (sin comodines, porque no van ^ y $)
 *  - SortingHandler (proyecto SortingService): estructura en 3 bloques
 *
 * ------------------
 * Fuentes externas:
 * ------------------
 *   - HttpHandler: 
 *          https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpHandler.html
 *   - Pattern:  
 *          https://sppcontests.org/lang_docs/jdk-11.0.24_doc-all/docs/api/java.base/java/util/regex/Pattern.html 
 *          https://javadoc.scijava.org/Java11/java.base/java/util/regex/class-use/Pattern.html
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//
import server.db.Players;
//

public final class PlayerRequestHandler implements HttpHandler {

  static private final String CLASS_NAME = PlayerRequestHandler.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

  static private final int NO_CONTENT = -1;

  // MODIFICACION: parecido a lo que se realiza en la asignatura de procesadores del lenguaje
  // Permito una cantidad de caracteres
  // Permito una longitud de 1 a 63
  // Exluyo el uso de la barra inicial `/`
  // Permito que, por lo menos, exista una letra en cualquier posicion
  static private final String REGEX_NOMBRE_JUGADOR = "(?!/)(?=[a-zA-Z0-9_@/-]*[a-zA-Z])[a-zA-Z0-9_@/-]{1,63}";
  static private final Pattern PATRON_NOMBRE_JUGADOR = Pattern.compile("(?!/)(?=[a-zA-Z0-9_@/-]*[a-zA-Z])[a-zA-Z0-9_@/-]{1,63}");

  @Override
  public void handle (final HttpExchange exchange) throws IOException
  {

    final String requestMethodName = exchange.getRequestMethod();
    if ("PUT".compareTo(requestMethodName) == 0)
    {
      LOGGER.info("PUT recibido");
      // Solicitud de inserción de nuevo jugador
      handlePutRequest(exchange);
    }
    else if ("GET".compareTo(requestMethodName) == 0)
    {
      LOGGER.info("GET recibido");
      // Solicitud de listado de jugadores
      handleGetRequest(exchange);
    }
    else
    {
      // Servicio no atiende otros tipos de requests.
      exchange.sendResponseHeaders(HTTP_BAD_REQUEST, NO_CONTENT);
    }

  }

  /**
   * Se realiza el ALTA de un jugador.
   *
   * Lee el cuerpo de la peticion, la normaliza y valida que tenga los param
   * esperados y que el nombre del jugador es valido
   *
   * En caso de que sea correcto entonces se registra en la bbdd mientras que si
   * registra un error entonces responde un 204 sin cuerpo
   *
   * Cualquier error en la peticion hace que se responda con un 204 sin cuerpo
   *
   * Este metodo lee el cuerpo de la peticion realiada, comprueba que el formato
   * de los datos que se reciben son los esperados y realiza el alta del jugador
   * en la base de datos
   *
   * ------------------
   * Fuentes internas:
   * ------------------
   * - Enunciado:
   *    p.2 (formato para playername y respuesta con writeBoolean)
   *    p.4 (Scanner, next(pattern), NFKC, NoSuchElementException, nombre de jugador sin espacios)
   *    p.7 (204 si no pasa la validacion)
   *    p.8 (204 si hay error interno)
   * - Tema 2b:
   *    p.2 (rechazar, no arreglar)
   *    p.6 (NFKC)
   *    p.7 (charset)
   * - SortingHandler: sendResponseHeaders(HTTP_OK, 0)
   *
   * -----------------
   * Fuentes externas:
   * -----------------
   * - HttpExchange (getRequestBody, sendResponseHeaders, getResponseBody):
   *                https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpExchange.html
   *
   * - Scanner (next(String), hasNext()):
   *                https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Scanner.html
   *
   * - NoSuchElementException:
   *                https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/NoSuchElementException.html
   *
   * - Normalizer:
   *                https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/Normalizer.html
   *
   * - Unicode TR15 (formas de normalizacion):
   *                https://www.unicode.org/reports/tr15
   *
   * - DataOutputStream (writeBoolean):
   *                https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/DataOutputStream.html
   */
  private void handlePutRequest (final HttpExchange exchange)
  {
    final Players jugadores = new Players();
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
        final String nombreJugador;

        // Obtengo el nombre del jugador de la peticion
        // Validamos este token mediante la REGEX de lo permitido
        nombreJugador = lineScanner.next(REGEX_NOMBRE_JUGADOR);

        // Cualquier cosa que no sea acerca del jugador
        // la voy a rechazar
        if (lineScanner.hasNext())
        {
          throw new NoSuchElementException("Residuo");
        }
        resultadoDeAlta = jugadores.newPlayer(nombreJugador);
      }
    }
    catch (final NoSuchElementException e)
    {
      logging(
              Level.WARNING,
              e, exchange,
              "Parametros incorrectos en PUT");
      responderSinContenido(exchange);
      return;
    }
    catch (final SQLException e)
    {
      logging(
              Level.WARNING,
              e, exchange,
              "Fallo en bbdd al dar de alta al juegador nuevo");
      responderSinContenido(exchange);
      return;
    }

    try
    {
      exchange.sendResponseHeaders(HTTP_OK, 0);
      try ( OutputStream os = exchange.getResponseBody();  DataOutputStream dos = new DataOutputStream(os))
      {
        dos.writeBoolean(resultadoDeAlta);
      }
    }
    catch (final IOException e)
    {
      logging(
              Level.WARNING,
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
    catch (final IOException e)
    {
      logging(
              Level.WARNING,
              e, exchange,
              "No se pudo enviar el codigo 204 al cliente");
    }
  }

  /**
   * Lista de jugadores.
   *
   * Obtiene la lista de jugadores y se las envia al cliente. Escribe a cada
   * jugador en un linea diferente
   *
   * Obtengo la lista antes de enviar 200, si ocurre algun fallo entonces envio
   * 204
   *
   * ------------------
   * Fuentes internas:
   * ------------------
   * - Enunciado:
   *    p.2 (GET /player: cuerpo vacio, un nombre por linea)
   *    p.5 (respuesta de varias lineas con OutputStreamWriter)
   *    p.8 (204 si hay error interno)
   * - Tema 2b:
   *    p.7 (charset explicito)
   *
   * -----------------
   * Fuentes externas:
   * -----------------
   * - OutputStreamWriter:
   *                https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/OutputStreamWriter.html
   *
   * - StandardCharsets:
   *                https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/StandardCharsets.html
   *
   * - HttpExchange (sendResponseHeaders, getResponseBody):
   *                https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpExchange.html
   */
  private void handleGetRequest (final HttpExchange exchange)
  {
    final Players jugadores = new Players();
    try
    {
      final List<String> listaDeJugadores = jugadores.getPlayers();
      exchange.sendResponseHeaders(HTTP_OK, 0);
      try (
              final OutputStream os = exchange.getResponseBody(); final Writer w = new OutputStreamWriter(
              os, StandardCharsets.UTF_8))
      {
        for (final String jugador : listaDeJugadores)
        {
          final Matcher comprobacionNombreJugador;
          comprobacionNombreJugador = PATRON_NOMBRE_JUGADOR.matcher(jugador);
          if (!comprobacionNombreJugador.matches())
          {
            LOGGER.warning("el nombre del jugador no es valido (bbdd)");
            continue;
          }

          w.write(jugador);
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
              Level.WARNING,
              ex,
              exchange,
              "Error de entrada/salida al escribir el listado de jugadores en GET");
    }
  }

  /**
   * Apunta en el registro log lo que ha pasado y cual fue la peticion - quien
   * la manda - con que metodo fue mandada - a que ruta
   *
   * ------------------
   * Fuentes internas:
   * ------------------
   * - Enunciado:
   *    p.7 (la firma viene de ahi, y el registro de excepciones va en este nivel)
   * 
   * -----------------
   * Fuentes externas:
   * -----------------
   * Logger:
   *                https://stackoverflow.com/questions/5950557/good-examples-using-java-util-logging
   *                https://stackoverflow.com/questions/7036765/how-to-i-format-my-log-message
   *                https://docs.oracle.com/en/java/javase/11/docs/api/java.logging/java/util/logging/Logger.html
   * 
   * HttpExchange (getRemoteAddress, getRequestMethod, getRequestURI):              
   *                https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpExchange.html
   *                
   * URI
   *                https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/URI.html
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
            exchange.getRequestURI().getPath(),
            eventDescription);

    LOGGER.log(level, mensaje, ex);
  }

}

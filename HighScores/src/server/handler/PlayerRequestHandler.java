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

  private void handlePutRequest(final HttpExchange exchange) {

    // TODO
    /**
     * README: > Este metodo sale directamente de `SortingHandler` tenemos >
     * metodo similar: alojar, ordenar, respuesta....
     *
     * 1) Lectura 2) Procesar 3) Respuesta
     */
    final Players p = new Players();
    final boolean resultadoDeAlta;

    /**
     * README: > "Se emplea un objeto de clase Scanner pra ller las lineas"
     * sacado > directamente de la practica p.4 > El charset explicito sale del
     * Tema 2b, p.7 en el cual comenta que > debemos de establecer el charset en
     * el que opera cada stream.
     */
    try ( Scanner bodyScanner
        = new Scanner(
            exchange.getRequestBody(),
            StandardCharsets.UTF_8.name())) {

      /**
       * README: > Normalizamos cada linea a la forma NFKC > Usamos Tema 2b, p.6
       * donde se extiende consenso en emplear NFKC
       */
      final String lineaCuerpo = bodyScanner.nextLine();
      final String lineaNormalizada = Normalizer.normalize(
          lineaCuerpo,
          Normalizer.Form.NFKC);

      /**
       * REVISAR ESTE CODIGO PORQUE NO ESTOY SEGURO DE SI ES NECESARIO
       */
      try ( Scanner lineScanner
          = new Scanner(lineaNormalizada)) {

        /**
         * README: > "next(String pattern) para obtener cada token" > "el token
         * playername se lee con la invocación scanner.next("playername")"
         */
        lineScanner.next("playername");
        lineScanner.next(":");
        final String nombre;
        nombre = lineScanner.next();

        /**
         * README: > Me apoyo en el Tema 2b, p.2: "rechazar datos inválidos… no
         * se debe intentar arreglar" > Tambien del enuncadio de la practica:
         * "expresiones regulares que se refieran a la entrada completa"
         */
        if (lineScanner.hasNext()) {
          /**
           * README: > "Es obligatorio capturar la excepción
           * NoSuchElementException" > "peticiones que no pasen los filtros…
           * HTTP_NO_CONTENT pero sin cuerpo"
           */
          throw new NoSuchElementException("Residuo");
        }
        resultadoDeAlta = p.newPlayer(nombre);
      }
    } catch (NoSuchElementException e) {
      logging(
          Level.WARNING,
          e, exchange,
          "Parametros incorrectos en PUT");
      responderSinContenido(exchange);
      return;
    } /**
     * README: > «relanzadas desde el nivel interior» se registran en el
     * handler; > errorinterno → 204 sin cuerpo
     */
    catch (SQLException e) {
      logging(
          Level.SEVERE,
          e, exchange,
          "Fallo en bbdd al dar de alta al juegador nuevo");
      responderSinContenido(exchange);
      return;
    }

    try {
      /**
       * README: > Cabecera con longitud libre sale directamente de
       * `SortingHandler`: > " el valor 0 indica que se envía una cantidad de
       * octetos no determinada" > Tambien indico que uso parte de la Teoria:
       * Anexo 1, p.14, pasos 5-6
       */
      exchange.sendResponseHeaders(HTTP_OK, 0);

      /**
       * README: > Stream de respuesta sacado de `SortingHandler` misma pareja
       * de recursos
       */
      try ( OutputStream os
          = exchange.getResponseBody();  DataOutputStream dos = new DataOutputStream(os)) {
        /**
         * README: > el servidor debe escribir la respuesta empleando el método
         * writeBoolean()
         */
        dos.writeBoolean(resultadoDeAlta);
      }
    } catch (IOException e) {
      logging(
          Level.SEVERE,
          e, exchange,
          "Error de entrada/salida al escribir respuesta en PUT");
    }
  }

  /**
   * El 204 envia http no content y no content y esa llamada lanza IOException
   * Los metodos que tenemos que realizar vienen sin throws Cada catch que
   * responde el 204 necesita su propio bloque try-catch y para ahorrarnos el
   * lio podemos crear este metodo
   *
   * PARA REVISAR: el profesor @MAZ en el netbeans de SortingHandler realiza el
   * catch directamente porque su handler declara throws IOExcepcion, pero los
   * metodos de este proyecot no lo hacen
   *
   * @param exchange
   *
   * README: > Propuesto para no repetir try carch constantemente, no lo pide el
   * enunciado > El resto de elementos parten del Anexo 1, p.13 y de
   * `SortingHandler`
   */
  private void responderSinContenido(final HttpExchange exchange) {
    try {
      exchange.sendResponseHeaders(HTTP_NO_CONTENT, NO_CONTENT);
    } catch (IOException e) {
      logging(
          Level.WARNING,
          e, exchange,
          "No se pudo enviar el codigo 204 al cliente");
    }
  }

  private void handleGetRequest(final HttpExchange exchange) {

    // TODO
    final Players p = new Players();
    try {
      final List<String> listaDeJugadores = p.getPlayers();

      /**
       * README: > Sacado del mismo `SortingHandler`
       */
      exchange.sendResponseHeaders(HTTP_OK, 0);
      try (
          final OutputStream os = exchange.getResponseBody(); /**
           * README: > "Cuando el cuerpo de respuesta … esté formado por más de
           * una línea > esa respuesta se debe escribir mediante un objeto de
           * clase OutputStreamWriter" > Me apoyo del Team 2b, p.6-7, aunque el
           * profe @MAZ utiliza: `Dfile.encoding=UTF-8`
           */
           final Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
        for (final String j : listaDeJugadores) {
          /**
           * README: > "un carácter end-line separa cada línea" > Saco el salto
           * de linea de NewScore que escribe el campo y luego salto de linea
           */
          w.write(j);
          w.write("\n");
        }
      }
    } catch (final SQLException ex) {
      logging(
          Level.WARNING,
          ex,
          exchange,
          "Fallo en bbdd al obtener el listado de jugadores");
      responderSinContenido(exchange);
    } catch (final IOException ex) {
      logging(
          Level.SEVERE,
          ex,
          exchange,
          "Error de entrada/salida al escribir el listado de jugadores en GET");
    }
  }

  /**
   * Informacion buscada: >
   * https://stackoverflow.com/questions/5950557/good-examples-using-java-util-logging
   * >
   * https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpExchange.html
   * >
   * https://stackoverflow.com/questions/7036765/how-to-i-format-my-log-message
   * > https://docs.astral.sh/ruff/rules/logging-string-format/
   */
  private void logging(
      final Level level,
      final Exception ex,
      final HttpExchange exchange,
      final String eventDescription) {
    final String mensaje = String.format(
        "%s - %s - %s - %s",
        exchange.getRemoteAddress().toString(),
        exchange.getRequestMethod(),
        exchange.getRequestURI().getPath(), /* scores atiende dos get */
        eventDescription);

    LOGGER.log(level, mensaje, ex);
  }

}

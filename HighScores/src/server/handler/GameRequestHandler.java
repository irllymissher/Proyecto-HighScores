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
 * Fuentes consultas internas:
 *    - Enunciado:
 *        p.4 (como leer el cuerpo)
 *        p.7 (registro y excepciones)
 *        p.8 (codigos de respuesta)
 *    - Tema 2b:
 *        p.2 (lo invalido se rechaza, no se arregla)
 *        p.6 (NFKC)
 *        p.7 (charset siempre explicito)
 *    - SortingHandler: para la estrcutura en 3 bloques
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
  public void handle(final HttpExchange exchange) throws IOException {

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

  /**
   * Se realiza el ALTA de un juego.
   *
   * p.ej. Llega una linea: "gamename : Super Mario Bros" Si va bien entonces
   * respondo con un 200 con un Booleano: true en caso de registrarse y false si
   * ya existia, si la peticion no es la correcta o algo inesperado y sin cuerpo
   * responde un 204
   *
   * Fuentes consultas internas: 
   *  - Enunciado: 
   *    p.2-3 (formato y writeBoolean) 
   *    p.4 (Scanner, next(pattern), NFKC, NoSuchElementExcepction, nombre de juegos
   *    con espacios) 
   *    p.7-8 (el 204)
   *
   * Fuente externas: - HttpHandler:
   * https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpHandler.html
   *
   * - HttpExchange.getRequestBody:
   * https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpExchange.html
   *
   * - Scanner: next(String) y nextLine():
   * https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Scanner.html
   * https://codegym.cc/groups/posts/scanner-nextline-method-in-java
   * https://docs.oracle.com/javase/tutorial/essential/io/scanning.html
   *
   * - Normalizer
   * https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/Normalizer.html
   *
   * - DataOutputStream:
   * https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/DataOutputStream.html
   * https://www.tutorialspoint.com/java/io/dataoutputstream_writeboolean.html
   */
  private void handlePutRequest (final HttpExchange exchange)
  {

    final Games g = new Games();
    final boolean resultadoDeAlta;

    try ( Scanner bodyScanner
                  = new Scanner(
                    exchange.getRequestBody(),
                    StandardCharsets.UTF_8.name()))
    {

      final String lineaCuerpo = bodyScanner.nextLine();

      // Primero se normalizo a NFKC y luego valido
      final String lineaNormalizada = Normalizer.normalize(
              lineaCuerpo,
              Normalizer.Form.NFKC);

      // Scanner para trocer la linea
      try ( Scanner lineScanner
                    = new Scanner(lineaNormalizada))
      {
        final String nombreDespuesDeTRim;
        final String nombreAntesDeTrim;

        lineScanner.next("gamename");
        lineScanner.next(":");

        // Me quedo con el resto de la linea
        //los juegos pueden llegar espacio
        nombreAntesDeTrim = lineScanner.nextLine();
        nombreDespuesDeTRim = nombreAntesDeTrim.trim();

        // Trim no me devuelve error si ocurre algo
        // decido comprobarlo
        if (nombreDespuesDeTRim.isEmpty())
        {
          throw new NoSuchElementException("Nombre de juego vacio tras limpiar espacios");
        }

        // Games se encarga de ponerlo en mayusculas
        resultadoDeAlta = g.newGame(nombreDespuesDeTRim);
      }

      // La peticion no pasa el filtro: 204 sin cuerpo y continuamos
    }
    catch (final NoSuchElementException e)
    {
      logging(
              Level.WARNING,
              e, exchange,
              "Param incorrectos para PUT en juegos");
      responderSinContenido(exchange);
      return;

      //La base de datos ha fallado, lo apuntamos y 204
    }
    catch (final SQLException e)
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

      // El 0, como en SortingHandler, significa que no sabe cuantos bytes
      // se van a mandar
      exchange.sendResponseHeaders(HTTP_OK, 0);
      try ( OutputStream os
                         = exchange.getResponseBody();  DataOutputStream dos = new DataOutputStream(os))
      {
        dos.writeBoolean(resultadoDeAlta);
      }

      // El 200 ha salido, no puedo cambiarlo por un 204
    }
    catch (final IOException e)
    {
      logging(
              Level.WARNING,
              e, exchange,
              "Error de i/o al escribir respuesta en PUT");
    }
  }

  /**
   * Lista de juegos.
   *
   * Un juego por cada linea. Si la base de datos falla, por lo que sea, recibe
   * un 204 sin cuerpo
   *
   * Fuentes consultas internas: 
   *  - Enunciado: 
   *    p.2-3 (formato) 
   *    p.5 (si son varias lineas, con OutPutStreamWrite) 
   *    p.7-8 (el 204)
   *
   * Fuentes consultas externas: - OutputStreamWriter:
   * https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/OutputStreamWriter.html 
   */
  private void handleGetRequest (final HttpExchange exchange)
  {

    final Games g = new Games();

    try
    {
      final List< String> listaJuegos;

      // Pedimos la lista antes de que haga 200
      listaJuegos = g.getGames();

      exchange.sendResponseHeaders(
              HTTP_OK,
              0);

      // Varias lineas de texto, realizado como lo pide el enunciado
      try (final OutputStream os = exchange.getResponseBody(); final Writer w = new OutputStreamWriter(
           os,
           StandardCharsets.UTF_8))
      {
        for (final String j : listaJuegos)
        {
          w.write(j);

          // Un juego por linea para que el cliente pueda leerlo
          // linea por linea
          w.write("\n");
        }
      }
    }
    catch (final SQLException ex)
    {
      logging(Level.WARNING, ex, exchange, "Fallo en bdd al conseguir los juegos");
      responderSinContenido(exchange);
    }
    catch (final IOException ex)
    {
      logging(Level.WARNING, ex, exchange, "Error de i/o al escribir el listado de juegos");
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
      exchange.sendResponseHeaders(HTTP_NO_CONTENT, NO_CONTENT);
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
   * Apunta en el registro log lo que ha pasado y cual fue la peticion - quien
   * la manda - con que metodo fue mandada - a que ruta
   *
   * Fuente consultada interna: 
   *  - Enunciado: 
   *    p.7 (la firma viene de ahí, y el registro de excepciones va en este nivel) 
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

    LOGGER.log(
            level,
            mensaje,
            ex);
  }

}

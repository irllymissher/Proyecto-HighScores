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
 * - Enunciado:
 *    p.3 (POST /score y GET /score con query)
 *    p.4 (lectura y validacion del cuerpo)
 *    p.7 (registro y excepciones)
 *    p.8 (códigos de respuesta)
 * - Tema 2b:
 *    p.2 (rechazar entradas invalidas)
 *    p.6 (NFKC)
 *    p.7 (charset explicito)
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
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Map;
import java.util.NoSuchElementException;
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
  
  // MODIFICACION: parecido a lo que se realiza en la asignatura de procesadores del lenguaje
  // Permito una cantidad de caracteres
  // Permito una longitud de 1 a 63
  // Exluyo el uso de la barra inicial `/`
  // Permito que, por lo menos, exista una letra en cualquier posicion
  static private final String PATRON_DEL_NOMBRE_DEL_JUGADOR = "(?!/)(?=[a-zA-Z0-9_@/-]*[a-zA-Z])[a-zA-Z0-9_@/-]{1,63}";

  @Override
  public void handle(final HttpExchange exchange) throws IOException {

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


  /**
   * Se realiza el ALTA de la puntuacion.
   *
   * Este metodo lee el jugador, los datos del juego   y la puntuacion que se ha recibido.
   * Cada linea leida se normaliza y se valida que los parametros sean los esperados, si
   * todo es correcto entonces se registra en la bbdd respondiendo con un 200, y con un
   * booleano indicando que se dio de alta, si falla entonces devuelve false
   *
   * Si el formato no es esperado o falla por alguna razon entonces lanzaremos un error
   * y se respondera con un 204 sin cuerpo
   *
   * Fuentes internas:
   * - Enunciado:
   *    p.2 (puntuación positiva; respuesta con writeBoolean)
   *    p.3 (cuerpo de tres líneas: playername, gamename, score)
   *    p.4 (Scanner, next(pattern), NFKC, NoSuchElementException
   *      obligatoria, nombre de jugador sin espacios y de juego con
   *      espacios, puntuacion de hasta 10 digitos, positiva y convertida
   *      con Integer.parseInt)
   *    p.7 (204 si no pasa la validacion)
   *    p.8 (204 si hay error interno)
   * - Tema 2b:
   *    p.2 (rechazar, no arreglar)
   *    p.4 (expresiones regulares sin comodines, conversiOn de tipos con
   *          manejo estricto de excepciones)
   *    p.6 (NFKC)
   *    p.7 (charset)
   * Fuentes externas:
   * - HttpExchange (getRequestBody, sendResponseHeaders, getResponseBody):
   *    https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpExchange.html
   * - Scanner (next(String), nextLine(), hasNext()):
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Scanner.html
   * - Pattern (sintaxis de [0-9]{1,10}):
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html
   * - Integer.parseInt:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Integer.html
   * - NumberFormatException:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/NumberFormatException.html
   * - NoSuchElementException:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/NoSuchElementException.html
   * - Normalizer:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/Normalizer.html
   * - DataOutputStream (writeBoolean):
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/DataOutputStream.html
   */
  private void handlePostRequest (final HttpExchange exchange)
  {
    final Scores s;
    final boolean resultadoDeAlta;
    final String nombreJugador;
    final int puntuacionRecibida;
    final String nombreJuego;

    s = new Scores();

    try ( Scanner bodyScanner = new Scanner(
            exchange.getRequestBody(),
            StandardCharsets.UTF_8.name()))
    {
      final String lineaCuerpo, lineaNormalizada;
      final String lineaJuego, lineaNormalzadaJuego;
      final String lineaPuntuacion, lineaNormalizadaPuntuacion;

      lineaCuerpo = bodyScanner.nextLine();
      lineaNormalizada = Normalizer.normalize(
              lineaCuerpo,
              Normalizer.Form.NFKC);
      /* JUGADOR */
      try ( Scanner linScanner = new Scanner(lineaNormalizada))
      {
        linScanner.next("playername");
        linScanner.next(":");
        
        // Validacion del nombre exactamente como se pide
        nombreJugador = linScanner.next(PATRON_DEL_NOMBRE_DEL_JUGADOR);
        if (linScanner.hasNext())
        {
          throw new NoSuchElementException("Residuo en jugador");
        }
      }

      lineaJuego = bodyScanner.nextLine();
      lineaNormalzadaJuego = Normalizer.normalize(
              lineaJuego,
              Normalizer.Form.NFKC);
      /* JUEGO */
      try ( Scanner linScanner = new Scanner(lineaNormalzadaJuego))
      {
        linScanner.next("gamename");
        linScanner.next(":");
        nombreJuego = linScanner.nextLine().trim();
        if (nombreJuego.isEmpty())
        {
          throw new NoSuchElementException("Nombre de juego vacio");
        }
      }

      lineaPuntuacion = bodyScanner.nextLine();
      lineaNormalizadaPuntuacion = Normalizer.normalize(
              lineaPuntuacion,
              Normalizer.Form.NFKC);
      /* PUNTUACION*/
      try ( Scanner linScanner = new Scanner(lineaNormalizadaPuntuacion))
      {
        linScanner.next("score");
        linScanner.next(":");
        
        // El patron acepta de 1 a 10 digitos desde 0 a 9
        // Usamos el parseint, como exige el enunciado, porque 10 digitos
        // no caben en un simple int
        puntuacionRecibida = Integer.parseInt(linScanner.next("[0-9]{1,10}"));
        if (linScanner.hasNext())
        {
          throw new NoSuchElementException("Residuo en scores");
        }
        
        // 0 no lo cuento como positivo
        if (puntuacionRecibida <= 0)
        {
          throw new NoSuchElementException("Puntuacion no es positiva");
        }
      }
      resultadoDeAlta = s.newScore(nombreJugador, nombreJuego, puntuacionRecibida);
    }
    catch (final NoSuchElementException e)
    {
      logging(
              Level.WARNING,
              e,
              exchange,
              "param incorrectos en de puntuaciones");
      responderSinContenido(exchange);
      return;
    }
    catch (final NumberFormatException e)
    {
      logging(
              Level.WARNING,
              e,
              exchange,
              "Puntuacion recibida no valida");
      responderSinContenido(exchange);
      return;
    }
    catch (final SQLException e)
    {
      logging(
              Level.WARNING,
              e,
              exchange,
              "fallo en bbdd al dar de alta la puntuacion");
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
              e,
              exchange,
              "Error de i/o al escribir respuesta en POST");
    }

  }

  /**
   * Este metodo procesa las solicitudes GET para obtener las listas de puntuaciones
   * 
   * Se valida la estructura de los parametros de la consulta antes de realizar la 
   * busqueda de la base de datos
   * 
   * Si la consulta falta o esta mal formada responde un 204, si es una clave 
   * desconocida responde 400
   * 
   * Fuentes internas:
   *  - Enunciado:
   *      p.3 (forma esperada -> clave=valor, sin espacios)
   *      p.4 (validacion de formato e informacion recibida)
   *      p.7 (peticiones invalida = respuesta HTTP NO CONTENT)
   * - Tema 2b: 
   *      p.1 (comprobacion antes de la llamada a getPlayerScores/getGameScores)
   *      p.2 (no dar por hecho que la consulta existe, responder 204 en vez de
   *          de intenetar arreglar la consulta, URL no confiable)
   *      p.4 (comprobacion  de tipo y estructura esperada)
   * - Anexo 1 (Protocolo HTTP en Java SE):
   *      p.5 (?query es un string con params de consulta opcionales, hay que revisar)
   *      p.6 (la consulta tiene forma nombre=valor)
   * 
   * Fuentes externas:
   *  - URI.getQuery()
   *      https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/URI.html
   *  - String.split(String)
   *      https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html
   *  - Validate inputs (Guideline 5-1 / INPUT-1)
   *      https://www.oracle.com/java/technologies/javase/seccodeguide.html
   */
  private void handleGetRequest (final HttpExchange httpExchange)
  {

    final URI uri = httpExchange.getRequestURI();
    final String queryGroup = uri.getQuery();
    
    ////////////////////////////// MODIFICACION ////////////////////////////////////////
    if (queryGroup == null ||queryGroup.isEmpty())
    {
      LOGGER.warning("La peticion get fue rechazada, la query es nula o esta vacia");
      responderSinContenido(httpExchange);
      return;
    }
    ///////////////////////////////////////////////////////////////////////////////////
    
    LOGGER.info(queryGroup);

    final String[] parts = queryGroup.split("=");
    
    ////////////////////////////// MODIFICACION ////////////////////////////////////////
    if (parts.length != 2)
    {
      LOGGER.warning("La peticion get fue rechazada, query tiene estructura invalida");
      responderSinContenido(httpExchange);
      return;
    }
    ///////////////////////////////////////////////////////////////////////////////////
    
    try
    {
      if ("playername".compareTo(parts[0]) == 0)
      {
        getPlayerScores(httpExchange, parts[1]);
      }
      else if ("gamename".compareTo(parts[0]) == 0)
      {
        getGameScores(httpExchange, parts[1]);
      }
      else
      {
        httpExchange.sendResponseHeaders(HTTP_BAD_REQUEST, NO_CONTENT);
      }
    }
    catch (final IOException ex)
    {
      LOGGER.log(Level.WARNING, "problema de I/O en respuesta", ex);
    }

  }

  /**
   * Obtiene puntuaciones de un jugador.
   *
   * Normaliza el nombre que se recibe y le solicita a la bbdd todas las
   * puntuaciones existentes.
   *
   * Le devuelve al cliente el nombre de cada juego junto con la puntuacion
   * que ha obtenido, cada dato es escrito en diferentes lineas
   *
   * Si falla en cualquier caso se registra el error
   * 
   * Fuentes internas: 
   *  - Enunciado: 
   *      p.3 
   *      p.5 (respuesta de varias lineas con OutputStreamWriter)
   *      p.8 (204 si hay error interno)  
   *  - Tema 2b:  
   *      p.6 (NFKC) 
   *      p.7 (charset) 
   *
   * Fuentes externas: 
   * - URI.getQuery:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/URI.html
   * - Normalizer:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/Normalizer.html
   * - Map
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Map.Entry.html
   * - OutputStreamWriter:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/OutputStreamWriter.html
   * - Writer
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/Writer.html
   * - String.valueOf:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html
   * - HttpExchange (sendResponseHeaders, getResponseBody):
   *    https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpExchange.html
   */
  private void getPlayerScores (final HttpExchange exchange,
                                final String playername)
  {

    final Scores s = new Scores();

    try
    {
      final String nombreNormalizado;
      final Map< String, Integer> listaPuntuaciones;

      nombreNormalizado = Normalizer.normalize(
              playername,
              Normalizer.Form.NFKC);

      listaPuntuaciones = s.getPlayerScores(nombreNormalizado);

      exchange.sendResponseHeaders(HTTP_OK, 0);

      try (final OutputStream os = exchange.getResponseBody(); final Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8))
      {
        for (final Map.Entry<String, Integer> entrada : listaPuntuaciones.entrySet())
        {
          w.write("gamename : ");
          w.write(entrada.getKey());
          w.write("\n");
          w.write("score : ");
          w.write(String.valueOf(entrada.getValue()));
          w.write("\n");
        }

      }
    }
    catch (final IOException ex)
    {
      logging(
              Level.WARNING,
              ex,
              exchange,
              "Error de i/o al escribir las puntuaciones del jugador ");
    }
    catch (final SQLException ex)
    {
      logging(
              Level.WARNING,
              ex,
              exchange,
              "Fallo en bbdd para obtener puntuaciones");
      responderSinContenido(exchange);
    }

  }

  /**
   * Obtiene las puntuaciones de un juego.
   *
   * Normaliza el nombre que se recibe y le solicita a la bbdd todas las
   * puntuaciones asociadas al juego
   *
   * Le devuelve al cliente el nombre de cada jugador junto con la puntuacion
   * que ha obtenido, cada dato es escrito en diferentes lineas
   *
   * Fuentes internas: 
   *  - Enunciado: 
   *      p.3  
   *      p.5 (respuesta de varias lineas con OutputStreamWriter)
   *      p.8 (204 si hay error interno)  
   *  - Tema 2b:  
   *      p.6 (NFKC) 
   *      p.7 (charset) 
   *
   * Fuentes externas: 
   * - URI.getQuery:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/URI.html
   * - Normalizer:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/Normalizer.html
   * - Map
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Map.Entry.html
   * - OutputStreamWriter:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/OutputStreamWriter.html
   * - Writer
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/Writer.html
   * - String.valueOf:
   *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html
   * - HttpExchange (sendResponseHeaders, getResponseBody):
   *    https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpExchange.html
   */
  private void getGameScores (final HttpExchange exchange,
                              final String gamename)
  {
    final Scores s = new Scores();

    try
    {
      final String nombreNormalizado;
      final Map< String, Integer> listaPuntuaciones;

      nombreNormalizado = Normalizer.normalize(
              gamename,
              Normalizer.Form.NFKC);

      listaPuntuaciones = s.getGameScores(nombreNormalizado);

      exchange.sendResponseHeaders(HTTP_OK, 0);

      try (final OutputStream os = exchange.getResponseBody(); final Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8))
      {
        for (final Map.Entry<String, Integer> entrada : listaPuntuaciones.entrySet())
        {
          w.write("playername : ");
          w.write(entrada.getKey());
          w.write("\n");
          w.write("score : ");
          w.write(String.valueOf(entrada.getValue()));
          w.write("\n");
        }

      }
    }
    catch (final IOException ex)
    {
      logging(
              Level.WARNING,
              ex,
              exchange,
              "Error de i/o al escribir las puntuaciones del juego ");
    }
    catch (final SQLException ex)
    {
      logging(
              Level.WARNING,
              ex,
              exchange,
              "Fallo en bbdd para obtener puntuaciones");
      responderSinContenido(exchange);
    }

  }

  /**
   * Apunta en el registro log lo que ha pasado y cual fue la peticion
   * - quien la manda
   * - con que metodo fue mandada
   * - a que ruta
   *
   * Fuente consultada interna:
   *  - Enunciado:
   *    p.7 (la firma viene de ahí, y el registro de excepciones va en este nivel)
   * - Fuentes externas:
   *  https://stackoverflow.com/questions/5950557/good-examples-using-java-util-logging
   *  https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpExchange.html
   *  https://stackoverflow.com/questions/7036765/how-to-i-format-my-log-message
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
            exchange.getRequestURI().getPath(),
            eventDescription);

    LOGGER.log(level, mensaje, ex);
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
}

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

  private void handlePostRequest(final HttpExchange exchange) {
    final Scores s;
    final boolean resultadoDeAlta;
    final String nombreJugador;
    final int puntuacionRecibida;
    final String nombreJuego;

    s = new Scores();

    try ( Scanner bodyScanner = new Scanner(
        exchange.getRequestBody(),
        StandardCharsets.UTF_8.name())) {
      final String lineaCuerpo, lineaNormalizada;
      final String lineaJuego, lineaNormalzadaJuego;
      final String lineaPuntuacion, lineaNormalizadaPuntuacion;

      lineaCuerpo = bodyScanner.nextLine();
      lineaNormalizada = Normalizer.normalize(
          lineaCuerpo,
          Normalizer.Form.NFKC);
      /* JUGADOR */
      try ( Scanner linScanner = new Scanner(lineaNormalizada)) {
        linScanner.next("playername");
        linScanner.next(":");
        nombreJugador = linScanner.next();
        if (linScanner.hasNext()) {
          throw new NoSuchElementException("Residuo en jugador");
        }
      }

      lineaJuego = bodyScanner.nextLine();
      lineaNormalzadaJuego = Normalizer.normalize(
          lineaJuego,
          Normalizer.Form.NFKC);
      /* JUEGO */
      try ( Scanner linScanner = new Scanner(lineaNormalzadaJuego)) {
        linScanner.next("gamename");
        linScanner.next(":");
        nombreJuego = linScanner.nextLine().trim();
        if (nombreJuego.isEmpty()) {
          throw new NoSuchElementException("Nombre de juego vacio");
        }
      }

      lineaPuntuacion = bodyScanner.nextLine();
      lineaNormalizadaPuntuacion = Normalizer.normalize(
          lineaPuntuacion,
          Normalizer.Form.NFKC);
      /* PUNTUACION*/
      try ( Scanner linScanner = new Scanner(lineaNormalizadaPuntuacion)) {
        linScanner.next("score");
        linScanner.next(":");
        puntuacionRecibida = linScanner.nextInt();
        if (linScanner.hasNext()) {
          throw new NoSuchElementException("Residuo en scores");
        }
      }
      resultadoDeAlta = s.newScore(nombreJugador, nombreJuego, puntuacionRecibida);
    } catch (final NoSuchElementException e) {
      logging(
          Level.WARNING,
          e,
          exchange,
          "param incorrectos en de puntuaciones");
      responderSinContenido(exchange);
      return;
    } catch (final SQLException e) {
      logging(
          Level.WARNING,
          e,
          exchange,
          "fallo en bbdd al dar de alta la puntuacion");
      responderSinContenido(exchange);
      return;
    }
    try {
      exchange.sendResponseHeaders(HTTP_OK, 0);
      try ( OutputStream os = exchange.getResponseBody();  DataOutputStream dos = new DataOutputStream(os)) {
        dos.writeBoolean(resultadoDeAlta);
      }
    } catch (final IOException e) {
      logging(
          Level.WARNING,
          e,
          exchange,
          "Error de i/o al escribir respuesta en POST");
    }

  }

  private void handleGetRequest(final HttpExchange httpExchange) {

    final URI uri = httpExchange.getRequestURI();
    final String queryGroup = uri.getQuery();
    final String[] parts = queryGroup.split("=");

    LOGGER.info(queryGroup);

    try {
      if ("playername".compareTo(parts[0]) == 0) {
        getPlayerScores(httpExchange, parts[1]);
      } else if ("gamename".compareTo(parts[0]) == 0) {
        getGameScores(httpExchange, parts[1]);
      } else {
        httpExchange.sendResponseHeaders(HTTP_BAD_REQUEST, NO_CONTENT);
      }
    } catch (final IOException ex) {
      LOGGER.log(Level.WARNING, "problema de I/O en respuesta", ex);
    }

  }

  private void getPlayerScores(final HttpExchange exchange,
      final String playername) {

    final Scores s = new Scores();

    try {
      final String nombreNormalizado;
      final Map< String, Integer> listaPuntuaciones;

      nombreNormalizado = Normalizer.normalize(
          playername,
          Normalizer.Form.NFKC);

      listaPuntuaciones = s.getPlayerScores(nombreNormalizado);

      exchange.sendResponseHeaders(HTTP_OK, 0);

      try (final OutputStream os = exchange.getResponseBody(); final Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
        for (final Map.Entry<String, Integer> entrada : listaPuntuaciones.entrySet()) {
          w.write("gamename : ");
          w.write(entrada.getKey());
          w.write("\n");
          w.write("score : ");
          w.write(String.valueOf(entrada.getValue()));
          w.write("\n");
        }

      }
    } catch (final IOException ex) {
      logging(
          Level.WARNING,
          ex,
          exchange,
          "Error de i/o al escribir las puntuaciones del jugador ");
    } catch (final SQLException ex) {
      logging(
          Level.WARNING,
          ex,
          exchange,
          "Fallo en bbdd para obtener puntuaciones");
      responderSinContenido(exchange);
    }

  }

  private void getGameScores(final HttpExchange exchange,
      final String gamename) {
    final Scores s = new Scores();

    try {
      final String nombreNormalizado;
      final Map< String, Integer> listaPuntuaciones;

      nombreNormalizado = Normalizer.normalize(
          gamename,
          Normalizer.Form.NFKC);

      listaPuntuaciones = s.getGameScores(nombreNormalizado);

      exchange.sendResponseHeaders(HTTP_OK, 0);

      try (final OutputStream os = exchange.getResponseBody(); final Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
        for (final Map.Entry<String, Integer> entrada : listaPuntuaciones.entrySet()) {
          w.write("playername : ");
          w.write(entrada.getKey());
          w.write("\n");
          w.write("score : ");
          w.write(String.valueOf(entrada.getValue()));
          w.write("\n");
        }

      }
    } catch (final IOException ex) {
      logging(
          Level.WARNING,
          ex,
          exchange,
          "Error de i/o al escribir las puntuaciones del juego ");
    } catch (final SQLException ex) {
      logging(
          Level.WARNING,
          ex,
          exchange,
          "Fallo en bbdd para obtener puntuaciones");
      responderSinContenido(exchange);
    }

  }

  private void logging(
      final Level level,
      final Exception ex,
      final HttpExchange exchange,
      final String eventDescription) {
    final String mensaje = String.format(
        "%s - %s - %s - %s",
        exchange.getRemoteAddress().toString(),
        exchange.getRequestMethod(),
        exchange.getRequestURI().getPath(),
        eventDescription);

    LOGGER.log(level, mensaje, ex);
  }

  private void responderSinContenido(final HttpExchange exchange) {
    try {
      exchange.sendResponseHeaders(HTTP_NO_CONTENT, NO_CONTENT);
    } catch (final IOException e) {
      logging(
          Level.WARNING,
          e, exchange,
          "No se pudo enviar el codigo 204 al cliente");
    }
  }
}

package server.db;

/**
 *
 * @author MAZ
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
//

public final class Scores {

  static private final String CLASS_NAME = Scores.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

  private final Connection connection;
  private final Games g;
  private final Players p;

  public Scores ()
  {
    this.connection = DBComponent.getConnection();
    this.p = new Players();
    this.g = new Games();
  }

  public boolean newScore (final String playername,
                           final String gamename,
                           final int score) throws SQLException
  {

    // Se obtiene identificador de jugador con nombre playername.
    final String idJugador = p.getPlayerId(playername);

    // Se obtiene identificador de juego con nombre gamename.
    final String idJuego = g.getGameId(gamename);
    /* Ya convertido en Mayusculas */

    // Se inserta puntuación.
    final String insertarConsulta = " insert into Scores (PlayerId, GameId, Score) values(?,?, ?)";
    if (playername == null || gamename == null)
    {
      return false;
    }
    if (idJugador.isEmpty()
        || idJuego.isEmpty())
    {
      return false;
    }

    try (final PreparedStatement preparedStatementInsertarPuntuaje
                                 = connection.prepareStatement(insertarConsulta))
    {

      preparedStatementInsertarPuntuaje.setString(1, idJugador);
      preparedStatementInsertarPuntuaje.setString(2, idJuego);

      preparedStatementInsertarPuntuaje.setInt(3, score);

      final int filasAfectadas = preparedStatementInsertarPuntuaje.executeUpdate();

      return filasAfectadas > 0;
    }
    catch (final SQLException e)
    {
      LOGGER.log(Level.WARNING, "Fallo al insertar el puntuaje ", e);
      throw e;
    }

  }

  public Map<String, Integer> getGameScores (final String gamename)
          throws SQLException
  {

    final Map<String, Integer> scores = new LinkedHashMap<>();
    final String idJuego = g.getGameId(gamename);

    if (gamename == null)
    {
      return scores;
    }

    /* Se me facilita leer la consulta asi */
    final String consulta
                 = "select p.PlayerName, MAX(s.Score) as MaxScore from Scores s "
                   + "join Players p on s.PlayerId = p.PlayerId "
                   + "where s.GameId = ? group by p.PlayerName order by MaxScore desc";

    try (final PreparedStatement preparedStatementPuntuacionesDeJuego
                                 = connection.prepareStatement(consulta))
    {
      preparedStatementPuntuacionesDeJuego.setString(1, idJuego);

      try (final ResultSet resultadoConsulta
                           = preparedStatementPuntuacionesDeJuego.executeQuery())
      {
        while (resultadoConsulta.next())
        {
          scores.put(
                  resultadoConsulta.getString("PlayerName"),
                  resultadoConsulta.getInt("MaxScore")
          );
        }
      }
    }
    catch (final SQLException e)
    {
      LOGGER.log(Level.WARNING, "Fallo en puntuaciones", e);
      throw e;
    }
    return scores;

  }

  public Map<String, Integer> getPlayerScores (final String playername)
          throws SQLException
  {

    final Map<String, Integer> scores = new LinkedHashMap<>();
    final String idJugador = p.getPlayerId(playername);

    // TODO
    if (playername == null)
    {
      return scores;
    }
    if (idJugador.isEmpty())
    {
      return scores;
    }
    final String consulta
                 = "select g.GameName, MAX(s.Score) as MaxScore from Scores s "
                   + "join Games g on s.GameId = g.GameId "
                   + "where s.PlayerId = ? group by g.GameName order by MaxScore desc";

    try (final PreparedStatement preparedStatementPuntuacioneJugadores
                                 = connection.prepareStatement(consulta))
    {
      preparedStatementPuntuacioneJugadores.setString(1, idJugador);

      try (final ResultSet resultadoConsulta
                           = preparedStatementPuntuacioneJugadores.executeQuery())
      {
        while (resultadoConsulta.next())
        {
          scores.put(
                  resultadoConsulta.getString("GameName"),
                  resultadoConsulta.getInt("MaxScore"));
        }
      }
    }
    catch (final SQLException e)
    {
      LOGGER.log(Level.WARNING, "Fallo al listar las puntuaciones del jugador", e);
      throw e;
    }
    return scores;
  }
}

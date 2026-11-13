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
 */

package server.db;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.PreparedStatement;

//
public final class Games {

  static private final String CLASS_NAME = Games.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

  static private final int ID_BYTE_LENGTH = 6;

  private final Connection connection;
  private final SecureRandom rg;

  public Games ()
  {
    this.connection = DBComponent.getConnection();
    this.rg = DBComponent.getRandomGenerator();
  }

  private String getRandomId ()
  {

    // Generación de secuencia aleatoria
    final byte[] rawId = new byte[ID_BYTE_LENGTH];
    rg.nextBytes(rawId);
    // Conversión a formato Base64
    return Base64.getEncoder().encodeToString(rawId);

  }

  public boolean newGame (final String name) throws SQLException
  {
    if (name == null)
    {
      return false;
    }

    final String juegoEnMayusc = name.toUpperCase();
    final String consulta = "insert into Games (GameId, GameName) values (?, ?)";
    // Si el juego ya está registrado, se devuelve false.

    // El identificador de un juego se pide al método getRandomId().
    if (!getGameId(name).isEmpty())
    {
      return false;
    }
    // Es necesario un bucle que vaya generando identificadores hasta
    // dar con uno que todavía no haya sido empleado.
    String id2 = getRandomId();
    while (!getGameName(id2).isEmpty())
    {
      id2 = getRandomId();
    }

    try (final PreparedStatement preparedStatementInsert
                                 = connection.prepareStatement(consulta))
    {
      preparedStatementInsert.setString(1, id2);

      preparedStatementInsert.setString(2, juegoEnMayusc);
      final int filasAfectadas = preparedStatementInsert.executeUpdate();
      return filasAfectadas > 0;
    }

    catch (final SQLException e)
    {
      LOGGER.log(Level.WARNING, "Fallo para registrar un juego nuevo", e);
      throw e;
    }
  }

  public List<String> getGames () throws SQLException
  {
    final List<String> games = new ArrayList<>(); 
    final String consulta = "select GameName from Games";
    try (final PreparedStatement preparedStatementJuegos
                                 = connection.prepareStatement(consulta))
    {

      try (final ResultSet resultadoConsulta = preparedStatementJuegos.executeQuery())
      {
        while (resultadoConsulta.next())
        {
          games.add(resultadoConsulta.getString("GameName"));
        }
      }
    }
    catch (final SQLException e)
    {
      LOGGER.log(Level.WARNING, "Fallo al listar los juegos", e);
      throw e;
    }
    return games;
  }

  String getGameId (final String name) throws SQLException
  {
    if (name == null)
    {
      return "";
    }
    final String juegoEnMayusculas = name.toUpperCase();
    final String consultaJuego = "select GameId from Games where GameName=?";

    try (final PreparedStatement preparedStatementJuegoEnMayusculas
                                 = connection.prepareStatement(consultaJuego))
    {

      preparedStatementJuegoEnMayusculas.setString(1, juegoEnMayusculas);
      try (final ResultSet resultadoConsulta = preparedStatementJuegoEnMayusculas.executeQuery())
      {
        if (resultadoConsulta.next())
        {
          return resultadoConsulta.getString("GameId");

        }
        else
        {
          return "";
        }
      }
    }
    catch (final SQLException e)
    {
      LOGGER.log(Level.WARNING, "Fallo al buscar el id del juego", e);
      throw e;
    }

  }

  String getGameName (final String gameId) throws SQLException
  {
    if (gameId == null)
    {
      return "";
    }
    final String consultaNombreJuego = "select GameName from Games where GameId=?";

    try (final PreparedStatement preparedStatementNombreJuego
                                 = connection.prepareStatement(consultaNombreJuego))
    {

      preparedStatementNombreJuego.setString(1, gameId);
      try (final ResultSet resultadoConsulta = preparedStatementNombreJuego.executeQuery())
      {
        if (resultadoConsulta.next())
        {
          return resultadoConsulta.getString("GameName");

        }
        else
        {
          return "";
        }
      }
    }
    catch (final SQLException e)
    {
      LOGGER.log(Level.WARNING, "Fallo al buscar el nombre del juego", e);
      throw e;
    }
  }

}

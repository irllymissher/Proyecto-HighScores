package server.db;
/**
 *
 * @author MAZ
 */
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//
public final class Players {

  static private final String CLASS_NAME = Players.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

  static private final int ID_BYTE_LENGTH = 9;

  private final Connection connection;
  private final SecureRandom rg;

  public Players () {
    this.connection = DBComponent.getConnection();
    this.rg = DBComponent.getRandomGenerator();
  }

  private String getRandomId () {

    // Generación de secuencia aleatoria
    final byte[] rawId = new byte[ID_BYTE_LENGTH];
    rg.nextBytes(rawId);
    // Conversión a formato Base64
    return Base64.getEncoder().encodeToString(rawId);

  }

  public boolean newPlayer (final String name) throws SQLException {

    // Si el jugadr ya está registrado, se devuelve false.

    // El identificador de un jugador se pide al método getRandomId().
    // Es necesario un bucle que vaya generando identificadores hasta
    // dar con uno que todavía no haya sido empleado.

  }

  public List<String> getPlayers () throws SQLException {
    final List<String> players = new ArrayList<>();

    // TODO

    return players;
  }

  String getPlayerId (final String name) throws SQLException {

    // TODO

  }

  String getPlayerName (final String playerId) throws SQLException {

    // TODO

  }

}

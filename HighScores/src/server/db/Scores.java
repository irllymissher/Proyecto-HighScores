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
//
public final class Scores {
  
  static private final String CLASS_NAME = Scores.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

  private final Connection connection;
  
  public Scores () {
    this.connection = DBComponent.getConnection();   
  }

  public boolean newScore (final String playername,
                           final String gamename,
                           final int score) throws SQLException {
    
    // Se obtiene identificador de jugador con nombre playername.

    // Se obtiene identificador de juego con nombre gamename.

    // Se inserta puntuación.

  }
  
  public Map<String, Integer> getGameScores (final String gamename)
                                 throws SQLException {
    
    final Map<String, Integer> scores = new LinkedHashMap<>();
    
    // TODO
    
  }
  
  public Map<String, Integer> getPlayerScores (final String playername)
                                 throws SQLException {
    
    final Map<String, Integer> scores = new LinkedHashMap<>();   
    
    // TODO
    
  }  
  
}

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
  
  public Scores () {
    this.connection = DBComponent.getConnection();   
    this.p = new Players();
    this.g = new Games();
  }

  public boolean newScore (final String playername,
                           final String gamename,
                           final int score) throws SQLException {
    
      
      
    // Se obtiene identificador de jugador con nombre playername.
    final String idJugador = p.getPlayerId(playername);
   
    // Se obtiene identificador de juego con nombre gamename.
    final String idJuego = g.getGameId(gamename);   /* Ya convertido en Mayusculas */

    // Se inserta puntuación.
    final String consulta =" insert into Scores (PlayerId, GameId, Score) values(?,?, ?)";
    if (playername == null || gamename == null)
    {
        return false;
    }
    if (idJugador.isEmpty() ||
            idJuego.isEmpty())
    {
        return false;
    }
    
    try( final PreparedStatement preparedStatementInsertarPuntuaje
            = connection.prepareStatement(consulta))
    {
        preparedStatementInsertarPuntuaje.setString(2, idJuego);
        preparedStatementInsertarPuntuaje.setString(1, idJugador);
        
        preparedStatementInsertarPuntuaje.setInt(3, score);
        
        
        
        final int filasAfectadas = preparedStatementInsertarPuntuaje.executeUpdate();
        
        return filasAfectadas > 0;
    }
    catch( final SQLException e)
    {
        LOGGER.log(Level.WARNING, "Fallo al insertar el puntuaje ", e);
        throw e;
    }
    

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

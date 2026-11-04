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
import java.sql.PreparedStatement;
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

    /* 1) Voy a comprobar si el jugador existe por su nombre */
    /* logicamente si no esta registrado devuevlo false*/
    /* 2) Usar un bucle para veriricar un ID que no este en la */
    /* base de datos */
    /* 3) Ahora insertamos al jugador */
  public boolean newPlayer (final String name) throws SQLException 
  {

    // Si el jugadr ya está registrado, se devuelve false.

    // El identificador de un jugador se pide al método getRandomId().
    // Es necesario un bucle que vaya generando identificadores hasta
    // dar con uno que todavía no haya sido empleado.
    
    String identificadorJugador;
    
    final String consultarInsertarJugador;
    consultarInsertarJugador = "insert into Players(PlayerId, PlayerName) values (?, ?)";
    
    /**
    * En vez de crear un try-catch puedo reutilizar el metodo
    * que he creado. Al llamar a getPlayerId me va a devolver
    * el id si existe y si no pues vacio
    * logicamente si existe entonces devuelvo falso
    */
    if (! "".equalsIgnoreCase(getPlayerId(name)))
    {
        return false;
    }
    
    /**
     * Genero ids al azar hasta que pueda encontrar uno que no
     * tenga ningun jugador, osea, que getPlayerName me devuelva ""
     */
    do
    {
        identificadorJugador = getRandomId();
       
    }while(! "".equalsIgnoreCase(getPlayerName(identificadorJugador)));
    
    try (final PreparedStatement preparedStatementInsertarJugador = connection.prepareStatement(consultarInsertarJugador))
    {
        preparedStatementInsertarJugador.setString(1, identificadorJugador);
        preparedStatementInsertarJugador.setString(2, name);
        
        /**
         * El enunciado me pide que haga return true o false segun si se ha modificado
         * la tabla o no , verifico las filas que han sido modificadas por el insert
         * si sale mayor que cero entonces algo fue modificado, si no pues nada
        */
        final int filasAfectadas = preparedStatementInsertarJugador.executeUpdate();
        return filasAfectadas>0;
    }
    catch (final SQLException e) 
    {
        LOGGER.log(Level.SEVERE,"fallo al insertar jugador en la bbdd ",e);
        throw e;
    }
  }

  public List<String> getPlayers () throws SQLException {
    final List<String> players = new ArrayList<>();

    // TODO
    final String consultarJugadoresSQL = "select PlayerName from Players";
    try (final PreparedStatement preparedStatementJugadores = connection.prepareStatement(consultarJugadoresSQL))
    {
        final ResultSet resultadoBusquedaJugadores = preparedStatementJugadores.executeQuery();
        while (resultadoBusquedaJugadores.next())
        {
            final String nombreDelJugador;
            nombreDelJugador = resultadoBusquedaJugadores.getString("PlayerName");
            players.add(nombreDelJugador);
        }
        return players;
    }
    catch (final SQLException e) 
    {
        LOGGER.log(Level.SEVERE, "Fallo al obtener la lista de todos los jugadores", e);
        throw e;
    }
  }

  String getPlayerId (final String name) throws SQLException 
  {

    final String consultarNombreJugadorSQL;
    consultarNombreJugadorSQL = "select PlayerId from Players where PlayerName =?";
    
    try(final PreparedStatement preparedStatementJugador = connection.prepareStatement(consultarNombreJugadorSQL))
    {
        preparedStatementJugador.setString(1, name);
        
        try (final ResultSet resultadoBusqueda = preparedStatementJugador.executeQuery())
        {
            if (resultadoBusqueda.next())
            {
                final String idJugador;
                idJugador = resultadoBusqueda.getString("PlayerId");
                return idJugador;
            }
            else
            {
                return "";
            }
        }
    }
    catch (final SQLException e)
    {
        LOGGER.log(Level.SEVERE, "consulta mal ejecutada: busqueda id del nombre del jugador ", e);
        throw e;
    }
  }

  String getPlayerName (final String playerId) throws SQLException /* Un semi copypaste del metodo anterior */
  {
    final String consultarNombreJugadorSQL;
    consultarNombreJugadorSQL = "select PlayerName from Players where PlayerId=?";
    
    try(final PreparedStatement preparedStatementJugador = connection.prepareStatement(consultarNombreJugadorSQL))
    {
        preparedStatementJugador.setString(1, playerId);
        
        try (final ResultSet resultadoBusqueda = preparedStatementJugador.executeQuery())
        {
            if (resultadoBusqueda.next())
            {
                final String nombreJugador;
                nombreJugador = resultadoBusqueda.getString("PlayerName");
                return nombreJugador;
            }
            else
            {
                return "";
            }
        }
    }
    catch (final SQLException e)
    {
        LOGGER.log(Level.SEVERE, "consulta mal ejecutada: busqueda del nombre del jugador ", e);
        throw e;
    }
  }

}

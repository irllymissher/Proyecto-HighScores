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
    boolean existeYaElIdentificador;
    
    final String consultaComprobarNuevoJugador;
    final String consultaComprobaridJugador;
    final String consultarInsertarJugador;
    
    consultaComprobarNuevoJugador = "select count(*) from Players where PlayerName=?";
    consultaComprobaridJugador = "select count(*) from Players where PlayerId = ?";
    consultarInsertarJugador = "insert into Players(PlayerId, PlayerName) values (?, ?)";
    
    try(final PreparedStatement preparedStatementNuevoJugador = connection.prepareStatement(consultaComprobarNuevoJugador))
    {
        preparedStatementNuevoJugador.setString(1, name);
        try (ResultSet resultadoConsulta = preparedStatementNuevoJugador.executeQuery())
        {
            if (
                    resultadoConsulta.next() 
                    &&
                    resultadoConsulta.getInt(1) > 0
               )
            {
                return false;
            }
        }
    }
    
    try(final PreparedStatement preparedStatementIdJugador = connection.prepareStatement(consultaComprobaridJugador))
    {
        do
        {
            identificadorJugador = getRandomId();
            preparedStatementIdJugador.setString(1, identificadorJugador);
            
            
            try(ResultSet resultadoConsulta = preparedStatementIdJugador.executeQuery())
            {
                if (
                    resultadoConsulta.next() 
                    &&
                    resultadoConsulta.getInt(1) > 0
                   )
                {
                    existeYaElIdentificador = true;
                }
                else
                {
                    existeYaElIdentificador = false;
                }
            }
        }while(existeYaElIdentificador);
    }
    try (PreparedStatement preparedStatementInsertarJugador = connection.prepareStatement(consultarInsertarJugador))
    {
        preparedStatementInsertarJugador.setString(1, identificadorJugador);
        preparedStatementInsertarJugador.setString(2, name);
        preparedStatementInsertarJugador.executeUpdate();
    }
      return true;
  }

  public List<String> getPlayers () throws SQLException {
    final List<String> players = new ArrayList<>();

    // TODO

    return players;
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
    }catch (final SQLException e)
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
    }catch (final SQLException e)
    {
        LOGGER.log(Level.SEVERE, "consulta mal ejecutada: busqueda del nombre del jugador ", e);
        throw e;
    }
  }

}

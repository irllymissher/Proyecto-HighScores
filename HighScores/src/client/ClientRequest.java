package client;
/**
 *
 * @author MAZ
 */
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
//
import client.request.NewPlayer;
import client.request.NewScore;
import client.request.NewGame;
import client.request.GameList;
import client.request.PlayerList;
import client.request.PlayerScores;
import client.request.GameScores;
//
final class ClientRequest {
  
  private final InetAddress serverIP;
  private final int port;
  
  ClientRequest (final InetAddress serverIP, final int port) {
    this.serverIP = serverIP;
    this.port = port;
  }
  
  boolean newPlayer (final String playername) {
    final NewPlayer requester = new NewPlayer(serverIP, port);
    return requester.request(playername);
  }
  
  List<String> playerList () {
    final PlayerList requester = new PlayerList(serverIP, port);
    return requester.request();
  }
  
  boolean newGame (final String gamename) {
    final NewGame requester = new NewGame(serverIP, port);
    return requester.request(gamename);
  }
  
  List<String> gameList () {
    final GameList requester = new GameList(serverIP, port);
    return requester.request();
  }
  
  boolean newScore (final String playername, final String gamename, final int score) {
    final NewScore requester = new NewScore(serverIP, port);
    return requester.request(playername, gamename, score);
  }
  
  Map<String, Integer> scoresByPlayer (final String playername) {
    final PlayerScores requester = new PlayerScores(serverIP, port);
    return requester.request(playername);
  }
  
  Map<String, Integer> scoresByGame (final String gamename) {
    final GameScores requester = new GameScores(serverIP, port);
    return requester.request(gamename);
  }
  
}

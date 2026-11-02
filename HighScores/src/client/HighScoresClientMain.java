package client;
/**
 *
 * @author MAZ
 */
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
//
final class HighScoresClientMain {

  /**
   * @param args the command line arguments
   */
  public static void main (final String[] args) {
    
    final ClientRequest request;
    try {
      final InetAddress serverIP = InetAddress.getLocalHost();
      final int port = Integer.parseInt(args[1]);
      if (port < 600) {
        System.out.println("Número de puerto demasiado bajo");
        return;
      }
      request = new ClientRequest(serverIP, port);
    } catch (final UnknownHostException ex) {
      System.err.println("Problema en instanciación de IP de servidor");
      return;
    } catch (final NumberFormatException ex) {
      System.err.println("Uso: <serverIP: IP> <port: int>");
      return;
    }

    try (Scanner scanner = new Scanner(System.in)) {

      int opcion;
      do {

        System.out.println("Opciones:");
        System.out.println("  1 - Añadir nueva puntuación");  
        System.out.println("  2 - Añadir nuevo jugador");
        System.out.println("  3 - Añadir nuevo juego");        
        System.out.println("  4 - Consultar puntuaciones por jugador");
        System.out.println("  5 - Consultar puntuaciones por juego");
        System.out.println("  6 - Listar jugadores");
        System.out.println("  7 - Listar juegos");
        System.out.println("  0 - Salir");
        System.out.print("Introduce opcion: ");

        try {
          
          opcion = scanner.nextInt();
          scanner.nextLine();

          switch (opcion) {
            
            case 1: {
              final String[] data = getNewHighScoreInputData(scanner);
              final String playername = data[0];
              final String gamename = data[1];
              final int score = Integer.parseInt(data[2]);
              final boolean result = request.newScore(playername, gamename, score);
              if (result) {
                System.out.println("Nueva puntuación almacenada");
              } else {
                System.out.println("Revise que jugador y juego estén registrados");
              }
              break;
            }

            case 2: {
              final String playername = getPlayerInputData(scanner);
              final boolean result = request.newPlayer(playername);
              if (result) {
                System.out.println("Nuevo jugador registrado");
              } else {
                System.out.println("Jugador " + playername + " probablemente ya esté registrado");
              }
              break;
            }    

            case 3: {
              final String gamename = getGameInputData(scanner);
              final boolean result = request.newGame(gamename);
              if (result) {
                System.out.println("Nuevo juego registrado");
              } else {
                System.out.println("Juego " + gamename + " probablemente ya esté registrado");
              }
              break;
            }
            
            case 4: {
              final String playername = getPlayerInputData(scanner);
              final Map<String, Integer> scores = request.scoresByPlayer(playername);
              if (!scores.isEmpty()) {
                System.out.println("Puntuaciones del jugador " + playername + ": ");
                for (final String name: scores.keySet())
                  System.out.println("\t" + name + ": " + scores.get(name));
              } else {
                System.out.println("No hay puntuaciones para el jugador "  + playername);
              }
              break;
            }

            case 5: {
              final String gamename = getGameInputData(scanner);
              final Map<String, Integer> scores = request.scoresByGame(gamename);
              if (!scores.isEmpty()) {
                System.out.println("Puntuaciones del juego " + gamename + ": ");
                for (final String name: scores.keySet())
                  System.out.println("\t" + name + ": " + scores.get(name));
              } else {
                System.out.println("No hay puntuaciones para el juego "  + gamename);
              }
              break;
            }            

            case 6: {
              final List<String> players = request.playerList();
              if (!players.isEmpty()) {
                System.out.println("Listado de jugadores: ");
                for (final String name: players)
                  System.out.println("\t" + name);
              } else {
                System.out.println("No hay jugadores");
              }
              break;
            }

            case 7: {
              final List<String> games = request.gameList();
              if (!games.isEmpty()) {
                System.out.println("Listado de juegos: ");
                for (final String name: games)
                  System.out.println("\t" + name);
              } else {
                System.out.println("No hay juegos");
              }
              break;
            }

            default: {
            }
          }

        } catch (final InputMismatchException ex) {
          scanner.nextLine();
          opcion = Integer.MAX_VALUE;
        }

      } while (opcion != 0);
    
    }

  }
  
  static private String[] getNewHighScoreInputData (final Scanner scanner) {
    final String[] data = new String[3];
    data[0] = getPlayerInputData(scanner);
    data[1] = getGameInputData(scanner);    
    data[2] = getHighScoreInputData(scanner);        
    return data;
  }
  
  static private String getPlayerInputData (final Scanner scanner) {
    System.out.print("Introduzca nombre de jugador: ");
    final String data = scanner.nextLine().trim();
    return data;
  }
  
  static private String getGameInputData (final Scanner scanner) {
    System.out.print("Introduzca nombre de juego: ");
    final String data = scanner.nextLine().trim();
    return data;
  }
  
  static private String getHighScoreInputData (final Scanner scanner) {
    System.out.print("Introduzca puntuación: ");
    final String data = Long.toString(scanner.nextLong());
    scanner.nextLine();
    return data;
  }    

}

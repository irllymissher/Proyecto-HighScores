package server;
/**
 *
 * @author MAZ
 */
import java.util.InputMismatchException;
import java.util.Scanner;
//
public final class HighScoresServerMain {

  static public void main (final String[] args) {

    final HighScoresServer server = new HighScoresServer();
    // Se arrancan componentes del servidor
    try {
      server.start();
    } catch (final Exception ex) {
      System.err.println("Problema en arranque de aplicación");
      return;
    }

    //
    // El menú sigue operando en la hebra de ejecución de arranque;
    // los diferentes servicios se ejecutan concurrentemente en otras hebras.
    //
    menu();

    //
    // Tras salir del menú, se ordena detener componentes del servidor
    //
    server.stop();

  }

  static private void menu () {

    final Scanner scanner = new Scanner(System.in);

    int election;
    do {

      System.out.println("Opciones:");
      System.out.println(" 0 - Detener servidor");
      System.out.print("Introduce opción: ");

      try {
        election = scanner.nextInt();
      } catch (final InputMismatchException ex) {
        election = Integer.MAX_VALUE;
      }
      scanner.nextLine();

    } while (election != 0);

  }

}

package server;
/**
 *
 * @author MAZ
 */
import server.db.DBComponent;
import server.handler.RequestComponent;
//
final class HighScoresServer {

  void start () {
    try {
      RequestComponent.start();
    } catch (final Exception ex) {
      System.err.println("Problema al arrancar componente de peticiones");
      return;
    }
    try {
      DBComponent.start();
    } catch (final Exception ex) {
      System.err.println("Problema al arrancar componente de base de datos");
    }
  }
  
  void stop () {
    RequestComponent.stop();
    DBComponent.stop();
  }
  
}

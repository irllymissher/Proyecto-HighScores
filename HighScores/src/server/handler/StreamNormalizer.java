package server.handler;

/**
 *
 * @author MAZ
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
//

final class StreamNormalizer {

  static private final String CLASS_NAME = StreamNormalizer.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

  static InputStream normalize(final InputStream is) {

    try {
      final PipedInputStream in = new PipedInputStream();
      final PipedOutputStream out = new PipedOutputStream(in);
      ForkJoinPool.commonPool().submit(() -> {

        try ( Scanner reader = new Scanner(is)) {

          //final Charset charset = Charset.defaultCharset();
          while (reader.hasNext()) {
            final String line = reader.nextLine();
            final String nLine = Normalizer.normalize(line, Normalizer.Form.NFKC);
            out.write(nLine.getBytes());
          }

        } catch (final IOException ex) {
          LOGGER.log(Level.INFO, "problema de I/O en normalización de cuerpo de petición", ex);
        }
      });

      return in;
    } catch (final IOException ex) {
      LOGGER.log(Level.INFO, "problema de I/O en normalización de cuerpo de petición", ex);
      return null;
    }

  }

}

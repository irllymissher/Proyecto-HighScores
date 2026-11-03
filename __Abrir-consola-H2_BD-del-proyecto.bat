@echo off
REM Abre la consola web de H2 para la BD del PROYECTO (highscoresDB). Se abre sola en el navegador.
REM -ifNotExists: deja que la BD se cree la primera vez que te conectas (si no, H2 da "Database not found").
REM IMPORTANTE: cierra esta ventana negra ANTES de arrancar el servidor en NetBeans (H2 bloquea el fichero).
java -cp "%~dp0HighScores\lib\h2-2.5.252.jar" org.h2.tools.Console -web -browser -ifNotExists

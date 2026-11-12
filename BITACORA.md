## 2026-11-02
- Hecho:
	- Estuve leyendo toda la practica, sobre todo las paginas 4 y 5 que son las que
	tenemos que implementar
	- Estuve trasteando H2 con ejemplos de clase anteriormente
	- Instalé el netbeans 12.6 porque tenia el 8.2
- Fuentes: Enunciado de entrega
- Dudas / decisiones:
	- Realice un diagrama de clases inicial para saber como estaban relacionadas las 
	clases entre si
	- Hice mi mapa para saber que ir haciendo cada dia, hoy simplemente era revision 
	y saber que hacer para los proximos dias
	
## 2026-11-03
- Hecho:
	- He realizado el P.4.1, las tablas: Players, Games y Scores
	- He probado cada una realizando consultas e insercciones y funcionan correctamente
	- Recordar no poner `,` y `;` en donde no debes 
- Fuentes: Enunciado de Entrega, Documentacion de H2
- Dudas / Decisiones:
	- PlayerId y GameId me parece que nopeuden ser la clave primaria de Scores porque un jugador
	puede tener varias puntuaciones del mismo juego (en el P.7 te dice que debes de elegir la 
	puntuacion más alta entre las que ha realizado) y esto lo comprobe porque hice que un usuario
	tuviera una puntuacion menor y luegon una mas grande 
	- Tambien noté que PlayerName y GaneName no tienen UNIQUE y puede ocurrir que un nombre repetido
	entre y segun lo leido en la practica esto se comprobaria en newPlayer y newGame 
	- Score no lleva NULL, pero me parece absurdo porque no se pueden tener puntuaciones vacias
	- Podriamos haber puestoun Check en el Score, para que sea positivo, pero se valida en Java

## 2026-11-04
- Hecho:
  - Realizado los 4 metodos de Players, el primero de todos fue getPlayerId porque dependen de él 
  asi sali todo mas facil
- Fallos:
  - tuve que cambiar los null por "" porque asi me lo pedia la practica
  - no recordaba que tenia que poner los LOGGER hasta que los he buscado
  - los close() no hacen falta porque ya los try-catch los implementa
  - bucle infinito por poner " " 
- Fuentes: Enunciado y practicas del profesor sobre todo los try-catch y logger.log
- Dudas/Decisiones:
  - newPlayer reutiliza getPlayerId y getPlayerNameen lugar de repetir las consultas
  - elegi while porque me recuerda mas al esquema de recorrido de estructuras de datos que el do while
  aunque em haya salido de manera mas natural
  - la validacion del nombre va en el handler y no en players
  - Java no te detecta los nombres de las tablas mal escritos 
## 2026-11-05

## 2026-11-08 (parte 1)
- Hecho:
	- Realizado los 3 metodfos de scores, efectivamente server.db finalizado 
- Fallos:
	- No les habia dado a Players y Games valor en el constructor
	- No concatné de manera correcta los sql
- Fuentes: enunciado
- Dudas/Decisiones
	- Uso join para evitarme una segunda consulta
	- Sacamos el id primero
	- La puntuacion positiva se valida en el handler, no en Scores
	- La firma usa INT y el enunciado dice LONG

## 2026-11-08 (parte 2)
- Hecho:
  - Realizado PlayerRequestHandler: logging, responderSinContenido, handlePutRequest
  y handleGetRequest
  - Comentado de donde sale cada parte (enunciado, SortingHandler, teoria)
 - Fallos:
  - en logging puse 3 %s para 4 datos y String.format se comia la descripcion sin avisar
  - no guardaba el resultado de newPlayer y no escribia nada con writeBoolean
  - puse throws SQLException en handlePutRequest y rompia el handle del profe
  - el if (hasNextLine()) dejaba la variable sin valor; nextLine() ya lanza la excepcion
  - intente atrapar IOException leyendo con Scanner, pero Scanner nunca la lanza
  - el GET lo hice en binario (writeInt/writeUTF, como SortingHandler) y el cliente
  lee lineas de texto, tiene que ir con OutputStreamWriter
  - parentesis y llave de mas
 - Fuentes: 
	- enunciado p.2, p.4, p.5 y p.7; SortingHandler; Anexo 1 p.13-14;
	Tema 2b p.2, p.4, p.6 y p.7; documentacion de HttpExchange (Oracle)
- Dudas/Decisiones:
  - no uso StreamNormalizer porque lo probe y pega las lineas y se queda colgado;
  leo con Scanner, normalizo cada linea a NFKC y saco los tokens con un segundo
  Scanner (preguntar al profe si se puede corregir el suyo)
  - logging lo pide la p.7 aunque no venga en el esqueleto; registra direccion,
  metodo, ruta y descripcion, pero no el nombre que manda el usuario
  - responderSinContenido para no repetir el try-catch del 204 (sendResponseHeaders
  lanza IOException y los metodos no tienen throws)
  - separo leer+procesar de responder: despues de enviar el 200 ya no puedo mandar
  el 204, en ese catch solo registro
  - rechazo lo que sobre despues del nombre (residuo)
  - Players se crea dentro del metodo porque RequestComponent arranca antes que la BD
  

## 2026-11-10
- Hecho:
  - GameRequestHandler y el del Score realizados completamente y los 7 servicios
  que pide el enunciado estan resueltos completamente 
- Fallos:
  - en el POST leia solo la primera linea del cuerpo y buscaba los 3 campos en ella;
  el cliente manda 3 lineas (playername, gamename, score), hay que leer una linea
  del cuerpo por cada campo
  - al copiar el bloque de score deje next("gamename") en vez de next("score")
  - declare las lineas normalizadas de juego y puntuacion pero seguia escaneando
  la del jugador
  - en los GET de puntuaciones escribi "gamename: " sin espacio antes de los dos
  puntos y el cliente no lo entiende (hace next(":") como token aparte)
  - tenia los catch intercambiados: el 204 iba en el de IOException, cuando el
  200 ya estaba enviado
- Fuentes:
  - Cliente del profe y pdf
- Dudas/Decisiones:
  - los nombres de juego pueden llevar espacios: leo el resto de la linea con
  nextLine().trim(), como hace el cliente PlayerScores l.81; por eso en juegos
  no compruebo residuo y en jugadores si
  - la puntuacion la leo con nextInt(): si no es un numero o no cabe en un int lanza
  InputMismatchException, que es hija de NoSuchElementException, asi que no
  necesito un catch aparte ni Integer.parseInt
  - no paso el nombre del juego a mayusculas en el handler, ya lo hace Games
  - para escribir la puntuacion uso String.valueOf: Writer.write(int) escribe un
  caracter, no el numero
  - pendiente para la validacion: puntuaciones negativas (dije el 8-nov que se
  validaban en el handler y aun no esta), /score sin consulta y ?playername= vacio
  hacen fallar el handleGetRequest del profesor

  ## 2026-11-10 (a la tarde)
- Hecho:
  - Puse los comentarios y javadoc correspondientes encima de los metodos y aclarando
  ciertas lineas que podrian dar a ambiguedades
  - Formato realizado a mi manera de hacer codigo con opciones de NetBeans
  - puntuacion hehca para que lea de 1 a 10 digitos con Integer.parseInt
- Fallos:
  - usé nextInt() por leer documentaicon en vez de lo que se me pedia integramente
- Ddudas/Decisiones:
  - 0 cero no cuenta como nada asi que apartir de 1 es positivo para mi

## 2026-11-11 
- Hecho
  - Validacion para el get de /scores, escrito por MAZ, no cambie las lineas del profe
  simplemente añadi comprobaciones
  - Hice los patrones del jugador y del juego validando con Pattern/Matcher//matches() 
  - valido el nombre, de las puntuaciones, que llegan por URL antes de ir a la BD
- Fallos:
  - me di cuenta que el metodo get del profesor MAZ no respondia ?playername= por lo que
  se perdia la peticion (arreglado, por supuesto)
  - los comodines fueron elimimnados de las regex
  - matcher() no se hace sobre un String, si se hace sobre un Pattern
- Decisiones/Dudas:
  - Hay metodos que no se pueden modfiicar como RequestComponent o DBComponent, en
  otras partes del codigo, escritas por el profesor, decidí no tocarla si no mas bien
  hacer comprobaciones pertinentens a lo escrito
  - isEmpty() queda comentado porque el patron ya rechaza el nombre vacio

# 2026-11-12 (linux)
- Hecho:
  - Arranque el servidor realizando distintas pruebas de funcionamiento
  - Probe nombres que podria aceptar y rechar con un 204
  - Realice pruebas en terminal lanzando curls con peticiones que el cliente no dejaria mandar
    /score sin consulta, abc, 11 digitos
- Fallos/Descubrimientos:
  - El servidor escuchaba un puerto en concreto y no en localhost
  - probando digitos vi que con 11 digitos se caiga, no es que pueda solucionarlo
  porque es codigo del profesor 
- Dudas/Decisiones:
  - Hay que validad lo que sale de la BD, incluidas las filas metidas a mano

# 2026-11-12 (casa)
- Hecho
  - revise el codgo de games y cambie una comprobacion de toUpperCase
  - quitado los comentarios //todo
  - renomnbrado de variables y comentarios de acorde a Clean Code y docuemntracion
  oficial de Java
- Fallos:
  - Metí, sin darme cuenta, el bucle comprobacion en el PUT en vez de GET
  - patron cruzado en getGamesScore como el pasado 11 de noviembre
  - me di cuenta que isEmpty no evita que haya un nullpointerexception porque tambien
  es un metodo
- Dudas/Decisiones:
  - valiod nombres, no puntuaciones por ser integer
  - sin @param para los javadoc porque no se que ponerles
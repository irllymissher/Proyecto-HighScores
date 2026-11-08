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

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
	
##2026-11-03
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
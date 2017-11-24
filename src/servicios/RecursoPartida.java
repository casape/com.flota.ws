package servicios;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import modelo.Partida;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("partidas")
public class RecursoPartida {
	// Diccionario con las partidas jugadas por los distintos clientes
	private Map<Integer, Partida> partidaDB = new ConcurrentHashMap<Integer, Partida>();
	// contador para construir los identificadores únicos de las partidas
	// guardadas
	private AtomicInteger idCounter = new AtomicInteger();

	/**
	 * Constructor por defecto
	 */
	public RecursoPartida() {
		super();
		System.out.println("construyo RecursoPartida");
	}

	/**
	 * Crea una nueva partida y la almacena en el diccionario
	 * 
	 * @param filas
	 *            filas del tablero
	 * @param columnas
	 *            columnas del tablero
	 * @param barcos
	 *            barcos en el tablero
	 * @param uriInfo
	 *            URI absoluta al nuevo recurso extraida del contexto
	 * @return cuerpo vacío y URI del recurso con la partida recien creada en la
	 *         cabecera Location
	 */
	@POST
	@Path("/{filas}/{columnas}/{barcos}")
	public Response nuevaPartida(@PathParam("filas") int filas, @PathParam("columnas") int columnas,
			@PathParam("barcos") int barcos, @Context UriInfo uriInfo) {
		Partida partida = new Partida(filas, columnas, barcos);
		int id = idCounter.incrementAndGet();
		partidaDB.put(id, partida);

		// Construye la respuesta incluyendo la URI absoluta al nuevo recurso
		// partida
		// Obtiene la ruta absoluta de la información de contexto inyectada
		// mediante @Context al método
		UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
		URI newURI = uriBuilder.path("partidas/" + id).build();

		// El método created añade el URI proporcionado a la cabecera 'Location'
		ResponseBuilder response = Response.created(newURI);
		// Devuelve el estado 201 indicando que la partida se ha CREATED con
		// éxito
		return response.status(Response.Status.CREATED).build();
	}

	/**
	 * Borra una partida del diccionario
	 * 
	 * @param idPartida
	 *            identificador de la partida
	 * @return cuerpo vacío y estado indicando si se ha podido borrar la partida
	 */
	@DELETE
	@Path("/{idPartida}")
	public Response borraPartida(@PathParam("idPartida") int idPartida) {
		if (partidaDB.remove(idPartida) == null)
			return Response.status(Response.Status.NOT_FOUND).build();
		else {
			ResponseBuilder builder = Response.ok();
			return builder.build();
		}
	}

	/**
	 * Prueba una casilla, y devuelve el resultado
	 * 
	 * @param idPartida
	 *            identificador de la partida
	 * @param fila
	 *            fila de la casilla
	 * @param columna
	 *            columna de la casilla
	 * @return cuerpo conteniendo el resultado de probar la casilla
	 */
	@PUT
	@Path("/{idPartida}/casilla/{fila},{columna}")
	public Response pruebaCasilla(@PathParam("idPartida") int idPartida, @PathParam("fila") int fila,
			@PathParam("columna") int columna) {
		int resultado = partidaDB.get(idPartida).pruebaCasilla(fila, columna);
		ResponseBuilder builder = Response.ok(resultado); // TODO resultato como
															// string?
		return builder.build();
	}

	/**
	 * Obtiene los datos de un barco.
	 * 
	 * @param idPartida
	 *            identificador de la partida
	 * @param idBarco
	 *            identificador del barco
	 * @return cuerpo conteniendo la cadena con informacion sobre el barco
	 *         "fila#columna#orientacion#tamanyo"
	 */
	@GET
	@Path("/{idPartida}/barco/{idBarco}")
	@Produces("text/plain")
	public Response getBarco(@PathParam("idPartida") int idPartida, @PathParam("idBarco") int idBarco) {
		String resultado = partidaDB.get(idPartida).getBarco(idBarco);
		ResponseBuilder builder = Response.ok(resultado);
		return builder.build();
	}

	/**
	 * Devuelve la informacion sobre todos los barcos
	 * 
	 * @param idPartida
	 *            identificador de la partida
	 * @return cuerpo conteniendo la codificación XML de la solución
	 */
	@GET
	@Path("/{idPartida}/solucion")
	@Produces("text/plain")
	public Response getSolucion(@PathParam("idPartida") int idPartida) {
		String[] solucion = partidaDB.get(idPartida).getSolucion();
		
		String solucionXML = solucionAXML(solucion,solucion.length);
		
		ResponseBuilder builder = Response.ok(solucionXML);
		return builder.build();
	}

	/**
	 * Construye una cadena con el código XML que contiene la solución de la
	 * partida
	 * 
	 * @param solucion
	 *            vector de cadenas con la solución
	 * @param numBarcos
	 *            número de barcos en la partida
	 * @return cadena con el código XML conteniendo la solución
	 */
	protected String solucionAXML(String[] solucion, int numBarcos) {
		StringBuilder str = new StringBuilder();
		str.append("<solucion tam=\"" + numBarcos + "\">");
		for (int i = 0; i < numBarcos; i++)
			str.append("<barco>" + solucion[i] + "</barco>");
		str.append("</solucion>");
		return str.toString();
	}

}

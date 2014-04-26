/**
 * 
 */
package servidor;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.Security;
import java.util.Properties;
import java.util.concurrent.Semaphore;

/**
 * Esta clase implementa el pool de conexiones para el servidor. Infraestructura
 * Computacional 201320 Universidad de los Andes. Las tildes han sido eliminadas
 * por cuestiones de compatibilidad.
 * 
 * @author Michael Andres Carrillo Pinzon
 */
public class Pool {

	/**
	 * Constante que especifica el numero de threads que se usan en el pool de
	 * conexiones.
	 */
	public static int n_threads;

	/**
	 * Ubicacion del properties (debe ser relativo al Script)
	 */
	private static final String PROPERTIES = "./InfracompNoSeguridad/servidor.properties";

	/**
	 * Puerto en el cual escucha el servidor.
	 */
	public static final int PUERTO = 5555;

	/**
	 * Metodo main del servidor con seguridad que inicializa un pool de threads
	 * determinado por la constante nThreads.
	 * 
	 * @param args
	 *            Los argumentos del metodo main (vacios para este ejemplo).
	 * @throws IOException
	 *             Si el socket no pudo ser creado.
	 */
	public static void main(String[] args) throws IOException {

		// Crea el socket que escucha en el puerto seleccionado.
		ServerSocket socket = new ServerSocket(PUERTO);

		// Crea un semaforo que da turnos para usar el socket.
		Semaphore semaphore = new Semaphore(1);

		// Obtiene la cantidad de threads del archivo properties
		Properties p = new Properties();
		p.load(new FileInputStream(PROPERTIES));
		n_threads = Integer.parseInt(p.getProperty("n_threads"));

		// Genera n threads que correran durante la sesion.
		ThreadServidor[] threads = new ThreadServidor[n_threads];
		for (int i = 0; i < n_threads; i++) {
			try {
				threads[i] = new ThreadServidor(socket, semaphore);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out
				.println("El servidor sin seguridad esta listo para aceptar conexiones con "
						+ n_threads + " threads.");
	}

}

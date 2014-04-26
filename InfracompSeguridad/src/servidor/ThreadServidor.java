/**
 * 
 */
package servidor;

import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.cert.CertificateNotYetValidException;

import utils.Seguridad;
import utils.Transformacion;

/**
 * Esta clase implementa cada thread del servidor. Cada vez que un cliente crea una conexion 
 * al servidor, un thread se encarga de atenderlo el tiempo que dure la sesion.
 * Infraestructura Computacional 201320
 * Universidad de los Andes.
 * Las tildes han sido eliminadas por cuestiones de compatibilidad.
 * @author Michael Andres Carrillo Pinzon
 */
public class ThreadServidor extends Thread{

	//----------------------------------------------------
	//CONSTANTES PARA LA DEFINICION DEL PROTOCOLO
	//----------------------------------------------------
	public static final String STATUS = "STATUS";
	public static final String OK = "OK";
	public static final String ALGORITMOS = "ALGORITMOS";
	public static final String DES = "DES";
	public static final String AES = "AES";
	public static final String BLOWFISH = "Blowfish";
	public static final String RSA = "RSA";
	public static final String RC4 = "RC4";
	public static final String HMACMD5 = "HMACMD5";
	public static final String HMACSHA1 = "HMACSHA1";
	public static final String HMACSHA256 = "HMACSHA256";
	public static final String CERTSRV = "CERTSRV";
	public static final String SEPARADOR = ":";
	public static final String INIT = "INIT";
	public static final String AUT = "AUT";
	public static final String STATTUTELA = "STATTUTELA";
	public static final String RESULTADO = "RESULTADO";
	public static final String INFO = "INFO";
	public static final String ERROR = "ERROR";
	public static final String ERROR_FORMATO = "Error en el formato. Cerrando conexion";
	
	/**
	 * Formato de los datos de las cadenas manejadas en el protocolo.
	 */
	public static final String COD = "US-ASCII";


	/**
	 * El socket que permite recibir requerimientos por parte de clientes.
	 */
	private ServerSocket socket;

	/**
	 * El semaforo que permite tomar turnos para atender las solicitudes.
	 */
	private Semaphore semaphore;

	/**
	 * Metodo que inicializa un thread y lo pone a correr.
	 * @param socket El socket por el cual llegan las conexiones.
	 * @param semaphore Un semaforo que permite dar turnos para usar el socket.
	 * @throws InterruptedException Si hubo un problema con el semaforo.
	 */
	public ThreadServidor(ServerSocket socket, Semaphore semaphore) throws InterruptedException {
		this.socket = socket;
		this.semaphore = semaphore;
		this.start();
	}

	/**
	 * Metodo que atiende a los usuarios.
	 */
	@Override
	public void run() {

		while (true) {
			Socket s = null;

			try {
				//////////////////////////////////////////////////////////////////////////
				// Recibe una conexion del socket.
				//////////////////////////////////////////////////////////////////////////
				semaphore.acquire();
				try {
					s = socket.accept();
				} catch (IOException e) {
					e.printStackTrace();
					semaphore.release();
					try {
						s.close();
					} catch (Exception e1) {
						// DO NOTHING
					}
					continue;
				}
				semaphore.release();
				
				System.out.println("Se recibe a un cliente.");

				// Inicializa el lector y escritor del socket.
				PrintWriter writer = new PrintWriter(s.getOutputStream() , true);
				BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));

				//////////////////////////////////////////////////////////////////////////
				// Recibe HOLA y el identificador del cliente.
				// En caso de error de formato, cierra la conexion.
				//////////////////////////////////////////////////////////////////////////
				String linea = reader.readLine();
				if (!linea.equals(INIT)) {
					writer.println(ERROR_FORMATO);
					throw new FontFormatException(linea);
				}

				//////////////////////////////////////////////////////////////////////////
				// Envia el status del servidor
				//////////////////////////////////////////////////////////////////////////
				writer.println(STATUS + SEPARADOR + OK);

				//////////////////////////////////////////////////////////////////////////
				// Recibe los algoritmos del cliente y los verifica
				//////////////////////////////////////////////////////////////////////////
				linea = reader.readLine();
				if (!(linea.contains(SEPARADOR) && linea.split(SEPARADOR)[0].equals(ALGORITMOS))) {
					writer.println(ERROR_FORMATO);
					throw new FontFormatException(linea);
				}

				// Verificar los algoritmos enviados
				String[] algoritmos = linea.split(SEPARADOR);
				// Comprueba y genera la llave simetrica para comunicarse con el servidor.
				if (!algoritmos[1].equals(DES) 
						&& !algoritmos[1].equals(AES)
						&& !algoritmos[1].equals(BLOWFISH) 
						&& !algoritmos[1].equals(RC4)){
					writer.println("Algoritmo no soportado o no reconocido: " 
						+ algoritmos[1] + ". Cerrando conexion");
					throw new NoSuchAlgorithmException();
				}
					
				// Comprueba que el algoritmo asimetrico sea RSA.
				if (!algoritmos[2].equals(RSA)) {
					writer.println("Algoritmo no soportado o no reconocido: " 
							+ algoritmos[2] + ". Cerrando conexion");
					throw new NoSuchAlgorithmException();
				}
				// Comprueba que el algoritmo HMAC sea valido.
				if (!(algoritmos[3].equals(HMACMD5) 
						|| algoritmos[3].equals(HMACSHA1) 
						|| algoritmos[3].equals(HMACSHA256))) {
					writer.println("Algoritmo no soportado o no reconocido: " 
							+ algoritmos[3] + ". Cerrando conexion");
					throw new NoSuchAlgorithmException();
				}

				// Confirmando al cliente que los algoritmos son soportados.
				writer.println(STATUS + SEPARADOR + OK);

				//////////////////////////////////////////////////////////////////////////
				// Enviando el certificado del servidor.
				//////////////////////////////////////////////////////////////////////////
				linea = reader.readLine();
				if (!linea.equals(CERTSRV)) {
					writer.println(ERROR_FORMATO);
					throw new FontFormatException(linea);
				}
				KeyPair keyPair = Seguridad.generateRSAKeyPair();
				java.security.cert.X509Certificate certSer = Seguridad.generateV3Certificate(keyPair);
				byte[] mybyte = certSer.getEncoded( );
				s.getOutputStream( ).write( mybyte );
				s.getOutputStream( ).flush( );

				//////////////////////////////////////////////////////////////////////////
				// Recibe la llave simetrica del cliente.
				//////////////////////////////////////////////////////////////////////////
				linea = reader.readLine();
				if (!(linea.contains(SEPARADOR) && linea.split(SEPARADOR)[0].equals(AUT))) {
					writer.println(ERROR_FORMATO);
					throw new FontFormatException(linea);
				}
				byte[] simetrica = Seguridad.asymmetricDecryption(
						Transformacion.destransformar(linea.split(SEPARADOR)[1]), 
						keyPair.getPrivate(), RSA);

				SecretKey llaveSimetrica = new SecretKeySpec(simetrica, 0, simetrica.length, algoritmos[1]);
				
				writer.println(STATUS + SEPARADOR + OK);

				//////////////////////////////////////////////////////////////////////////
				// Recibe el login y el password del cliente.
				// Usuario: usuarioic
				// Password: ic201320
				//////////////////////////////////////////////////////////////////////////
				linea = reader.readLine();
				if (!(linea.contains(SEPARADOR) )) {
					writer.println(ERROR_FORMATO);
					throw new FontFormatException(linea);
				}
				String [] parametros = linea.split(SEPARADOR);
				if (parametros.length != 2) {
					writer.println(ERROR_FORMATO);
					throw new FontFormatException(linea);
				}
				String creden = new String(Seguridad.symmetricDecryption(
						Transformacion.destransformar(parametros[0]), llaveSimetrica, algoritmos[1]));
				String credenciales[] = creden.split(",");
				byte[] password = Transformacion.destransformar(parametros[1]);
				if (!credenciales[0].equals("usuarioic") || !credenciales[1].equals("ic201320") ||
						!Seguridad.verificarIntegridad(creden.getBytes(), llaveSimetrica, algoritmos[3], password)) {
					writer.println(STATUS + SEPARADOR + ERROR);
					throw new FontFormatException(linea);
				}
				// Confirmando al cliente que los parametros son correctos.
				writer.println(STATUS + SEPARADOR + OK);

				//////////////////////////////////////////////////////////////////////////
				// Recibe la peticion del usuario.
				//////////////////////////////////////////////////////////////////////////
				linea = reader.readLine();
				if (!(linea.contains(SEPARADOR) && linea.split(SEPARADOR)[0].equals(STATTUTELA))) {
					writer.println(ERROR_FORMATO);
					throw new FontFormatException(linea);
				}
				//Desencripta la peticion
				String cedula = new String(Seguridad.symmetricDecryption(
						Transformacion.destransformar( linea.split(SEPARADOR)[1]), 
						llaveSimetrica, algoritmos[1]));
				//Crea un estado ficticio mediante una funcion aleatoria y saca su digest
				String resultado = cedula + ": ";
				String[] listaEstados = {"RECIBIDA" , "EN COLA" , 
						"EN TRAMITE" , "EN APROBACION" , "RESPUESTA EMITIDA"};
				resultado += listaEstados[resultado.charAt(0)%listaEstados.length];
				//Envia el resultado en la cadena especificada.
				writer.println( INFO + SEPARADOR + 
						Transformacion.transformar(
								Seguridad.symmetricEncryption(
										resultado.getBytes(COD), llaveSimetrica, algoritmos[1]))+
						SEPARADOR +
						Transformacion.transformar(
								Seguridad.hmacDigest(
										resultado.getBytes(COD), llaveSimetrica, algoritmos[3])));

				//////////////////////////////////////////////////////////////////////////
				// Recibe el resultado de la transaccion y termina la conexion.
				//////////////////////////////////////////////////////////////////////////
				linea = reader.readLine();
				if (!(linea.contains(SEPARADOR) && linea.split(SEPARADOR)[1].equals(OK))) {
					throw new Exception();
				}

				System.out.println("Termino requerimientos del cliente en perfectas condiciones.");

			} catch (InterruptedException e) {
				// Si hubo algun error tomando turno en el semaforo.
				// No deberia alcanzarse en condiciones normales de ejecucion.
				e.printStackTrace();
			}catch (NullPointerException e) {
				// Probablemente la conexion fue interrumpida.
				e.printStackTrace();
			} catch (IOException e) {
				// Error en la conexion con el cliente.
				e.printStackTrace();
			} catch (FontFormatException e) {
				// Si hubo errores en el protocolo por parte del cliente.
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// Si los algoritmos enviados no son soportados por el servidor.
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				// Error en adicionar el proveedor de seguridad.
				// No deberia alcanzarce en condiciones normales de ejecución.
				e.printStackTrace();
			} catch (CertificateEncodingException e) {
				// El certificado no se pudo serializar.
				// No deberia alcanzarce en condiciones normales de ejecución.
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// El certificado no se pudo generar.
				// No deberia alcanzarce en condiciones normales de ejecución.
				e.printStackTrace();
			} catch (SignatureException e) {
				// El certificado no se pudo generar.
				// No deberia alcanzarce en condiciones normales de ejecución.
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// El certificado no se pudo generar.
				// No deberia alcanzarce en condiciones normales de ejecución.
				e.printStackTrace();
			} catch (CertificateException e) {
				// El certificado no se pudo generar.
				// No deberia alcanzarce en condiciones normales de ejecución.
				e.printStackTrace();
			} catch (CertificateNotYetValidException e) {
				// El certificado del cliente no se pudo recuperar.
				// El cliente deberia revisar la creacion y envio de su certificado.
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// Error en el proceso de encripcion de datos del servidor.
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// No se pudo generar un sobre digital sobre la llave simetrica.
				// No deberia alcanzarce en condiciones normales de ejecución.
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// No se pudo generar un sobre digital sobre la llave simetrica.
				// No deberia alcanzarce en condiciones normales de ejecución.
				e.printStackTrace();
			} catch (Exception e) {
				// El cliente reporto que la informacion fue infructuosa.
				e.printStackTrace();
			} finally {
				try {
					s.close();
				} catch (Exception e) {
					// DO NOTHING
				}
			}
		}
	}
}

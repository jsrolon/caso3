package cliente;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import servidor.ThreadServidor;
import utils.Seguridad;
import utils.Transformacion;

/**
 * Esta clase implementa al cliente del servidor que se desarrollo anteriormente.
 * Infraestructura Computacional 201320
 * Universidad de los Andes.
 * Las tildes han sido eliminadas por cuestiones de compatibilidad.
 * @author Michael Andres Carrillo Pinzon
 */
public class Cliente 
{
	/**
	 * Algoritmo asimetrico.
	 */
	private final static String ASIMETRICO = "RSA";

	/**
	 * Algoritmo de generacion codigos HMAC.
	 */
	private final static String DIGEST = "HMACMD5";
	//private final static String DIGEST = "HMACSHA1";
	//private final static String DIGEST = "HMACSHA256";

	/**
	 * Algoritmo simetrico.
	 */
	private final static String SIMETRICO = "AES";
	//private final static String SIMETRICO = "DES";
	//private final static String SIMETRICO = "RC4";
	//private final static String SIMETRICO = "Blowfish";

	/**
	 * Puerto al cual se va a comunicar.
	 */
	public static final int PUERTO = 5555;

	/**
	 * URL de la maquina a la que se va a conectar.
	 */
	public static final String SERVIDOR = "localhost";

	/**
	 * Pide parametros de consola e imprime sobre la consola.
	 */
	public static final boolean enPrueba = true;

	/**
	 * Metodo main de la clase Cliente.
	 * @param args Los argumentos del cliente.
	 * @throws InterruptedException Si hubo problemas.
	 */
	public static void main(String args[]) throws InterruptedException
	{
		// Adiciona la libreria como un proveedor de seguridad.
		// Necesario para crear llaves.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());		

		cliente();
	}

	/**
	 * Metodo que instancia un cliente.
	 */
	public static void cliente() {
		Socket comunicacion;
		SecretKey simetrica;
		try
		{		
			//SE CREA EL SOCKET 
			comunicacion = new Socket( SERVIDOR, PUERTO);

			//SE CREA EL ESCRITOR PARA MANDAR MENSAJES AL SERVIDOR
			PrintWriter escritor = new PrintWriter( comunicacion.getOutputStream(), true);

			//SE CREA UN LECTOR DE CONSOLA
			BufferedReader consola = new BufferedReader(new InputStreamReader(System.in));

			//SE CREA EL LECTOR DE MENSAJES DEL SERVIDOR
			BufferedReader lector = new BufferedReader(new InputStreamReader(comunicacion.getInputStream()));

			//INPUT STREAM PARA RECIBIR FLUJO DE BYTES DEL SERVIDOR
			InputStream input = comunicacion.getInputStream();

			//OUTPUT STREAM PARA MANDAR FLUJO DE BYTES DEL SERVIDOR
			OutputStream output = comunicacion.getOutputStream();


			//SE ENVIA LA IDENTIFICACION CLIENTE
			escritor.println("INIT");

			//SE RECIBE EL STATUS DE LA ACCION ANTERIOR
			String status = lector.readLine();

			//SE ENVIA LOS ALGORITMOS A USAR
			escritor.println("ALGORITMOS:"+ SIMETRICO + ":" + ASIMETRICO +":" + DIGEST);

			//SE RECIBE EL STATUS DE LA ACCION ANTERIOR
			status = lector.readLine();

			//SE RECIBE EL CERTIFICADO
			escritor.println("CERTSRV");

			byte[] certificadoServidorBytes = new byte[520];
			int numBytesLeidos = input.read(certificadoServidorBytes);
			CertificateFactory creador = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(certificadoServidorBytes);
			X509Certificate certificadoServidor = (X509Certificate)creador.generateCertificate(in);

			//////////////////////////////////////////////////////////////////////////
			// Envia la llave simetrica al cliente en un sobre digital.
			// Encripta la llave con la clave publica del cliente.
			//////////////////////////////////////////////////////////////////////////
			escritor.println("AUT");
			status = lector.readLine();

			String credentials = "usuarioic,ic201320";
			escritor.println(credentials);
			status = lector.readLine();



			//SE ESPERA A QUE EL USUARIO INSERTE EL ID
			String idP = "45856951";
			if (enPrueba) {
				System.out.println("Ingrese la tutela a confirmar:");
				idP = consola.readLine();
			}

			//SE CIFRA EL INPUT DEL USUARIO
			String idPCifrado = idP;

			//SE ENVIA LA SOLICITUD
			escritor.println("STATTUTELA:"+idPCifrado);


			//SE RECIBE LA RESPUESTA DEL SERVIDOR Y SE DECIFRA
			String consultaCifrada = lector.readLine();

			consultaCifrada = consultaCifrada.substring(5);

			String respuesta = consultaCifrada;

			//SE REVISA EL MENSAJE CON EL DIGEST
			//boolean verificacion = verificarIntegridad(generarHMAC(respuesta,simetrica) ,digest );

			if (enPrueba) System.out.println("RESPUESTA RECIBIDA: "+ respuesta);
			escritor.println("RESULTADO:OK:FIN");

			comunicacion.close();
		}
		catch (Exception e)
		{
			System.out.println("Error cliente");
			e.printStackTrace();
		} 
	}
}

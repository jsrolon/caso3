package cliente;

import java.io.FileInputStream;
import java.security.Security;
import java.util.Properties;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class Generator {

	private static final String PROPERTIES = "./InfracompSeguridad/cliente.properties";
	
	private LoadGenerator generator;
	
	public Generator()
	{
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(PROPERTIES));
		
			Task work=createTask();
			int numberTask=Integer.parseInt(p.getProperty("number"));
			int gapBetweenTasks=Integer.parseInt(p.getProperty("gap"));
			generator = new LoadGenerator("Client-Server Load Test", numberTask, work, gapBetweenTasks);
			generator.generate();
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Task createTask()
	{
		return new TaskCliente();
	}
	
	public static void main(String[] args)
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());	
		@SuppressWarnings("unused")
		Generator gen=new Generator();
	}
}

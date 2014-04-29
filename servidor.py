import fileinput
import re
import subprocess
import time

tipo = './InfracompSeguridad/'
properties = 'servidor.properties'
jar = 'servidor.jar'

# Iterar sobre seguridad y no seguridad
for j in range(0, 1):

	# Iterar sobre el número de threads
	for i in range(0, 1):
	
		# Reemplazar la cantidad de threads para iterar
		for line in fileinput.input(tipo + properties, inplace=True):
			print(re.sub('[0-9]+$', '{}'.format(2 ** i), line), end='')
		
		# Inicializar el servidor
		print("SCRIPT: Iniciando el servidor...")
		serverProcess = subprocess.Popen(['java', '-jar', tipo + jar])

		# Tiempo necesario para que inicie el servidor
		time.sleep(0.4)

		# Llamar al proceso remoto que inicia GLoad y luego copiar sus resultados
		# debe ser sincrono
		# subprocess.call(['ssh', 'rlbstr@192.168.1.146', 'python3 caso3/cliente.py'])
			
		# Apagar servidor y agrupar resultados de la prueba
		serverProcess.terminate()
		print("SCRIPT: Servidor apagado!")
		time.sleep(1) # simulando tiempo de procesamiento
		
	tipo = './InfracompNoSeguridad/'

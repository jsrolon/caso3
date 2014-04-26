import fileinput
import re
import subprocess
import time

tipo = './InfracompSeguridad/'
properties = 'servidor.properties'
jar = 'servidor.jar'

# Iterar sobre seguridad y no seguridad
for j in range(0, 2):

	# Iterar sobre el número de threads
	for i in range(0, 5):
	
		# Reemplazar la cantidad de threads para iterar
		for line in fileinput.input(tipo + properties, inplace=True):
			print(re.sub('[0-9]+$', '{}'.format(2 ** i), line), end='')
		
		# Ciencia, acá es lo chévere
		for k in range(0, 1):
			# Inicializar el servidor
			print("Iniciando el servidor")
			serverProcess = subprocess.Popen(['java', '-jar', tipo + jar])

			# Tiempo necesario para que inicie el servidor
			time.sleep(0.4)

			# Llamar al proceso remoto que inicia GLoad y recibir sus resultados
			# debe ser síncrono
			# TODO
			
			# Apagar servidor y agrupar resultados de la prueba
			serverProcess.terminate()
			print("servidor apagado")
			time.sleep(1) # simulando tiempo de procesamiento
		
	tipo = './InfracompNoSeguridad/'
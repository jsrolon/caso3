import fileinput
import subprocess
import re
import time

tipo = 'InfracompSeguridad/'
properties = 'cliente.properties'
propertiesServidor = 'servidor.properties'
jarCliente = 'cliente.jar'
jarServidor = 'servidor.jar'

for i in range(0, 5):

	numthreads = 2 ** i
	serverProcess = subprocess.Popen(['ssh', 'rlbstr@192.168.1.146', 'java -jar caso3/' + tipo + jarServidor + ' {}'.format(numthreads)])

	time.sleep(1)

	for line in fileinput.input(tipo + propertiesServidor, inplace=True):
		print(re.sub('[0-9]+$', '{}'.format(numthreads), line), end='')

	# casos
	for repeticiones in (0,10):
		for line in fileinput.input(tipo + properties, inplace=True):
			if 'gap=' in line:
				print(re.sub('(gap=)[0-9]+$', 'gap={}'.format(20), line), end='')
			elif 'number' in line:
				print(re.sub('(number=)[0-9]+$', 'number={}'.format(400), line), end='')
			else:
				print(re.sub('(times=)[0-9]+$', 'times={}'.format(repeticiones), line), end='')
		# Ejecutar JAR
		subprocess.call(['java', '-jar', tipo + jarCliente])

		for line in fileinput.input(tipo + properties, inplace=True):
			if 'gap=' in line:
				print(re.sub('(gap=)[0-9]+$', 'gap={}'.format(40), line), end='')
			elif 'number' in line:
				print(re.sub('(number=)[0-9]+$', 'number={}'.format(200), line), end='')
			else:
				print(re.sub('(times=)[0-9]+$', 'times={}'.format(repeticiones), line), end='')
		# Ejecutar JAR
		subprocess.call(['java', '-jar', tipo + jarCliente])

		for line in fileinput.input(tipo + properties, inplace=True):
			if 'gap=' in line:
				print(re.sub('(gap=)[0-9]+$', 'gap={}'.format(80), line), end='')
			elif 'number' in line:
				print(re.sub('(number=)[0-9]+$', 'number={}'.format(100), line), end='')
			else:
				print(re.sub('(times=)[0-9]+$', 'times={}'.format(repeticiones), line), end='')
		# Ejecutar JAR
		subprocess.call(['java', '-jar', tipo + jarCliente])

	serverProcess.terminate()
		
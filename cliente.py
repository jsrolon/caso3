import fileinput
import subprocess
import re

tipo = './InfracompNoSeguridad/'
properties = 'cliente.properties'
jar = 'cliente.jar'

for repeticiones in (0,6):
	for line in fileinput.input(tipo + properties, inplace=True):
		print(re.sub('(gap=)[0-9]+$', '\1{}'.format(20), line), end='')
		print(re.sub('(number=)[0-9]+$', '\1{}'.format(400), line), end='')
		print(re.sub('(times=)[0-9]+$', '\1{}'.format(repeticiones), line), end='')
	# Ejecutar JAR
	subprocess.call(['java', '-jar', tipo + jar])

	for line in fileinput.input(tipo + properties, inplace=True):
		print(re.sub('(gap=)[0-9]+$', '\1{}'.format(40), line), end='')
		print(re.sub('(number=)[0-9]+$', '\1{}'.format(200), line), end='')
		print(re.sub('(times=)[0-9]+$', '\1{}'.format(repeticiones), line), end='')
	# Ejecutar JAR
	subprocess.call(['java', '-jar', tipo + jar])

	for line in fileinput.input(tipo + properties, inplace=True):
		print(re.sub('(gap=)[0-9]+$', '\1{}'.format(80), line), end='')
		print(re.sub('(number=)[0-9]+$', '\1{}'.format(100), line), end='')
		print(re.sub('(times=)[0-9]+$', '\1{}'.format(repeticiones), line), end='')
	# Ejecutar JAR
	subprocess.call(['java', '-jar', tipo + jar])
	
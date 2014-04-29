import fileinput
import subprocess
import re

tipo = './InfracompNoSeguridad/'
properties = 'cliente.properties'
jar = 'cliente.jar'

for repeticiones in (0,6):
	for line in fileinput.input(tipo + properties, inplace=True):
		if 'gap=' in line:
			print(re.sub('(gap=)[0-9]+$', 'gap={}'.format(20), line), end='')
		elif 'number' in line:
			print(re.sub('(number=)[0-9]+$', 'number={}'.format(400), line), end='')
		else:
			print(re.sub('(times=)[0-9]+$', 'times={}'.format(repeticiones), line), end='')
	# Ejecutar JAR
	subprocess.call(['java', '-jar', tipo + jar])

	for line in fileinput.input(tipo + properties, inplace=True):
		if 'gap=' in line:
			print(re.sub('(gap=)[0-9]+$', 'gap={}'.format(40), line), end='')
		elif 'number' in line:
			print(re.sub('(number=)[0-9]+$', 'number={}'.format(200), line), end='')
		else:
			print(re.sub('(times=)[0-9]+$', 'times={}'.format(repeticiones), line), end='')
	# Ejecutar JAR
	subprocess.call(['java', '-jar', tipo + jar])

	for line in fileinput.input(tipo + properties, inplace=True):
		if 'gap=' in line:
			print(re.sub('(gap=)[0-9]+$', 'gap={}'.format(80), line), end='')
		elif 'number' in line:
			print(re.sub('(number=)[0-9]+$', 'number={}'.format(100), line), end='')
		else:
			print(re.sub('(times=)[0-9]+$', 'times={}'.format(repeticiones), line), end='')
	# Ejecutar JAR
	subprocess.call(['java', '-jar', tipo + jar])
	
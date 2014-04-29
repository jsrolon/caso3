import fileinput
import subprocess

tipo = './InfracompNoSeguridad/'
properties = 'cliente.properties'
jar = 'cliente.jar'

for repeticiones in (0,6):
	for line in fileinput.input(tipo + properties, inplace=True):
		if line.find('gap=')!=-1:
			print('gap=',20000,end='')
		elif line.find('number=')!=-1:
			print('number=',400, end='')
		else:
			print('times=',repeticiones, end='')
	# Ejecutar JAR
	subprocess.call(['java', '-jar', tipo + jar])

	for line in fileinput.input(tipo + properties, inplace=True):
		if line.find('gap=')!=-1:
			print('gap=',40000,end='')
		elif line.find('number=')!=-1:
			print('number=',200, end='')
		else:
			print('times=',repeticiones, end='')
	# Ejecutar JAR
	subprocess.call(['java', '-jar', tipo + jar])

	for line in fileinput.input(tipo + properties, inplace=True):
		if line.find('gap=')!=-1:
			print('gap=',80000,end='')
		elif line.find('number=')!=-1:
			print('number=',100, end='')
		else:
			print('times=',repeticiones, end='')
	# Ejecutar JAR
	subprocess.call(['java', '-jar', tipo + jar])
	
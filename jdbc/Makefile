# Ensimag 2A BDD - Projet 2021/22
# ============================
#
# Ce Makefile permet de compiler le démonstrateur de la base de données
#
# Compilation:

all: main

main:
	javac -encoding cp1252 -d bin -classpath lib/ojdbc6.jar -sourcepath src src/Main.java

# Execution:

exec:
	java -classpath lib/ojdbc6.jar:bin Main


clean:
	rm -rf bin/*


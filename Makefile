
compile: compile_servidor compile_cliente

compile_servidor: clean_servidor
	cd src/servidor && javac MainServidorLeiloes.java

compile_cliente: clean_cliente
	cd src/cliente && javac MainClienteLeilao.java

cliente:
	cd src/cliente && java MainClienteLeilao

servidor:
	cd src/servidor && java MainServidorLeiloes


clean: clean_cliente clean_servidor

clean_cliente:
	rm -f src/cliente/*.class

clean_servidor:
	rm -f src/servidor/*.class

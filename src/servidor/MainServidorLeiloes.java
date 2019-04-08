
import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class MainServidorLeiloes {

    public static void main(String args[]) throws IOException {
        ServerSocket ss = new ServerSocket(9999);
        Contas contas = new Contas();
        GestorLeilao gestorLeilao = new GestorLeilao();
        Socket cs;
        ServidorLeComandos slc;
        ServidorEscreveComandos sec;
        Thread threadLeComandos, threadEscreveComandos;

        System.out.println("Servidor a aceitar conexões na porta 9999...");

        while((cs=ss.accept()) != null) {
            System.out.println("Novo cliente ligado.");

            // queue partilhada por ambas as threads de um dado cliente
            BlockingQueue<String> q = new  ArrayBlockingQueue<String>(100);

            // thread que escreve para o cliente
            sec = new ServidorEscreveComandos(cs, q);
            threadEscreveComandos = new Thread(sec);

            // thread que lê comandos do cliente
            slc = new ServidorLeComandos(cs, contas, gestorLeilao, q, threadEscreveComandos, 10);
            threadLeComandos = new Thread(slc);

            // começar ambas
            threadLeComandos.start();
            threadEscreveComandos.start();
        }
        ss.close();
    }
}
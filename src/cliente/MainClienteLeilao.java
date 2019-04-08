import java.io.*;
import java.net.*;

public class MainClienteLeilao {

    public static void main(String args[]) throws IOException {
        ClienteEscreveSocket cli = new ClienteEscreveSocket("localhost", 9999);

        boolean sair = cli.menuInicial();
        if(!sair) {
            cli.menuLeiloes();
        }
        cli.fecharSocket();
        
    }


}

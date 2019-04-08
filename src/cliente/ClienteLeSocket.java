import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;

class ClienteLeSocket implements Runnable {

    private BufferedReader br;
    private BlockingQueue<String> q;
    public String username = "";

    public ClienteLeSocket(BufferedReader br, BlockingQueue<String> q) {
        this.br = br;
        this.q = q;
    }

    public void run() {
        String [] parts;
        String s, item, valor, vencedor;

        try {
            while((s = br.readLine()) != null) {
                parts = s.split(" ");
                if(parts[0].equals("Terminou")) {
                    item = parts[1];
                    valor = parts[2];
                    vencedor = parts[3];
                    if(vencedor.equals(username)) {
                        System.out.println("\nGanhei o leilão do '" + item + "' por " + valor + " €.\n");
                    }
                    else{
                        System.out.println("\nO '" + vencedor + "' ganhou o leilão do '" + item + "' por " + valor + " €.\n");
                    }
                }
                else if(parts[0].equals("Ultrapassada")) {
                    item = parts[1];
                    valor = parts[2];
                    vencedor = parts[3];
                    System.out.println("\nO '" + vencedor + "' fez uma licitação de maior valor ("+valor+" €) do leilão '" + item + "'.\n");
                }
                else {
                    q.put(s);
                }

            }
        }
        catch (IOException e ) {
            System.out.println(e.getMessage());
        }
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}


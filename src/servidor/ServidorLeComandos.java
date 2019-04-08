import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class ServidorLeComandos implements Runnable {
    private Socket cs;
    private Contas contas;
    private GestorLeilao gestorLeilao;
    private String username;
    private BlockingQueue<String> q;
    private long timeoutQueue;
    private Thread tEscreve;

    public ServidorLeComandos (Socket cs, Contas contas, GestorLeilao gestorLeilao, BlockingQueue<String> q, Thread tEscreve, long timeoutQueue) {
        this.cs = cs;
        this.contas = contas;
        this.gestorLeilao = gestorLeilao;
        this.username = null;
        this.q = q;
        this.timeoutQueue = timeoutQueue;
        this.tEscreve = tEscreve;
    }

    public void run () {
        String s;
        String [] parts;
        Leilao leilao;
        Integer numLeilao;
        float valor;
        String vencedor;
        List<String> leiloes;

        try {
            InputStream is = cs.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
            
            boolean resgistado = false;
            boolean login = false;
            int actualiza;
            boolean acesso, adiciona, queueComEspaco = true;

            while((s=br.readLine()) != null && queueComEspaco) {
                s = s.trim();// reduz todos os espaços e tabs a um espaço

                parts = s.split(" ");

                if(username == null) {
                    switch(parts[0]) {

                        case ("Registar"):
                            adiciona = contas.adicionaConta(parts[1], parts[2]);
                            if(adiciona) {
                                contas.validaAcessoConta(parts[1], parts[2]);
                                this.username = parts[1]; // verrrrr
                                queueComEspaco = q.offer("ok", timeoutQueue, TimeUnit.SECONDS);
                            }
                            else {
                                queueComEspaco = q.offer("error", timeoutQueue, TimeUnit.SECONDS);
                            }
                            break;

                        case ("Login") :
                            acesso = contas.validaAcessoConta(parts[1], parts[2]);
                            if(acesso) {
                                this.username = parts[1];
                                queueComEspaco = q.offer("ok", timeoutQueue, TimeUnit.SECONDS);
                            }
                            else {
                                queueComEspaco = q.offer("error", timeoutQueue, TimeUnit.SECONDS);
                            }
                            
                            break;

                        default:
                            queueComEspaco = q.offer("error", timeoutQueue, TimeUnit.SECONDS);
                            break;
                    }
                }

                // o user já fez login
                else {

                    switch(parts[0]) {
                        case ("Iniciar"):

                            leilao = new Leilao(parts[1], Float.parseFloat(parts[2]), username);
                            numLeilao = gestorLeilao.adicionaLeilao(leilao);
                            // pw.println("Leilão iniciado com sucesso!");
                            queueComEspaco = q.offer(Integer.toString(numLeilao), timeoutQueue, TimeUnit.SECONDS);
                            break;

                        case ("Listar"):
                            leiloes = gestorLeilao.getLeiloesActivos(username);
                            queueComEspaco = q.offer(Integer.toString(leiloes.size()), timeoutQueue, TimeUnit.SECONDS); // número de leiloes
                            for (String l: leiloes) 
                                queueComEspaco = q.offer(l, timeoutQueue, TimeUnit.SECONDS);
                             break;

                        case ("Licitar") :
                            numLeilao = Integer.parseInt(parts[1]);                           
                            valor = Float.parseFloat(parts[2]);

                            actualiza = gestorLeilao.actualizaLicitacao(numLeilao,username, valor, q);

                            if(actualiza == 1) {
                                queueComEspaco = q.offer("ok", timeoutQueue, TimeUnit.SECONDS); 
                            }
                            else if(actualiza == 0) {
                                queueComEspaco = q.offer("error2", timeoutQueue, TimeUnit.SECONDS); // valor inferior ou igual à licitação actual;
                            }
                            else if(actualiza == -1) {
                                queueComEspaco = q.offer("error3", timeoutQueue, TimeUnit.SECONDS); // leilão terminado
                            }
                            else if(actualiza == -2){
                                queueComEspaco = q.offer("error1", timeoutQueue, TimeUnit.SECONDS);  // leilao inexistente
                            }
                            break;

                        case ("Finalizar"):
                            numLeilao = Integer.parseInt(parts[1]);
                            leilao = gestorLeilao.getLeilaoAtivo(numLeilao);
                            if(leilao!=null) {
                                if(leilao.getVendedor().equals(username)) {
                                    gestorLeilao.terminaLeilao(numLeilao);
                                    if(leilao.getLicitadorActual() == null) {
                                        queueComEspaco = q.offer("error3");
                                    } else {
                                        queueComEspaco = q.offer(leilao.getLicitadorActual(), timeoutQueue, TimeUnit.SECONDS );
                                        queueComEspaco = q.offer(Float.toString(leilao.getValor()), timeoutQueue, TimeUnit.SECONDS);
                                    }
                                }
                                else {
                                    queueComEspaco = q.offer("error2", timeoutQueue, TimeUnit.SECONDS); // Não tem permissões para encerrar Leilão!
                                }
                            }
                            else {
                                queueComEspaco = q.offer("error1", timeoutQueue, TimeUnit.SECONDS); // leilao inexistente
                            }
                            break;
                    }
                }
            }

            if(queueComEspaco == false) {
                tEscreve.interrupt();
            }

            br.close();
            isr.close();
            is.close();

            cs.close();

        }
        

        catch (IOException e ) {
            System.out.println(e.getMessage());
        }
        catch(InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

}

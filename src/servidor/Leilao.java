
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;


public class Leilao {
    private String item;
    private float valor;
    private String vendedor;
    private String licitadorActual;
    private Map<String, BlockingQueue<String>> licitadores;
    private boolean terminado;
    private final ReentrantLock lock = new ReentrantLock();


    public Leilao(String item, float valor, String vendedor) {
        this.item = item;
        this.valor = valor;
        this.vendedor = vendedor;
        licitadorActual = null;
        licitadores = new HashMap<String,BlockingQueue<String>>();
        terminado = false;

    }


    public String descricaoItem() {
        return item;
    }

    public float getValor() {
        return valor;
    }

    public String getVendedor() {
        return vendedor;
    }


    public String getLicitadorActual() {
        return licitadorActual;
    }

    public Map<String, BlockingQueue<String>> getLicitadores() {
        return licitadores;
    }

    public boolean getTerminado() {
        return terminado;
    }

    private ReentrantLock getLock(){
        return lock;
    }

    public void terminar() {
        terminado = true;
        for(BlockingQueue<String> q:licitadores.values()) {
            q.offer(this.toStringTerminaLeilao());
        }

    }
    
    
    public boolean eLicitadorActual(String username) {
        return licitadorActual != null && licitadorActual.equals(username);
    }


    public int actualizaLicitacao(String licitadorNovo, float valor, BlockingQueue<String> q){
        int actualiza;
        if(terminado == false && valor > this.valor) {
            this.valor = valor; 
            if(licitadorActual != null && !licitadorNovo.equals(licitadorActual)) {
                BlockingQueue<String> queue = licitadores.get(licitadorActual);
                queue.offer(this.toStringLicitacaoUltrapassada(licitadorNovo));
            }
            licitadorActual = licitadorNovo;
            licitadores.put(licitadorNovo, q);
            actualiza = 1;
        }
        else {
            if(terminado == true) {
                actualiza = -1;
            }
        
            else {
                actualiza = 0;
            }
        }
        return actualiza;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }


    public String toStringTerminaLeilao(){

        StringBuilder sb = new StringBuilder();
        sb.append("Terminou ");
        sb.append(item);
        sb.append(" ");
        sb.append(valor);
        sb.append(" ");
        sb.append(licitadorActual);
        
        return sb.toString();
    }

    public String toStringLicitacaoUltrapassada(String licitadorNovo) {

        StringBuilder sb = new StringBuilder();
        sb.append("Ultrapassada ");
        sb.append(item);
        sb.append(" ");
        sb.append(valor);
        sb.append(" ");
        sb.append(licitadorNovo);
        
        return sb.toString();
    }

    public String toStringListar(){
        StringBuilder sb = new StringBuilder();
        sb.append("\t\t");
        sb.append(item);
        sb.append("\t\t");
        sb.append(valor);
        return sb.toString();
    }
}

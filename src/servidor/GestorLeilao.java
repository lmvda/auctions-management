
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class GestorLeilao {
    private Integer contador;
    private Map<Integer, Leilao> leiloesActivos;
    private Map<Integer,Leilao> leiloesTerminados;
    private RWLock rwlock;


    public GestorLeilao() {
        contador = 0;
        leiloesActivos = new TreeMap<Integer, Leilao>();
        leiloesTerminados = new TreeMap<Integer,Leilao>();
        rwlock = new RWLock();
    }



    public List<String> getLeiloesActivos(String username) throws InterruptedException {
        
        try{
            rwlock.readLock();

            Leilao leilao;
            List<String> result = new ArrayList<>();

            for (Integer id: leiloesActivos.keySet()) {
                leilao = leiloesActivos.get(id);
                
                try{
                    leilao.lock();

                    if(leilao.getVendedor().equals(username)) {
                        result.add(" Meu       " + id + leilao.toStringListar());
                    }
                    else if(leilao.eLicitadorActual(username)) {
                        result.add(" A ganhar  " + Integer.toString(id) + leilao.toStringListar());
                    }
                    else if(leilao.getLicitadores().containsKey(username)){
                        result.add(" A perder  " + Integer.toString(id) + leilao.toStringListar());
                    }
                    else {
                        result.add("           " + Integer.toString(id) + leilao.toStringListar());

                    }
                } finally {
                    leilao.unlock();
                }
            }            
            return result;
        } finally {
            rwlock.readUnlock();
        }
    }


    public Map<Integer,Leilao> getLeiloesTerminados() throws InterruptedException {
        
        try {
            rwlock.readLock();
            return leiloesTerminados;
        } finally {
            rwlock.readUnlock();
        }
    }

    public Leilao getLeilaoAtivo(int numLeilao) throws InterruptedException
    {
        try {
            rwlock.readLock();
            return leiloesActivos.get(numLeilao);
        } finally {
            rwlock.readUnlock();
        }

    }

    

    public Integer adicionaLeilao(Leilao leilao) throws InterruptedException {

        try{
            rwlock.writeLock();
            contador ++;
            leiloesActivos.put(contador, leilao);
            return contador;
        } finally {
            rwlock.writeUnlock();
        }
    }

    public void terminaLeilao(Integer num) throws InterruptedException {

        try{
            rwlock.readLock();
            Leilao leilao = leiloesActivos.get(num);
            rwlock.readUnlock(); //Não tenho a certeza que isto esteja bem, mas pareceu-me mais eficiente porque assim ele consegue aceder ao leilão
            leilao.lock(); //que quer encerrar muito mais rapidamente
            leilao.terminar();
            leilao.unlock();
            rwlock.writeLock();
            leiloesTerminados.put(num,leilao);
            leiloesActivos.remove(num);
        } finally {
            rwlock.writeUnlock();
        }
    }

    public int actualizaLicitacao(int numLeilao, String username, float valor, BlockingQueue<String> q) throws InterruptedException{

        try{
            rwlock.readLock();
            if(!leiloesActivos.containsKey(numLeilao))
            {
                if(leiloesTerminados.containsKey(numLeilao)) {
                    return (-1); // o leilão já terminou
                }
                else {
                    return (-2); // leilão inexistente
                }
            }
            Leilao leilao = leiloesActivos.get(numLeilao);
            leilao.lock();
            int actualiza = leilao.actualizaLicitacao(username, valor, q);
            leilao.unlock();
            return actualiza;
        } finally {
            rwlock.readUnlock();
        }

    }


}


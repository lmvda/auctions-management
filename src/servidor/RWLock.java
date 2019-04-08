
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.ReentrantLock;


public class RWLock {

    private int readers;
    private int writers;
    private int waitWriters;

    private final Lock lock = new ReentrantLock();
    private Condition rd = lock.newCondition();
    private Condition wr = lock.newCondition(); 

    public RWLock() {
        readers = 0;
        writers = 0;
        waitWriters = 0;
    }


    public void readLock() throws InterruptedException{
        lock.lock();
            while (writers + waitWriters != 0) {
                rd.await(); //vamos pôr o leitor em espera
            }
            readers++;
        lock.unlock();
    }


    public void readUnlock() throws InterruptedException{
        lock.lock();
            readers--;
            if (readers == 0)
                wr.signalAll();
        lock.unlock();
    }


    public void writeLock() throws InterruptedException{
        lock.lock();
            waitWriters++;
            while ((writers + readers) != 0) {
                wr.await(); //vamos pôr o escritor em espera
            }
            writers++;
            waitWriters--;
        lock.unlock();
    }


    public void writeUnlock() throws InterruptedException {
        lock.lock();
            writers--;
            wr.signalAll(); // Acordo todos os escritores mas como só um é que consegue entrar até podíamos fazer um signal simples
            rd.signalAll(); // Acordo todos os leitores
        lock.unlock();
    }
}
import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;


class ServidorEscreveComandos implements Runnable {
    private Socket cs;
    private BlockingQueue<String> q;

    public ServidorEscreveComandos(Socket cs, BlockingQueue<String> q) {
        this.cs = cs;
        this.q = q;
    }

    public void run() {
        try{
            OutputStream os = cs.getOutputStream();
            PrintWriter pw = new PrintWriter(os, true);
            String s;

            while((s=(String)q.take()) != null) {
                pw.println(s);
            }

            pw.close();
            os.close();
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

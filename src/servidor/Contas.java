
import java.util.HashMap;
import java.util.Map;



public class Contas {
    private Map<String, String> contas;

    public Contas () {
        contas = new HashMap<String, String>();
    }

    public boolean validaAcessoConta (String username, String password) {
        boolean res = false;
        if(contas.containsKey(username) && contas.get(username).equals(password)) {
            res = true;
        }
        return res;
    }

    public synchronized boolean adicionaConta(String username, String password) {
        boolean adiciona;
        if(!contas.containsKey(username)) {
            contas.put(username, password);
            adiciona = true;
        }
        else {
            adiciona = false;
        }
        return adiciona;
    }

    public synchronized void removeConta(String username) {
        if(contas.containsKey(username)) {
            contas.remove(username);
        }
    }

}
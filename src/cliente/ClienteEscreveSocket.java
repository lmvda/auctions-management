import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class ClienteEscreveSocket {

    private Socket socket;

    private OutputStream os;
    private PrintWriter pw;

    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader br;
    private BlockingQueue<String> q; // Oferece metodo que bloqueia a espera que chegue a resposta do cliente, o método take
    private Thread threadLeitura;
    private ClienteLeSocket leituras;
    private Scanner keyboard;



    public ClienteEscreveSocket(String ip, int port) throws IOException {
        socket = new Socket("localhost", 9999);

        os = socket.getOutputStream();
        pw = new PrintWriter(os);

        is = socket.getInputStream();
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        q = new ArrayBlockingQueue<String>(100000);
        leituras = new ClienteLeSocket(br, q);
        threadLeitura = new Thread(leituras);
        threadLeitura.start();
    }



    public boolean menuInicial()  throws IOException {

        String menu = ("\n1. Registar\n2. Login\ns. Sair\n");
        keyboard = new Scanner(System.in);

        boolean logRegisto = false;
        String opcao = "s";
        String username;
        String password;


        System.out.println("Bem-Vindo ao Sistema Online de Leilões!");

        try {
            do {
                System.out.println(menu);
                opcao = keyboard.nextLine();

                switch(opcao) {
                    case "1":
                        System.out.print("Username: ");
                        username = keyboard.nextLine();
                        System.out.print("Password: ");
                        password = keyboard.nextLine();
                        pw.println("Registar " + username + " " + password);
                        pw.flush();

                        if(q.take().equals("ok")) {
                            apresentaResposta("Registo com sucesso.");
                            this.leituras.username = username;
                            logRegisto = true;
                        } else {
                            apresentaResposta("Registo inválido. Utilizador já existe.");
                        }
                        break;

                    case "2":
                        System.out.print("Username: ");
                        username = keyboard.nextLine();
                        System.out.print("Password: ");
                        password = keyboard.nextLine();
                        pw.println("Login " + username + " " + password);
                        pw.flush();

                        if((q.take()).equals("ok")) {
                            apresentaResposta("Login realizado com sucesso.");
                            this.leituras.username = username;
                            logRegisto = true;
                        } else {
                            apresentaResposta("Login inválido.");
                        }
                        break;

                    case "s":
                        apresentaResposta("Obrigado e volte sempre");
                        break;

                    default:
                        apresentaResposta("Opção inválida!");
                }
            }
            while(!opcao.equals("s") && logRegisto == false);
        }
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return opcao.equals("s");
    }


    public void menuLeiloes()  throws IOException  {

        String menu2 = ("\n1. Iniciar Leilão\n2. Listagem dos leilões do momento\n3. Licitar \n4. Finalizar um leilão\ns. Sair\n");
        String item;
        String preco;
        String nLeilao;
        String vencedor;
        String opcao;
        String resposta;

        Scanner keyboard = new Scanner(System.in);
        try {
            do {
                System.out.println(menu2);
                opcao = keyboard.nextLine();
                switch(opcao) {
                    case "1":
                        System.out.print("Descrição do Item: ");
                        item = keyboard.nextLine();
                        System.out.print("Preço: ");
                        preco = keyboard.nextLine();
                        pw.println("Iniciar " + item + " " + preco);
                        pw.flush();
                        apresentaResposta("O número do leilão é " + q.take());
                        break;

                    case "2":
                        pw.println("Listar");
                        pw.flush();
                        int numLeiloes = Integer.parseInt(q.take());
                        if(numLeiloes == 0) {
                            apresentaResposta("Não há leilões activos!");
                        }
                        else {

                            List<String>  respostas = new ArrayList<String>();
                            respostas.add("---------------------------------------------------------------------");
                            respostas.add(" Tipo\t   Nº Leilão \tDescrição \tLicitação Actual");
                            respostas.add("---------------------------------------------------------------------");
                            for(int i=0; i < numLeiloes; i++) {
                                respostas.add(q.take());
                            }
                            respostas.add("---------------------------------------------------------------------");
                            apresentaRespostas(respostas);                            
                        }
                        break;

                    case "3":
                        System.out.println("Número do leilão: ");
                        nLeilao = keyboard.nextLine();
                        System.out.print("Preço : ");
                        preco = keyboard.nextLine();
                        pw.println("Licitar " + nLeilao + " " + preco);
                        pw.flush();
                        resposta = q.take();
                        if(resposta.equals("ok")) {
                             apresentaResposta("Licitação efectuada com sucesso!");
                        }
                        else if (resposta.equals("error1")){
                            apresentaResposta("Leilão inexistente");
                        }
                        else if(resposta.equals("error2")) {
                            apresentaResposta("Valor da licitação inferior ou igual a licitação mais elevada até ao momento!");
                        }
                        else if(resposta.equals("error3")) {
                            apresentaResposta("Este leilão já terminou!");
                        }

                        break;

                    case "4":
                        System.out.print("Número do leilão: ");
                        nLeilao = keyboard.nextLine();
                        pw.println("Finalizar " + nLeilao);
                        pw.flush();
                        resposta = q.take();
                        if(resposta.equals("error1")) {
                            apresentaResposta("Leilão inexistente");
                        }
                        else if(resposta.equals("error2")) {
                            apresentaResposta("Não tem permissões para encerrar Leilão!");
                        }
                        else if(resposta.equals("error3")) {
                            apresentaResposta("Leilão encerrado!\nNão houve licitações.");
                        }
                        else {
                            vencedor = resposta;
                            preco = q.take();
                            List<String>  respostas = new ArrayList<String>();
                            respostas.add("Leilão Encerrado!");
                            respostas.add(("O vencedor foi o " + vencedor + " e pagou " + preco+ "€."));
                            apresentaRespostas(respostas);
                        }

                        break;

                    case "s":
                        apresentaResposta("Obrigado e volte sempre");
                        break;
                }

            }
            while(!opcao.equals("s"));
        }
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public void fecharSocket()  throws IOException  {
        pw.close();
        os.close();

        br.close();
        isr.close();
        is.close();

        socket.close();
    }

    private void apresentaResposta(String resposta) {
        System.out.print(String.format("\033[2J"));

        System.out.println(resposta);
        System.out.println("\n\nPrima qualquer tecla para continuar...");
        keyboard.nextLine();
        System.out.print(String.format("\033[2J"));
    }


    private void apresentaRespostas(List<String> respostas) {
        System.out.print(String.format("\033[2J"));

        for(String s: respostas) {
            System.out.println(s);
        }
        System.out.println("\n\nPrima qualquer tecla para continuar...");
        keyboard.nextLine();
        System.out.print(String.format("\033[2J"));
    }



}

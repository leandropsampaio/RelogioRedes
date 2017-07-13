package Controller;

import Conexao.Conexao;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Leandro Pereira Sampaio
 */
public class ThreadReceber implements Runnable {

    private TelaRelogioController relogio;
    private boolean liderBuffer;
    

    public ThreadReceber(TelaRelogioController relogio) {
        this.relogio = relogio;
    }

    @Override
    public void run() {
        Conexao conexao = Conexao.getInstancia();

        while (true) {
  
          try {
               String[] comandos = conexao.receber().split(";");
               System.out.println(comandos[0]);
               if(comandos.length>1){ //Não responde a mensagens de si mesmo
                   
               
                    //Recebendo msg de sincronização
                    if(comandos[0].equals("sincronizar2")){
                        relogio.atualizarTempo(Integer.parseInt(comandos[2]), Integer.parseInt(comandos[1]));
                    }

                    //Bullynando o lider
                    if(comandos[0].equals("bullying")){
                        System.out.println("Bully");
                        if(comandos[2].equals(conexao.getId())){
                            conexao.setMsgRecebida(true);
                            System.out.println("Eu sou o líder");
                            if(Integer.parseInt(comandos[3]) > relogio.getContador())
                                conexao.setLiderMenor(false);
                        }
                    }

                    //Alguem pediu eleição e enviou seu tempo para todos
                    if(comandos[0].equals("eleicao2")){
                        if(Integer.parseInt(comandos[2]) > conexao.getContMestre()){ //Vê se a hora é maior que a sua
                            conexao.setMestre(comandos[1]);
                        }
                        conexao.enviar("eleicaoFinal");
                    }
                    if(comandos[0].equals("eleicao1")){
                        conexao.setMestre(conexao.getId());
                        if(relogio.getContador() > conexao.getContMestre()){
                            conexao.setContMestre(relogio.getContador());
                        }
                        conexao.setEleicao(false);
                        if(Integer.parseInt(comandos[2]) > conexao.getContMestre()){ //Vê se a hora é maior que a sua
                            conexao.setMestre(comandos[1]);
                        }
                        conexao.enviar("eleicao2;" + conexao.getId() + ";" + relogio.getContador());
                    }
          } //tamanho do comando <= 1
                if(comandos[0].equals("eleicaoFinal")){
                    System.out.println("O líder é o:" + conexao.getMestre());
                    conexao.setEleicao(true);
                }
               //Enviar para o mestre pedido de sincronização
                if(comandos[0].equals("sincronizar1")){
                    if(conexao.getMestre().equals(conexao.getId())){
                        conexao.enviar("sincronizar2;" + relogio.getContador() + ";" + relogio.getHora());
                    }
                }
               
               
          } catch (IOException ex) {
                Logger.getLogger(ThreadReceber.class.getName()).log(Level.SEVERE, null, ex);
            }
               
               
//
//                if(comandos[0].equals("entrar")) {
//                    System.out.println("11111111111111111111111111111111111");
//                    String acao = comandos[1];
//                    String idRecebido = comandos[2];
//
//                    if (acao.equals("enviar")) {
//                        System.out.println("enviar");
//                        System.out.println("22222222222222222222222222222222222");
//                            
//                        if (!idRecebido.equals(conexao.getId())) {
//                            System.out.println("33333333333333333333333333333333");
//                            System.out.println(idRecebido);
//                            conexao.enviar("entrar;receber;" + idRecebido + ";" + relogio.getid());
//
//                        }
//                    } else if (acao.equals("receber")) {
//                        System.out.println("4444444444444444444444444444444444");
//                        System.out.println("receber");
//
//                        int id = Integer.parseInt(comandos[3]);
//                        System.out.println(id + "id");
//                        if (idRecebido.equals(conexao.getId())) {
//                            System.out.println(idRecebido);
//                            System.out.println("55555555555555555555555555555555555");
//                            if (id >= relogio.getid()) {
//                                relogio.setId(id + 1);
//                            }
//
//                            conexao.enviar("mestre;enviar;" + conexao.getId());
//                        }
//                    }
//
//                    System.out.println(relogio.getid() + "final");
//                }else if (comandos[0].equals("mestre")) {
//                    System.out.println("66666666666666666666666666666666666666");
//                    String acao = comandos[1];
//                    String nome = comandos[2];
//                    if (acao.equals("enviar")) {
//                        System.out.println("777777777777777777777777777777777777");
//                        System.out.println("enviar");
//                        if (!nome.equals(conexao.getId())) {
//                            System.out.println("888888888888888888888888888888888888");
//                            System.out.println(nome);
//                            conexao.enviar("mestre;receber;" + nome + ";" + relogio.getid() + ";" + conexao.getId());
//
//                        }
//                    } else if (acao.equals("receber")) {
//                        System.out.println("receber");
//                        System.out.println("9999999999999999999999999999999999999999");
//                        int id = Integer.parseInt(comandos[3]);
//                        System.out.println(id + "id");
//                        String mestre = comandos[4];
//                        if (nome.equals(conexao.getId())) {
//                            System.out.println(nome);
//                            System.out.println("************************************************");
//                            if (id < relogio.getid()) {
//                                conexao.setMestre(mestre);
//                            }
//
//                        }
//                    }
//
//                    System.out.println("Coordenador: " + conexao.getMestre());
//                } else if (comandos[0].equals("enviaTempo")) {
//                    System.out.println("---------------------------------------------------------");
//                    if (!comandos[1].equals(conexao.getId())) {
//                        this.relogio.atualizarTempo(Integer.parseInt(comandos[2]), Integer.parseInt(comandos[3]));
//                    }
        }
}
}

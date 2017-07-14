package Controller;

import Conexao.Conexao;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe ThreadReceber, thread responsável por receber as mensagens enviadas
 * pelos outros relógios.
 *
 * @author Leandro Pereira Sampaio
 */
public class ThreadReceber implements Runnable {

    private TelaRelogioController relogio;

    public ThreadReceber(TelaRelogioController relogio) {
        this.relogio = relogio;
    }

    @Override
    /**
     * *
     * Respostas aos protocolos recebidos
     */
    public void run() {
        Conexao conexao = Conexao.getInstancia();

        while (true) {

            try {
                String[] comandos = conexao.receber().split(";");
                System.out.println(comandos[0]);
                //Não responde a mensagens de si mesmo
                if (comandos[1].equals(conexao.getId()) == false) {

                    //Enviar para o mestre pedido de sincronização
                    if (comandos[0].equals("sincronizar1")) {
                        if (conexao.getMestre().equals(conexao.getId())) {
                            conexao.enviar("sincronizar2;" + conexao.getId() + ";" + relogio.getContador() + ";" + relogio.getHora() + ";");
                        }
                    }

                    //Recebendo msg de sincronização
                    if (comandos[0].equals("sincronizar2")) {
                        if (Integer.parseInt(comandos[3]) < relogio.getHora() || Integer.parseInt(comandos[3]) < conexao.getHorasMestre()) {
                            conexao.enviar("eleicao1;" + conexao.getId() + ";" + relogio.getContador() + ";" + relogio.getHora());
                        } else if ((Integer.parseInt(comandos[3]) == relogio.getHora()) && (Integer.parseInt(comandos[2]) < conexao.getContMestre()
                                || Integer.parseInt(comandos[2]) < relogio.getContador())) {
                            conexao.enviar("eleicao1;" + conexao.getId() + ";" + relogio.getContador() + ";" + relogio.getHora());
                        } else {
                            relogio.atualizarTempo(Integer.parseInt(comandos[3]), (Integer.parseInt(comandos[2]) - relogio.getDelays()[Integer.parseInt(conexao.getMestre())]));
                        }
                    }

                    //Bullynando o lider
                    if (comandos[0].equals("bullying")) {
                        System.out.println("Bully");
                        if (comandos[2].equals(conexao.getId())) {
                            conexao.enviar("msgrecebida;" + conexao.getId() + ";" + relogio.getContador() + ";" + relogio.getHora());
                            System.out.println("Eu sou o líder");
                            if (Integer.parseInt(comandos[4]) > relogio.getHora()) {
                                conexao.enviar("lidermenor;" + conexao.getId());
                            } else if ((Integer.parseInt(comandos[4]) == relogio.getHora()) && (Integer.parseInt(comandos[3]) > relogio.getContador())) {
                                conexao.enviar("lidermenor;" + conexao.getId());
                            }
                        }
                    }
                    //Recebe msg do lider avisando que é necessário fazer uma nova eleição pois ele tem o horário menor
                    if (comandos[0].equals("lidermenor")) {
                        conexao.setLiderMenor(true);
                    }

                    //Alguem pediu eleição e enviou seu tempo para todos
                    if (comandos[0].equals("eleicao1")) {
                        conexao.setMestre(conexao.getId());
                        if (relogio.getHora() > conexao.getHorasMestre()) {
                            conexao.setHorasMestre(relogio.getHora());
                            conexao.setContMestre(relogio.getContador());
                        } else if ((relogio.getHora() == conexao.getHorasMestre()) && (relogio.getContador() > conexao.getContMestre())) {
                            conexao.setContMestre(relogio.getContador());
                        }
                        conexao.setEleicao(false);
                        if (Integer.parseInt(comandos[3]) > conexao.getHorasMestre()) {
                            conexao.setMestre(comandos[1]);
                        } else if ((Integer.parseInt(comandos[3]) == conexao.getHorasMestre()) && (Integer.parseInt(comandos[2]) > conexao.getContMestre())) {
                            conexao.setMestre(comandos[1]);
                        }
                        conexao.enviar("eleicao2;" + conexao.getId() + ";" + relogio.getContador() + ";" + relogio.getHora());
                    }
                    if (comandos[0].equals("eleicao2")) {
                        if (Integer.parseInt(comandos[3]) > conexao.getHorasMestre()) {
                            conexao.setMestre(comandos[1]);
                        } else if ((Integer.parseInt(comandos[3]) == conexao.getHorasMestre()) && (Integer.parseInt(comandos[2]) > conexao.getContMestre())) {
                            conexao.setMestre(comandos[1]);
                        }
                        conexao.enviar("eleicaoFinal;" + conexao.getId());
                    }
                    //Recebe msg do lider avisando que está td bem
                    if (comandos[0].equals("msgrecebida")) {
                        conexao.setMsgRecebida(true);
                        if (Integer.parseInt(comandos[3]) > relogio.getHora()) {
                            conexao.setMestre(comandos[1]);
                            conexao.setContMestre(Integer.parseInt(comandos[2]));
                            conexao.setHorasMestre(Integer.parseInt(comandos[3]));
                        } else if ((Integer.parseInt(comandos[3]) == relogio.getHora()) && (Integer.parseInt(comandos[2]) > relogio.getContador())) {
                            conexao.setMestre(comandos[1]);
                            conexao.setContMestre(Integer.parseInt(comandos[2]));
                        }
                    }

                    if (comandos[0].equals("eleicaoFinal")) {
                        System.out.println("O líder é o:" + conexao.getMestre());
                        conexao.setEleicao(true);
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(ThreadReceber.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

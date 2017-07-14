package Controller;

import Conexao.Conexao;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.util.Timer;

/**
 * Classe controladora TelaRelogioController, responsável pela implementação dos
 * métodos da interface gráfica.
 *
 * @author Leandro Pereira Sampaio e Bernardo Rosa
 */ 
public class TelaRelogioController {

    /**
     * @return the delays
     */
    public int[] getDelays() {
        return delays;
    }

    /**
     * @param delays the delays to set
     */
    public void setDelays(int[] delays) {
        this.delays = delays;
    }

    private Timer timer;
    private JLabel labelHora;
    private JLabel labelMinuto;
    private JLabel labelSegundo;
    private Date tempo;
    private long delay;
    private int drift = 1000;
    private int id, contador, segundo, minuto, hora;
    private int[] delays;
    private Conexao conexao = Conexao.getInstancia();

    public TelaRelogioController(JLabel labelHora, JLabel labelMinuto, JLabel labelSegundo) {
        this.labelHora = labelHora;
        this.labelMinuto = labelMinuto;
        this.labelSegundo = labelSegundo;
        this.id = 0;
        this.contador = 0;
        this.segundo = 0;
        this.minuto = 0;
        this.hora = 0;
        this.delay = 0;
        this.delays = new int[100];
        this.timer = new Timer();
        this.tempo = new Date();
    }

    /**
     * Método responsável por modificar o horário do relógio em tempo de
     * execução.
     *
     */
    public void alterarTempo(JFormattedTextField novoHorario) {
        System.out.println("NOVO HORÁRIO: " + novoHorario.getText());
        String[] horario = novoHorario.getText().split(":");

        String fieldh = horario[0];
        String fieldm = horario[1];
        String fields = horario[2];

        //Se houver tempo:
        if ((!(fieldh.equals(""))) && (!(fieldm.equals(""))) && (!(fields.equals("")))) {

            if ((!(fieldh.equals(" "))) && (!(fieldm.equals(" "))) && (!(fields.equals(" ")))) {

                // Verificar horário
                if (Integer.parseInt(fieldh) < 24 && Integer.parseInt(fieldm) < 60 && Integer.parseInt(fields) < 60) {
                    labelHora.setText(fieldh);
                    hora = Integer.parseInt(fieldh);
                    labelMinuto.setText(fieldm);
                    labelSegundo.setText(fields);

                    //Condição de modificação da variável contadora:
                    if (fieldm.equals("0")) {
                        setContador((Integer) Integer.parseInt(fields));
                    } else {
                        setContador((Integer) (Integer.parseInt(fieldm) * 60) + Integer.parseInt(fields));
                    }
                } else {
                    JOptionPane.showMessageDialog(labelMinuto, "Por gentileza, coloque um horário válido...");
                }
            }
        }
    }

    /**
     * Método responsável por sincronizar seu horário, com os horários dos
     * demais relógios.
     *
     */
    public void sincronizar() {
        try {
            if (conexao.getMestre().equals(conexao.getId())) {
                conexao.enviar("sincronizar2;" + this.id + ";" + this.contador + ";" + this.hora);
            } else {
                conexao.enviar("sincronizar1;" + conexao.getId());
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(TelaRelogioController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TelaRelogioController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Método que libera o evento do botão de alterar drift, que modifica o
     * drift do relógio em tempo de execução.
     *
     */
    public void clicaAlterarDrift(String driftRecebido) {
        String field = driftRecebido;

        //Se houver drift:
        if ((!(field.equals(""))) && (!(field.equals(" "))) && (!(field.equals("0")))) {
            drift = Integer.parseInt(field);   //Modifica o valor
        }
    }

    /**
     * Método responsável por inicializar a contagem e criar criar uma Thread
     * para recebimento de mensagens.
     *
     */
    public void iniciar() {
        System.out.println("PASSOU!");
        labelHora.setText("0");
        labelMinuto.setText("0");
        labelSegundo.setText("0");

        this.contagem();   //Chama a tarefa

//        try {
//            System.out.println("AQUIIIIII!!!!!!!");
//            this.conexao.enviar("entrar;enviar;" + this.conexao.getId());
//        } catch (IOException ex) {
//            Logger.getLogger(TelaRelogioController.class.getName()).log(Level.SEVERE, null, ex);
//        }
        //this.contagem();   //Chama a tarefa
        ThreadReceber tr = new ThreadReceber(this);
        new Thread(tr).start();
        this.bullying();
    }

    /**
     * Método responsável por fazer a contagem e incrementar nas variáveis do
     * relógio.
     *
     */
    public void contagem() {
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    try {
                        //Contagem ilimitada
                        Thread.sleep(drift);   //No caso, correspondente ao tempo de drift
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TelaRelogioController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    setContador((Integer) (getContador() + 1));   //Variável de controle do tempo
                    segundo = getContador() % 60;
                    labelSegundo.setText(String.valueOf(segundo));
                    minuto = getContador() / 60;

                    if (minuto == 60) {
                        minuto = 0;
                    }
                    labelMinuto.setText(String.valueOf(minuto));

                    if ((getHora() == 23) && (getContador() == 3600)) {   //Final do dia, reinicia toda contagem
                        setHora((Integer) 0);
                        setContador((Integer) 0);
                    }

                    if (getContador() == 3600) {   //Final da hora, incrementa a hora
                        setHora((Integer) (getHora() + 1));
                        setContador((Integer) 0);
                    }
                    labelHora.setText(String.valueOf(hora));
                }
            }
        }.start();
    }

    /**
     * Método responsável por fazer a eleição.
     *
     */
    public void bullying() {
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    System.out.println(conexao.getId());
                    System.out.println(conexao.getMestre());
                    if (conexao.isEleicao()) {
                        try {
                            System.out.println("Bullyng");
                            conexao.setMsgRecebida(false);
                            conexao.setLiderMenor(false);

                            //Testando tempo de atraso
                            if (!conexao.getMestre().equals("9123213")) {
                                tempo = new Date();
                                delay = tempo.getTime();
                            }

                            conexao.enviar("bullying;" + conexao.getId() + ";" + conexao.getMestre() + ";" + getContador() + ";" + getHora());

                            Thread.sleep(200);

                            if (conexao.isMsgRecebida() && !conexao.getMestre().equals("9123213")) {
                                tempo = new Date();
                                long temp = tempo.getTime();
                                delay = (temp - delay) / 2;

                                getDelays()[Integer.parseInt(conexao.getMestre())] = (int) delay;
                                System.out.println("Tempo de atraso: " + delay);
                            }

                            if (conexao.isMsgRecebida() == false && conexao.getMestre().equals(conexao.getId()) == false) { //Líder não recebeu a msg
                                conexao.setHorasMestre(getHora());
                                conexao.setContMestre(contador);
                                System.out.println("Questionando o líder");
                                conexao.enviar("eleicao1;" + conexao.getId() + ";" + getContador() + ";" + getHora());
                            }

                            if (conexao.isLiderMenor()) {
                                conexao.setHorasMestre(getHora());
                                conexao.setContMestre(contador);
                                System.out.println("Questionando o líder");
                                conexao.enviar("eleicao1;" + conexao.getId() + ";" + getContador() + ";" + getHora());
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(TelaRelogioController.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(TelaRelogioController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

        }.start();
    }

    /**
     * Método responsável por atualizar as variáveis do horário.
     *
     */
    public void atualizarTempo(int hora, int contador) {
        this.setHora(hora);
        this.setContador(contador);
        segundo = contador % 60;
        labelSegundo.setText(String.valueOf(segundo));
        minuto = contador / 60;

        if (minuto == 60) {
            minuto = 0;
        }
        labelMinuto.setText(String.valueOf(minuto));

        if ((hora == 23) && (contador == 3600)) {   //Final do dia, reinicia toda contagem
            hora = 0;
            contador = 0;
        }

        if (contador == 3600) {   //Final da hora, incrementa a hora
            hora++;
        }
        labelHora.setText(String.valueOf(hora));
    }

    /**
     * Método responsável por retornar a hora.
     *
     */
    public int getHora() {
        return hora;
    }

    /**
     * Método responsável por adicionar a hora.
     *
     */
    public void setHora(int hora) {
        this.hora = hora;
    }

    /**
     * Método responsável por retornar o contador de tempo.
     *
     */
    public int getContador() {
        return contador;
    }

    /**
     * Método responsável por adicionar um valor ao contador.
     *
     */
    public void setContador(int contador) {
        this.contador = contador;
    }

    /**
     * Método responsável por retornar o id.
     *
     */
    public int getid() {
        return id;
    }

    /**
     * Método responsável por adicionar um valor ao id.
     *
     */
    public void setId(int id) {
        this.id = id;
    }
}

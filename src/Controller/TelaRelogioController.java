package Controller;

import Conexao.Conexao;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Classe controladora TelaRelogioController, responsável pela configuração dos
 * elementos da interface do relógio.
 *
 * @author Leandro Pereira Sampaio
 */
public class TelaRelogioController {

    /**
     * @return the hora
     */
    public Integer getHora() {
        return hora;
    }

    /**
     * @param hora the hora to set
     */
    public void setHora(Integer hora) {
        this.hora = hora;
    }

    /**
     * @return the contador
     */
    public Integer getContador() {
        return contador;
    }

    /**
     * @param contador the contador to set
     */
    public void setContador(Integer contador) {
        this.contador = contador;
    }

    private TextField fieldDrift;
    private JLabel labelAlteraMinuto;
    private JLabel labelHora;
    private JLabel labelMinuto;
    private JLabel labelAlteraHora;
    private JLabel labelSegundo;
    private JLabel labelAlteraSegundo;
    private int drift = 1000;
    private Integer id = 0, contador = 0, segundo = 0, minuto = 0, hora = 0;
    private Conexao conexao = Conexao.getInstancia();

    public TelaRelogioController(JLabel labelHora, JLabel labelMinuto, JLabel labelSegundo) {
        this.labelHora = labelHora;
        this.labelMinuto = labelMinuto;
        this.labelSegundo = labelSegundo;
    }

    /**
     * Método que libera o evento do botão de alterar tempo, que modifica o
     * horário do relógio em tempo de execução.
     *
     * @param event
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
                }
            }
        }
        
        JOptionPane.showMessageDialog(labelMinuto, "Por gentileza, coloque um horário válido...");
    }

    /**
     * Método que libera o evento do botão de alterar drift, que modifica o
     * drift do relógio em tempo de execução.
     *
     * @param event
     */
    @FXML
    void clicaAlterarDrift(ActionEvent event) {
        String field = fieldDrift.getText();

        //Se houver drift:
        if ((!(field.equals(""))) && (!(field.equals(" "))) && (!(field.equals("0")))) {
            drift = Integer.parseInt(field);   //Modifica o valor
        }
    }

    /**
     * Método inicial de carregamento da tela.
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
    }

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
                    labelSegundo.setText(segundo.toString());
                    minuto = getContador() / 60;

                    if (minuto == 60) {
                        minuto = 0;
                    }
                    labelMinuto.setText(minuto.toString());

                    if ((getHora() == 23) && (getContador() == 3600)) {   //Final do dia, reinicia toda contagem
                        setHora((Integer) 0);
                        setContador((Integer) 0);
                    }

                    if (getContador() == 3600) {   //Final da hora, incrementa a hora
                        setHora((Integer) (getHora() + 1));
                        setContador((Integer) 0);
                    }
                    labelHora.setText(getHora().toString());

                    System.out.println(conexao.getId());
                    System.out.println(conexao.getMestre());
                    if (conexao.isEleicao() && Integer.parseInt(conexao.getId()) != Integer.parseInt(conexao.getMestre())) {

                        try {
                                System.out.println("Bullyng");
                                conexao.enviar("bullying;" + conexao.getId()+ ";"+ conexao.getMestre() + ";" + getContador());
                                conexao.setMsgRecebida(true);
                                Thread.sleep(5000);
                                
                                if(conexao.isMsgRecebida()){
                                    System.out.println("Eleito");
                                    conexao.enviar("eleicao1;" + conexao.getId() + ";"+ getContador());
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

    public void atualizarTempo(Integer hora, Integer contador) {
        this.setHora(hora);
        this.setContador(contador);
        segundo = contador % 60;
        labelSegundo.setText(segundo.toString());
        minuto = contador / 60;

        if (minuto == 60) {
            minuto = 0;
        }
        labelMinuto.setText(minuto.toString());

        if ((hora == 23) && (contador == 3600)) {   //Final do dia, reinicia toda contagem
            hora = 0;
            contador = 0;
        }

        if (contador == 3600) {   //Final da hora, incrementa a hora
            hora++;
            contador = 0;
        }
        labelHora.setText(hora.toString());
    }

    public Integer getid() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

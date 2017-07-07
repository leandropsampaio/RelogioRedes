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
import javax.swing.JLabel;

/**
 * Classe controladora TelaRelogioController, responsável pela configuração dos
 * elementos da interface do relógio.
 *
 * @author Leandro Pereira Sampaio
 */
public class TelaRelogioController {

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
    public void alterarTempo(ActionEvent event) {
        String fieldh = labelAlteraHora.getText();
        String fieldm = labelAlteraMinuto.getText();
        String fields = labelAlteraSegundo.getText();

        //Se houver tempo:
        if ((!(fieldh.equals(""))) && (!(fieldm.equals(""))) && (!(fields.equals("")))) {

            if ((!(fieldh.equals(" "))) && (!(fieldm.equals(" "))) && (!(fields.equals(" ")))) {
                labelHora.setText(fieldh);
                //hora = Integer.parseInt(fieldh);
                labelMinuto.setText(fieldm);
                labelSegundo.setText(fields);

                //Condição de modificação da variável contadora:
                if (fieldm.equals("0")) {
                    contador = Integer.parseInt(fields);
                } else {
                    contador = (Integer.parseInt(fieldm) * 60) + Integer.parseInt(fields);
                }
            }
        }
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

        try {
            System.out.println("AQUIIIIII!!!!!!!");
            this.conexao.enviar("entrar;enviar;" + this.conexao.getId());
        } catch (IOException ex) {
            Logger.getLogger(TelaRelogioController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //this.contagem();   //Chama a tarefa
        ThreadReceber tr = new ThreadReceber(this);
        new Thread(tr).start();
    }

    public void contagem() {
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    System.out.println("AAAAAAAAAa");
                    try {
                        //Contagem ilimitada
                        Thread.sleep(drift);   //No caso, correspondente ao tempo de drift
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TelaRelogioController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    contador++;   //Variável de controle do tempo
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

                    if (conexao.getId().equals(conexao.getMestre())) {

                        try {
                            conexao.enviar("enviaTempo;" + conexao.getId() + ";" + hora + ";" + contador);
                        } catch (IOException ex) {
                            Logger.getLogger(TelaRelogioController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }.start();
    }

    public void atualizarTempo(Integer hora, Integer contador) {
        this.hora = hora;
        this.contador = contador;
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

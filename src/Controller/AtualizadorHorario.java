package Controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;

/**
 *
 * @author Leandro Pereira Sampaio
 */
public class AtualizadorHorario extends Thread {

    private final JLabel hr; //label que guarda a hora atual
    private boolean pause; //

    public AtualizadorHorario(JLabel hora) {
        this.hr = hora;
    }

    public void pausarHorario(boolean pause) {
        if (pause) {
            this.pause = true;
        } else {
            this.pause = false;
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Date d = new Date();
                if (pause) {
                    System.out.println("PAUSE");
                } else {
                    System.out.println("PLAY");
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                    this.hr.setText(sdf.format(d));
                    Thread.sleep(1000);
                    this.hr.revalidate();
                }
            }
        } catch (InterruptedException ex) {
            System.out.println("Problema na atualização da hora!");
            ex.printStackTrace();
        }
    }
}

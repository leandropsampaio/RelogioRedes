package Conexao;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;

/**
 *
 * @author Leandro Pereira Sampaio
 */
public class Conexao{

    /**
     * @return the liderMenor
     */
    public boolean isLiderMenor() {
        return liderMenor;
    }

    /**
     * @param liderMenor the liderMenor to set
     */
    public void setLiderMenor(boolean liderMenor) {
        this.liderMenor = liderMenor;
    }

    /**
     * @return the contMestre
     */
    public int getContMestre() {
        return contMestre;
    }

    /**
     * @param contMestre the contMestre to set
     */
    public void setContMestre(int contMestre) {
        this.contMestre = contMestre;
    }

    /**
     * @return the eleicao
     */
    public boolean isEleicao() {
        return eleicao;
    }

    /**
     * @param eleicao the eleicao to set
     */
    public void setEleicao(boolean eleicao) {
        this.eleicao = eleicao;
    }

    /**
     * @return the msgRecebida
     */
    public boolean isMsgRecebida() {
        return msgRecebida;
    }

    /**
     * @param msgRecebida the msgRecebida to set
     */
    public void setMsgRecebida(boolean msgRecebida) {
        this.msgRecebida = msgRecebida;
    }

    private static Conexao Conexao;
    private final int PORTA = 5000;
    private final String GRUPO = "225.7.2.8";
    private MulticastSocket multicast;
    private String id;
    private String mestre;
    private boolean msgRecebida;
    private boolean eleicao;
    private int contMestre;
    private boolean liderMenor;

    /**
     * Método que inicializa a classe.
     *
     */
    public static void singleton() throws UnknownHostException, IOException {
        Conexao = new Conexao();
    }

    /**
     * Método que retorna a instância da classe.
     *
     */
    public static Conexao getInstancia() {
        return Conexao;
    }

    /**
     * Método que conecta ao grupo.
     *
     */
    public void conectar() throws IOException {
        this.multicast = new MulticastSocket(this.PORTA);
        this.multicast.joinGroup(InetAddress.getByName(this.GRUPO));
        this.eleicao = true;
        this.msgRecebida = true;
        this.setLiderMenor(false);
        System.out.println("Conectou!");
        //this.multicast.setSoTimeout(2000);
    }

    /**
     * Método que desconecta do grupo.
     *
     */
    public void desconectar() throws IOException {
        this.multicast.leaveGroup(InetAddress.getByName(this.GRUPO));
        this.multicast.close();
    }


    public void enviar(String s) throws SocketException, UnknownHostException, IOException {
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = s.getBytes();
        socket.send(new DatagramPacket(buf, buf.length, InetAddress.getByName(this.GRUPO), this.PORTA));
        socket.close();
    }

    /**
     * Método que recebe uma string do grupo e a retorna.
     *
     */
    public String receber() throws IOException, SocketTimeoutException {
        byte[] buf = new byte[256];
        DatagramPacket pack = new DatagramPacket(buf, buf.length);
        this.multicast.receive(pack);
        return (new String(pack.getData()).trim());
    }

    public String getMestre() {
        return mestre;
    }

    public void setMestre(String mestre) {
        this.mestre = mestre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.*;


public class ChatFrame extends JFrame implements ActionListener, ConnectionListener {

    private static final int width = 600;
    private static final int height = 400;

    private final static int serverPort = Config.PORT;
    private final static String host = "localhost";
    private ServerListenerThread connection;

    private JTextArea log;
    private JTextField nickNameField;
    private JTextField chatField;

    public ChatFrame() {
        log = new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        nickNameField = new JTextField("Очередной");
        nickNameField.setEditable(false);
        chatField = new JTextField();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);
        add(log);
        add(log, BorderLayout.CENTER);
        add(nickNameField, BorderLayout.NORTH);
        add(chatField, BorderLayout.SOUTH);

        chatField.addActionListener(this);
        try {
            connection = new ServerListenerThread(this, host, serverPort);
        } catch (IOException ex) {
            System.out.println(ex);
        }

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = chatField.getText();
        if(msg.equals("")) return;
        chatField.setText(null);
        Message mess = new Message(nickNameField.getText(), msg);
        connection.send(mess);
    }

    @Override
    public void onConnectionReady(ServerListenerThread tcpConnection) {
        Message mess = new Message(null, "Connection ready...");
        printMsg(mess);
    }

    @Override
    public void onReceiveMessage(ServerListenerThread сonnect, Message message) {
        printMsg(message);
    }

    @Override
    public void onDisconnect(ServerListenerThread сonnect){
        Message mess = new Message(null, "Connection closed...");
        printMsg(mess);
    }

    @Override
    public void onException(ServerListenerThread сonnect, Exception e) {
        Message mess = new Message(null, "Connection closed..." + ": " + e);
        printMsg(mess);
    }

    private synchronized void printMsg(Message msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("[" + msg.getFrom() + "]" + " " + msg.getMessage() + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatFrame();
            }
        });
    }
}
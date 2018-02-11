import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;

/**
 * Created by Igor on 03.02.2018.
 */
public class AuthForm {
    private JPanel rootPanel;
    private JButton authorizeButton;
    private JButton registrationButton;
    private JTextField passwordTextField;
    private JTextField loginTextField;

    private JFrame frame;

    private static int serverPort;
    private final static String host = Config.HOST;
    private ConnectToClient connection;
    private static Socket socket = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;




    public AuthForm(Socket socket) throws IOException{
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        this.connection = new ServerListenerThread(socket, outputStream, inputStream);

        connection.send("Сlient");
        frame = new JFrame("Chat ver 1.0.");
        frame.setContentPane(rootPanel);
        frame.setSize(300,400);


        authorizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String login = loginTextField.getText();
                String password = passwordTextField.getText();
                try {
                    if(!connection.authorization(login, password)){
                        JOptionPane.showMessageDialog(frame, "Ошибка при авторизации");
                    }else{
                        Account account = new Account(login, password);
                        new ChatForm(account, connection);
                        frame.dispose();
                    }
                }catch (IOException error){
                    JOptionPane.showMessageDialog(frame, "Ошибка при авторизации");
                }
            }
        });

        registrationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String login = loginTextField.getText();
                String password = passwordTextField.getText();
                try{
                    String status = connection.registration(login, password);
                    System.out.println(login + password);
                    System.out.println(status);
                    if(status.equals("#Success")){
                        JOptionPane.showMessageDialog(frame, "Пользователь " +login+ " успешно зарегестрирован");
                    }else if(status.equals("#alreadyRegistered")){
                        JOptionPane.showMessageDialog(frame, "Данный пользователь уже зарегистрирован");
                    }else if(status.equals("#incorrectPassword")){
                        JOptionPane.showMessageDialog(frame, "Неверный формат пароля");
                    }
                }catch (IOException error){
                    JOptionPane.showMessageDialog(frame, "Ошибка при регистрации");
                }
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String [] args) throws IOException{

        System.out.println("Введите порт!\n");
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        int p = Integer.parseInt(keyboard.readLine());
        setPort(p);
        System.out.println("Port:" + serverPort);

        try {
            socket = new Socket(host, serverPort);
        }catch (IOException e){
            System.out.println("Unable to connect to server.");
            System.exit(0);
        }

        new AuthForm(socket);
    }

    public static void setPort(int p){
        for(int i=0;i<Config.PORTARRAY.length;i++)
            if(Config.PORTARRAY[i]==p)
                serverPort=p;
    }


}

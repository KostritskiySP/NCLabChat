import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import Entities.Message;
/**
 * Created by Igor on 04.02.2018.
 */
public class ChatForm implements ActionListener, ConnectionListener  {
    private JTextField topBar;
    private JTextField dawnBar;
    private JTextArea chat;
    private JPanel rootPanel;
    private JTextArea onlineUsers;
    private JButton onlineButton;
    private JFrame frame;
    private JMenuBar menuBar = null;

    private ConnectToClient connection;
    private Thread thread = null;

    private Account account;

    public ChatForm(Account account, ConnectToClient connection) {

        this.connection = connection;
        this.account = account;

        frame = new JFrame("Chat ver 1.0.");
        menuBar = new JMenuBar();
        menuBar.add(menu());
        onlineUsers.setEditable(false);
        frame.setJMenuBar(menuBar);
        //connection.send("Client");
        topBar.setEditable(false);
        topBar.setText("Пользователь: " + account.getLogin());
        chat.setEditable(false);
        frame.setContentPane(rootPanel);
        frame.setSize(600,300);
        dawnBar.addActionListener(this);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        chat.append("Вы вошли в чат, как " + account.getLogin() + "." + "\n");
        chat.append("Наберите сообщение и нажмите Enter." + "\n");
        connection.startThread(this);
        onlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connection.sendOnlineRequest();

                } catch (InterruptedException error) {
                }
            }
        });

        frame.setVisible(true);


    }

    private JMenu menu(){

        JMenu file = new JMenu("Файл");
        JMenuItem exit = new JMenuItem("Выход");
        JMenuItem about = new JMenuItem("О программе");

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connection.disconnect();
                    frame.dispose();
                }catch (IOException error){
                    JOptionPane.showMessageDialog(frame, "Ошибка,при закрытии приложения");
                }
            }
        });

        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Программа реализует средство обмена сообщениями по компьютерной сети в режиме реального времени. \n Авторы:\n Кострицкий Сергей \n Сысоев Игорь \n Тимофеева Анастасия ");
            }
        });

        file.add(about);
        file.add(exit);

        return file;

    }

    public synchronized void printMessage(Message message){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (message.getFrom().equals("SERVER")){
                    chat.append("SERVER : " + message.getMessage() + "\n");
                    try {
                        connection.sendOnlineRequest();
                        System.out.println("запрос отправлен");
                    }catch (InterruptedException ex){

                    }
            }else chat.append("[ " + message.getFrom() + " ] : " + message.getMessage() + "\n");
            }
        });
    }

    public synchronized void showOnline(ArrayList<String> onlineUserList) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println(onlineUserList);
                ArrayList<String> filteredList = new ArrayList();
                for(String next: onlineUserList) {
                    if (!filteredList.contains(next)) {
                        filteredList.add(next);
                    }
                }
                onlineUsers.setText("Пользователи онлайн: \n");
                for(int i = 0; i < filteredList.size(); i++){
                    onlineUsers.append(i +". " + filteredList.get(i) + "\n");
                }
            }
        });
    }




    @Override
    public void actionPerformed(ActionEvent e) {
       String message = dawnBar.getText();
       if(message.equals("!logOut") || message.equals("!online") || message.equals("!AUTHORIZE") || message.equals("!REGISTRATION") || message.equals("!disconnect") || message.equals("!history")){
           chat.append("Невозможно ввести команду \n");
       }else{
           connection.send(message);
           connection.send("!OK");
       }
       dawnBar.setText("");
    }


    @Override
    public void onConnectionReady(ServerListenerThread tcpConnection) {

    }

    @Override
    public void onReceiveMessage(ServerListenerThread сonnect, Message message) {
        printMessage(message);
    }

    @Override
    public void onlineUsers(ServerListenerThread сonnect, ArrayList<String> onlineList) {
        showOnline(onlineList);
    }

    @Override
    public void onException(ServerListenerThread сonnect, Exception e) {

    }


}

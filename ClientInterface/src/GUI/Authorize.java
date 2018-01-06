package GUI;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Authorize extends JFrame implements ActionListener {

    JTextField login;
    JPasswordField password;
    JLabel loginlbl;
    JLabel pwdlbl;
    JButton submitbtn;
    JPanel formPanel;
    JLabel massage;

    public Authorize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(330, 200);
        setTitle("Авторизация");

        massage = new JLabel("Зарегистрируйтесь, чтобы воспользоваться чатом");
        login = new JTextField();
        password = new JPasswordField();
        loginlbl = new JLabel("Логин:");
        pwdlbl = new JLabel("Пароль:");
        submitbtn = new JButton("Принять");

        formPanel = new JPanel(new GridLayout(6, 1));
        formPanel.add(massage);
        formPanel.add(loginlbl);
        formPanel.add(login);
        formPanel.add(pwdlbl);
        formPanel.add(password);
        formPanel.add(submitbtn);
        add(formPanel);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

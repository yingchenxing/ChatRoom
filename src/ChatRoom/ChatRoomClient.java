package ChatRoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class ChatRoomClient extends JFrame {
    private String connectionAddress = "127.0.0.1";
    private int connectionPort = 6666;
    private String name;


    private final JTextArea ta = new JTextArea(10, 20);
    private final JScrollPane sp = new JScrollPane(ta);
    private final JTextField tf = new JTextField(20);
    private final JButton sendBtn = new JButton("发送");
    private final JPanel jp = new JPanel();
    private Socket socket = null;
    private boolean isOnline = false;//标志是否连接服务器

    private void initClient() {
        this.setTitle(name);
        this.add(sp, BorderLayout.CENTER);
        jp.add(tf);
        jp.add(sendBtn);
        this.add(jp, BorderLayout.SOUTH);
        this.setBounds(300, 300, 300, 400);

        //设置监听
        AbstractAction listener = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String strSend = tf.getText();
                if (strSend.isEmpty())
                    return;
                //发送方法
                send(name + ":" + strSend);
                tf.setText("");
            }
        };
        tf.addActionListener(listener);
        sendBtn.addActionListener(listener);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ta.setEditable(false);
        tf.requestFocus();

//发送登录信息到服务器
        try {
            socket = new Socket(connectionAddress, connectionPort);
            send(name + "进入了服务器！");
            isOnline = true;

        } catch (ConnectException e) {
            System.out.println("连接失败，服务器不在线或地址填写错误！");
            ta.append("连接失败，服务器不在线！");
        } catch (SocketException e) {
            System.out.println("连接失败，地址错误！");
            ta.append("连接失败，地址错误！");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setVisible(true);
        new Thread(new ReceiveInfo()).start();
    }

    //发送信息到服务器上
    public void send(String str) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            ta.append("服务器未连接，请检查服务器是否开启！\n");
        }
    }

    //接受服务器的信息的线程
    class ReceiveInfo implements Runnable {
        @Override
        public void run() {
            try {
                while (isOnline) {
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    String str = dis.readUTF();
                    ta.append(str + "\n");
                }
            } catch (SocketException e) {
                System.out.println("服务器意外中断！");
                ta.append("服务器意外中断！");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void login() {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 200);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);

        JLabel userLabel = new JLabel("User:");
        userLabel.setBounds(10, 20, 80, 26);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);


        JLabel addressLabel = new JLabel("server:");
        addressLabel.setBounds(10, 50, 80, 25);
        panel.add(addressLabel);

        JTextField addressText = new JTextField(20);
        addressText.setBounds(100, 50, 165, 25);
        panel.add(addressText);

        JButton loginButton = new JButton("connect");
        loginButton.setBounds(10, 80, 80, 25);
        panel.add(loginButton);

        //设置相关事件
        loginButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!userText.getText().isEmpty())
                    name = userText.getText();
                else
                    name = "匿名用户";

                if (!addressText.getText().isEmpty()) {
                    connectionAddress = addressText.getText().split(":", 2)[0];
                    try {
                        connectionPort = Integer.parseInt(addressText.getText().split(":", 2)[1]);
                    } catch (ArrayIndexOutOfBoundsException e1) {
                        System.out.println("输入格式错误，请重新输入！");
//                        JTextArea wrong = new JTextArea("输入格式错误，请重新输入！");
//                        frame.add(wrong);
                    }
                }
                System.out.println(connectionAddress + ":" + connectionPort);
                frame.setVisible(false);
                initClient();

            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        ChatRoomClient chatRoomClient = new ChatRoomClient();
        chatRoomClient.login();
    }


}

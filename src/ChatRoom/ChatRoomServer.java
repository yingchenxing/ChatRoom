package ChatRoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

public class ChatRoomServer extends JFrame {
    private static final int PORT = 6666;

    private final JTextArea serverTa = new JTextArea();
    private final JScrollPane serverSp = new JScrollPane(serverTa);
    private final JPanel btnTool = new JPanel();
    private final JButton startBtn = new JButton("启动服务器");
    private final JButton stopBtn = new JButton("关闭服务器");
    JLabel addressLabel;
    JTextArea addressTa;
    JLabel portLabel;
    JTextArea portTa;
    JLabel onlineLabel;
    JTextArea onlineTa;
    JPanel labelPanel;

    private ServerSocket socket = null;
    private Socket s = null;

    private final ArrayList<ClientConnection> connectionList = new ArrayList<>();//用于存放连接的客户端类

    private boolean isOnline = false;
    private final int onlineNum;

    public ChatRoomServer() throws IOException {
        showMenu();
        onlineNum = 0;

        if (isOnline) {
            serverTa.append("服务器已连接！\n");
        } else {
            serverTa.append("服务器未连接！\n");
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                isOnline = false;
                try {
                    if (s != null) {
                        s.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                    serverTa.append("服务器断开！");
                    System.exit(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        stopBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isOnline = false;
                try {
                    if (s != null) {
                        s.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                    serverTa.append("服务器断开！");
                    System.exit(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });

        startBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (socket == null) {
                        socket = new ServerSocket(PORT);
                    }

                    isOnline = true;
                    serverTa.append("服务器已连接！\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


        //窗口关闭时服务器也关闭
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverTa.setEditable(false);
        this.setVisible(true);
        startServer();
    }

    //服务器启动的方法
    public void startServer() {
        try {
            socket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isOnline = true;

        //可以接受多个客户端的连接
        try {
            while (isOnline) {
                s = socket.accept();
                connectionList.add(new ClientConnection(s));
                onlineTa.setText(connectionList.size() + "");
            }
        } catch (SocketException e) {
            System.out.println("服务器中断！");
        } catch (IOException e) {
            System.out.println("发生读写错误！");
        }
    }

    //服务器端的连接对象
    class ClientConnection implements Runnable {
        Socket s = null;
        String name = null;

        public ClientConnection(Socket s) {
            this.s = s;
            (new Thread(this)).start();
        }

        //同时接受客户端信息

        @Override
        public void run() {
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(s.getInputStream());

                //用于持续接受信息
                while (isOnline) {
                    String str = dis.readUTF();
                    System.out.println(str);
                    if (name == null) {
                        name = str.substring(0, str.length() - 7);
                    }
                    serverTa.append(str + "\n");

                    //遍历list来调用send方法
                    Iterator<ClientConnection> it = connectionList.iterator();
                    while (it.hasNext()) {
                        ClientConnection out = it.next();
                        out.send(str);
                    }
                }
            } catch (SocketException e) {
                if (name == null) {
                    System.out.println("一个客户端下线了");
                    serverTa.append(s.getInetAddress() + "|" + s.getPort() + "客户端下线了\n");
                } else {
                    System.out.println(name + "下线了");
                    serverTa.append(name + "下线了\n");
                }

                Iterator<ClientConnection> it = connectionList.iterator();
                while (it.hasNext()) {
                    ClientConnection out = it.next();
                    out.send(name + "下线了！");
                }
                //从list中移除s
                connectionList.remove(this);
                onlineTa.setText(connectionList.size() + "");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //每个类发送数据的方法
        public void send(String str) {
            try {
                DataOutputStream dos = new DataOutputStream(this.s.getOutputStream());
                dos.writeUTF(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void showMenu() {
        this.setTitle("聊天室服务器");
        addressLabel = new JLabel("服务器地址:");
        addressTa = new JTextArea("127.0.0.1");
        addressTa.setEditable(false);
        portLabel = new JLabel("服务器端口：");
        portTa = new JTextArea(PORT + "");
        portTa.setEditable(false);
        onlineLabel = new JLabel("在线人数：");
        onlineTa = new JTextArea(connectionList.size() + "");
        onlineTa.setEditable(false);
        labelPanel = new JPanel();
        labelPanel.add(addressLabel);
        labelPanel.add(addressTa);
        labelPanel.add(portLabel);
        labelPanel.add(portTa);
        labelPanel.add(onlineLabel);
        labelPanel.add(onlineTa);
        labelPanel.setBounds(0, 0, 500, 200);

        this.add(labelPanel, BorderLayout.NORTH);
        this.add(serverSp, BorderLayout.CENTER);
        btnTool.add(startBtn);
        btnTool.add(stopBtn);
        this.add(btnTool, BorderLayout.SOUTH);
        this.setBounds(0, 0, 500, 300);
    }

    public static void main(String[] args) {
        try {
            ChatRoomServer sc = new ChatRoomServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

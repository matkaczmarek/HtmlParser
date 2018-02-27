/**
 * Created by mat_k on 29.12.2017.
 */

import javafx.scene.control.ComboBox;

import java.sql.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;

public class GUI extends JFrame {

    class JDBC{

        void createNewDatabase() {

            String url = "jdbc:sqlite:dbHistory.sqlite";

            try (Connection conn = DriverManager.getConnection(url)) {
                if (conn != null) {
                    DatabaseMetaData meta = conn.getMetaData();
                    System.out.println("The driver name is " + meta.getDriverName());
                    System.out.println("A new database has been created.");
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage() + "1");
            }
        }

        void createNewTable() {

            String url = "jdbc:sqlite:dbHistory.sqlite";

            String sql = "CREATE TABLE IF NOT EXISTS history (\n"
                    + "	link text PRIMARY KEY,\n"
                    + " date_ INTEGER\n"
                    + ");";

            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement()) {
                // create a new table
                stmt.execute(sql);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        public void update(String link) {
            String sql = "UPDATE history SET date_ = strftime('%s','now') "
                    + "WHERE link = ?";

            String url = "jdbc:sqlite:dbHistory.sqlite";

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // set the corresponding param
                pstmt.setString(1, link);
                // update
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        void insert(String link) throws SQLException {
            String sql = "INSERT INTO history(link, date_) VALUES(?, strftime('%s','now'))";

            String url = "jdbc:sqlite:dbHistory.sqlite";

            Connection conn = DriverManager.getConnection(url);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, link);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage() + "2");
                update(link);
            }
        }

        List<String> selectAll() throws SQLException {
            String sql = "SELECT link FROM history ORDER BY date_ DESC";

            String url = "jdbc:sqlite:dbHistory.sqlite";

            Connection conn = DriverManager.getConnection(url);

            ResultSet resultSet = null;

            java.util.List<String> links = new ArrayList<String>();

            try (Statement stmt  = conn.createStatement();
                 ResultSet rs    = stmt.executeQuery(sql)){

                resultSet = rs;

                // loop through the result set

                while (rs.next())
                    links.add(rs.getString("link"));


            } catch (SQLException e) {
                System.out.println(e.getMessage() + "3");
            }


            return links;
        }

        ResultSet selectLink(String link) throws SQLException {
            String sql = "SELECT link FROM history WHERE link = " + "'" + link + "';" ;

            String url = "jdbc:sqlite:dbHistory.sqlite";

            Connection conn = DriverManager.getConnection(url);

            ResultSet rs = null;

            try (Statement stmt  = conn.createStatement();
                ResultSet s  = stmt.executeQuery(sql)){

                rs = s;

            } catch (SQLException e) {
                System.out.println(e.getMessage() + "4");
            }

            return rs;
        }
    }

    private JButton start = new JButton("Start");
    private JLabel label = new JLabel("Hello");
    private JLabel labelLinks = new JLabel("Links");
    private JLabel labelHistory = new JLabel("History");
    private JLabel labelAddress = new JLabel("Type some address here");
    private JTextField adr = new JTextField("Type some adress.");
    private JProgressBar progressBar = new JProgressBar();
    private JList list = new JList<String>();
    private JScrollPane scrollPane = new JScrollPane(list);
    private JComboBox<String> comboBox = new JComboBox<String>();
    private JComboBox<String> historyComboBox = new JComboBox<String>();
    private int prog = 0;

    private GUI() throws SQLException {
        setLayout(null);
        initUI();
    }

    private void initUI() throws SQLException {

        JDBC jdbc = new JDBC();

        jdbc.createNewDatabase();
        jdbc.createNewTable();

        start.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Parser parser = new Parser() {
                    @Override
                    protected void done() {
                        if (!this.isCancelled()) {

                            start.setEnabled(true);
                            for (String link : links)
                                comboBox.addItem(link);

                            historyComboBox.removeAllItems();

                            List<String> resultSet = new ArrayList<String>();

                            try {
                                resultSet = jdbc.selectAll();
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }

                            try {
                                for(String x : resultSet)
                                    historyComboBox.addItem(x);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                            label.setText(String.format("Images found: %d size of %.2f MB ( %d bytes ) and %d links", set.size(), getSum() / (1024.0 * 1024.0), getSum(), links.size()));
                        }
                    }

                    @Override
                    protected void process(java.util.List<String> chunks) {
                        progressBar.setMaximum(set.size());
                        prog += chunks.size();
                        progressBar.setValue(prog);
                        progressBar.setString(prog + "/" + set.size());
                        progressBar.setStringPainted(true);
                    }
                };

                start.setEnabled(false);

                prog = 0;

                label.setText("Computing...");

                parser.setAddress(adr.getText());

                try {
                    jdbc.insert(parser.getAddress());
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

                comboBox.removeAllItems();

                progressBar.setValue(0);
                progressBar.setString("");

                parser.execute();
            }
        });

        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adr.setText((String) comboBox.getSelectedItem());
            }
        });

        historyComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adr.setText((String) historyComboBox.getSelectedItem());
            }
        });

        start.setFont(new Font("Arial", Font.PLAIN, 18));
        start.setBounds(100, 100, 100, 100);

        label.setFont(new Font("Arial", Font.PLAIN, 18));
        label.setBounds(100, 200, 600, 200);

        adr.setFont(new Font("Arial", Font.PLAIN, 14));
        adr.setBounds(250, 150, 500, 50);

        labelAddress.setFont(new Font("Arial", Font.PLAIN, 12));
        labelAddress.setBounds(250, 110, 250, 50);

        progressBar.setBounds(100, 350, 400, 50);

        labelLinks.setFont(new Font("Arial", Font.PLAIN, 12));
        labelLinks.setBounds(100, 410, 50, 50);

        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBounds(100, 450, 400, 50);

        labelHistory.setFont(new Font("Arial", Font.PLAIN, 12));
        labelHistory.setBounds(100, 510, 50, 50);

        historyComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        historyComboBox.setBounds(100, 550, 400, 50);

        List<String> resultSet = jdbc.selectAll();

        for (String x : resultSet)
            historyComboBox.addItem(x);

        add(start);
        add(labelLinks);
        add(label);
        add(adr);
        add(progressBar);
        add(comboBox);
        add(labelHistory);
        add(historyComboBox);
        add(labelAddress);

        setTitle("URL analyzer");
        setSize(800, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void createLayout(JComponent... arg){

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addComponent(arg[0])
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addComponent(arg[0])
        );
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            GUI ex = null;
            try {
                ex = new GUI();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ex.setVisible(true);
        });
    }
}

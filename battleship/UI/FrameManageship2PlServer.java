package battleship.UI;
/* Name: Phan Manh Son
 ID: ITDSIU21116
 Purpose: Battle ship game which play by human vs computer
*/

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;

import battleship.Map;

public class FrameManageship2PlServer extends JFrame implements ActionListener, KeyListener {
    private static final long serialVersionUID = 2923975805665801740L;
    private static final int NUM_NAVI = 10;
    LinkedList<int[]> myShips;// contiene le navi inserite,serve per
    LinkedList<int[]> advShips; // costruire la frameBattle
    boolean finito = false;
    int naviInserite = 0;
    int[] counterShip = { 1, 2, 3, 4 };
    Map mappa;
    UIManagePanel choosePan;
    UIMapPanel mapPanel;

    public FrameManageship2PlServer() {
        super("Burning Battle");
        mappa = new Map();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(900, 672);
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/res/images/icon.png")));
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
        UIJPanelBG container = new UIJPanelBG(
                Toolkit.getDefaultToolkit().createImage(getClass().getResource("/res/images/wood.jpg")));
        mapPanel = new UIMapPanel("manage");
        container.add(mapPanel);
        choosePan = new UIManagePanel();
        container.add(choosePan);
        mapPanel.setBounds(25, 25, 600, 620);
        choosePan.setBounds(580, 25, 280, 800);
        // Pannello interno contenente le navi da posizionare.
        this.add(container);
        for (int i = 0; i < mapPanel.bottoni.length; i++) {
            for (int j = 0; j < mapPanel.bottoni[i].length; j++) {
                mapPanel.bottoni[i][j].addActionListener(this);
                mapPanel.bottoni[i][j].setActionCommand("" + i + " " + j);
            }
        }
        choosePan.random.addActionListener(this);
        choosePan.reset.addActionListener(this);
        choosePan.gioca.addActionListener(this);
        myShips = new LinkedList<int[]>();
        advShips = new LinkedList<int[]>();
        new ReciveShipsAdv().start();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        String testo = source.getText();
        // RESET
        if (testo.equals("reset")) {
            reset();
        }
        // RANDOM
        else if (testo.equals("random")) {
            random();
        }
        // GIOCA
        else if (testo.equals("gioca")) {
            gioca();

        } else {
            if (finito) {
                return;
            }
            StringTokenizer st = new StringTokenizer(source.getActionCommand(), " ");
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int nave = -1;
            int dim = 0;
            int dir;
            for (int i = 0; i < choosePan.ship.length; i++) {
                if (choosePan.ship[i].isSelected())
                    nave = i;
            }
            switch (nave) {
                case 0:
                    dim = 4;
                    break;
                case 1:
                    dim = 3;
                    break;
                case 2:
                    dim = 2;
                    break;
                case 3:
                    dim = 1;
                    break;
            }
            if (choosePan.direction[0].isSelected())// 0=orizzontale 1=verticale
                dir = 0;
            else
                dir = 1;
            boolean inserito = mappa.insertShip(x, y, dim, dir);
            if (inserito) {
                // incrementa il numero di navi inserite
                naviInserite++;
                // decrementa il contatore della nave inserita
                counterShip[nave]--;
                choosePan.counterLabel[nave].setText("" + counterShip[nave]);
                // disabilita la nave se sono state inserite tutte e seleziona
                // automaticamente un'altra nave da inserire
                if (choosePan.counterLabel[nave].getText().equals("0")) {
                    choosePan.ship[nave].setEnabled(false);
                    for (int i = 0; i < choosePan.ship.length; i++) {
                        if (choosePan.ship[i].isEnabled() && !choosePan.ship[i].isSelected()) {
                            choosePan.ship[i].setSelected(true);
                            break;
                        }
                    }
                }
                // verifica se abbiamo inserito tutte le navi (10)
                if (naviInserite == NUM_NAVI) {
                    finito = true;
                    choosePan.direction[0].setEnabled(false);
                    choosePan.direction[1].setEnabled(false);
                    choosePan.gioca.setEnabled(true);
                }
                int[] dati = { x, y, dim, dir };
                myShips.add(dati);
                mapPanel.disegnaNave(dati);
            }
        }
        this.requestFocusInWindow();
    }

    private void random() {
        if (naviInserite == NUM_NAVI) {
            reset();
        }
        Random r = new Random();
        int[] dati = new int[4];
        for (int i = 0; i < counterShip.length; i++) {
            for (int j = 0; j < counterShip[i]; j++) {
                dati = mappa.insertRandomShip(r, counterShip.length - i);
                myShips.add(dati);
                mapPanel.disegnaNave(dati);
            }
        }
        naviInserite = NUM_NAVI;
        finito = true;
        choosePan.gioca.setEnabled(true);
        for (int i = 0; i < choosePan.ship.length; i++) {
            choosePan.ship[i].setEnabled(false);
        }
        choosePan.direction[0].setEnabled(false);
        choosePan.direction[1].setEnabled(false);
        for (int i = 0; i < counterShip.length; i++) {
            counterShip[i] = 0;
            choosePan.counterLabel[i].setText("0");
        }
        choosePan.ship[0].setSelected(true);

    }

    private void reset() {
        mappa = new Map();
        myShips = new LinkedList<int[]>();
        for (int i = 0; i < Map.DIM_map; i++) {
            for (int j = 0; j < Map.DIM_map; j++) {
                mapPanel.bottoni[i][j].setEnabled(true);
            }
        }
        finito = false;
        choosePan.gioca.setEnabled(false);
        for (int i = 0; i < choosePan.ship.length; i++) {
            choosePan.ship[i].setEnabled(true);
        }
        choosePan.direction[0].setEnabled(true);
        choosePan.direction[1].setEnabled(true);
        for (int i = 0; i < counterShip.length; i++) {
            counterShip[i] = i + 1;
            choosePan.counterLabel[i].setText("" + (i + 1));
        }
        choosePan.ship[0].setSelected(true);
        naviInserite = 0;
    }

    private void gioca() {

        FrameBattle2PL battle = new FrameBattle2PL(myShips, advShips, mappa);
        battle.frame.setVisible(true);
        this.setVisible(false);
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        char s = Character.toLowerCase(arg0.getKeyChar());
        int tasto = arg0.getKeyCode();
        if (s == 'g') {

            random();
            gioca();
        } else {
            if (s == 'r') {
                random();
            } else {
                if (tasto == KeyEvent.VK_DELETE || tasto == KeyEvent.VK_BACK_SPACE) {
                    reset();
                } else {
                    if (tasto == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                }
                if (tasto == KeyEvent.VK_ENTER) {
                    if (finito) {
                        gioca();
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {

    }

    @Override
    public void keyTyped(KeyEvent arg0) {

    }

    class ReciveShipsAdv extends Thread {
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(8081);
                Socket s = ss.accept();
                ObjectInputStream input;

                input = new ObjectInputStream(s.getInputStream());

                advShips = (LinkedList<int[]>) input.readObject();
                System.out.println("Ho ricevuto: \t" + advShips);
                //ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());

                ss.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}

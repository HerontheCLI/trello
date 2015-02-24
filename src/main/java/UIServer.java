import com.google.gson.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.trello4j.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import org.trello4j.model.*;
import java.awt.*;

public class UIServer {
    static JFrame frame;

    public static void createAndShowGUI() {
        frame = new JFrame("Trello Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(200, 0);
        frame.setResizable(true);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setOpacity(0.9f);

        FlowLayout experimentLayout = new java.awt.FlowLayout();
        //experimentLayout.setAlignment(java.awFlowLayout);
        frame.setLayout(experimentLayout);

        //frame.pack();
        //frame.setVisible(true);

        try {
            // Start TrelloClient WorkerThread, this call is non-blocking
            TrelloClient.GetInstance().execute();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void clearLists() {
        frame.getContentPane().removeAll();
    }

    public static void addList(final String sListId, final List<Card> listOfCards) {
        JPanel f = new ListPanel(sListId, listOfCards);
        frame.getContentPane().add(f);
        frame.pack();
        frame.setVisible(true);
    }
}

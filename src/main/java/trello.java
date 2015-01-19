import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.trello4j.model.*;
import org.trello4j.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import java.io.*;

import com.google.gson.*;

class UIServer
{
    private static JFrame frame;
    private static TrelloClient tClient;

    public static void main(String[] args)
    {
        tClient = new TrelloClient();

        try
        {
            tClient.initialize();
        }
        catch (Exception e)
        {
            System.out.println(e);
            return;
        }

        if (args.length > 0 && args[0].equals("-?"))
        {

            String NEW_LINE = System.getProperty("line.separator");
            System.out.println(
                    "trello client 0.1" + NEW_LINE
                    + "-? this hel file" + NEW_LINE
                    + "-b show boards i have access to" + NEW_LINE
                );
            return;
        }

        if (args.length > 0 && args[0].equals("-b"))
        {
            try
            {
                tClient.showMyBoards();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            return;
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI()
    {
        // Create and set up the window.
        frame = new JFrame("Trello Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocation(1000,0);
        frame.setResizable(true);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop( true );
        frame.setOpacity(0.5f); //?

        JButton button = new JButton("this is a long text");
        button.setForeground(Color.blue);
        //button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        //button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent ev) {
                    if (ev.isPopupTrigger()) {
                    System.out.println("right pressed clicked!");
                    //menu.show(ev.getComponent(), ev.getX(), ev.getY());
                    }
                }

                public void mouseReleased(MouseEvent ev) {
                    if (ev.isPopupTrigger()) {
                    System.out.println("right released clicked!");
                    //menu.show(ev.getComponent(), ev.getX(), ev.getY());
                    }
                }

                public void mouseClicked(MouseEvent ev) {
                    System.out.println("left clicked!");
                }
            });

        frame.getContentPane().add(button);

        JButton b2 = new JButton("dos");
        frame.getContentPane().add(b2);

        java.awt.FlowLayout experimentLayout = new java.awt.FlowLayout();
        frame.setLayout(experimentLayout);

        // Display the window.
        frame.pack();
        frame.setVisible(true);

        // Start TrelloClient WorkerThread
        try
        {
            //tClient.execute();
        }
        catch (Exception e)
        {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void update(int n, Card c)
    {
        JButton b = (JButton)frame.getContentPane().getComponent(n);
        b.setText(c.getName());
        frame.pack();
        frame.setVisible(true);
    }
}

class TrelloClient extends SwingWorker<Integer, Integer>
{
    private static final String API_KEY = "c67c1cdec3b70b84a052b4d085c15eb1";
    private static final int MAX_API_CALLS = 1;
    private static final int UPDATE_INTERVAL = 1000 * 60 * 60; // 1 Hour

    private JsonArray configListArray;
    private String configUserToken;
    private Trello trello4jClient;
    private boolean isInitialized;

    protected Integer doInBackground() throws Exception
    {
        if (!isInitialized)
        {
            throw new Exception("Client has not been initialized.");
        }

        for (int nApiCalls = 0; ;nApiCalls++)
        {
            doWork();

            Thread.sleep(UPDATE_INTERVAL);
        }

    }

    private void doWork() throws Exception
    {
        for (int a = 0; a < configListArray.size(); a++)
        {
            List<Card> bs2 = null;
            System.out.println("[apicall] getCardsByList");
            String listId = configListArray.get(a).toString().replace('"', ' ').trim();

            try
            {
                bs2 = trello4jClient.getCardsByList(listId);
            }
            catch(Exception e)
            {
                System.out.println(e);
            }


            if (null == bs2)
            {
                System.out.println("board `" + listId + "` does not exist or is invalid." );
                throw new Exception();
            }

            if (0 == bs2.size())
            {
                System.out.println("board `" + listId + "` has no cards!" );
                throw new Exception();
            }

            for (Card b : bs2)
            {
                System.out.println("name:" + b.getName());
            }

            // Update the UI
            UIServer.update(a, bs2.get(0));
        }

    }

    public void initialize() throws Exception
    {
        if (isInitialized)
        {
            throw new Exception("Client has already been initialized");
        }

        boolean canContinue = readConfig();

        if (!canContinue)
        {
            throw new Exception("Unable to stat trello client.");
        }

        isInitialized = true;

        trello4jClient = new TrelloImpl(API_KEY, configUserToken.substring(1, configUserToken.length()-1));

        System.out.println("[apicall] init");
    }

    public void showMyBoards() throws Exception
    {
        if (!isInitialized)
        {
            throw new Exception("Client has not been initialized.");
        }

        List<Board> bs = trello4jClient.getBoardsByMember("my");

        if (0 == bs.size())
        {
            System.out.println("you dont have access to any boards?");
        }

        for (Board b : bs)
        {
            System.out.println("id:" +   b.getId());
            System.out.println("desc:" + b.getDesc());
            System.out.println("name:" + b.getName());
        }
    }

    /**
      * returns true if the configuration has the minimun necessary info
      *                   to continue execution of the client.
      **/
    private boolean parseConfig(String s)
    {
        JsonElement jElement = new JsonParser().parse(s);
        JsonObject  jObject = jElement.getAsJsonObject();

        // Search for usertoken
        configUserToken = jObject.get("usertoken").toString();
        if (null == configUserToken)
        {
            System.out.println("config.json is missing the usertoken");
            return false;
        }

        // Search for at lest one list
        configListArray = jObject.getAsJsonArray("lists");

        if (null == configListArray || configListArray.size() == 0)
        {
            System.out.println("config.json is missing at least 1 list");
            return false;
        }

        return true;
    }

    private boolean readConfig()
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("config.json"));
            String json = "";
            String s;

            while((s = br.readLine()) != null) json += s;

            return parseConfig(json);
        }
        catch(IOException ioe)
        {
            System.out.println("IO error. Does config.json exist?");
            return false;
        }

    }

}


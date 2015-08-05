import com.google.gson.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import java.awt.Desktop;
import java.net.URI;
import java.net.*;

import org.trello4j.*;
import org.trello4j.model.*;

/**
 * Anything that has to do with reading or writing to
 * the terminal.
 *
 *
 * */
public class TrelloCmd
{
    private static TrelloClient tClient;
    final static String url = "https://trello.com/1/authorize?key="+
                            TrelloClient.getKey()
                            +"&expiration=30days&name=trelloc&response_type=token&scope=read,write,account";

    private static void updateJsonToken()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SimpleSwingBrowser browser = new SimpleSwingBrowser();
                browser.setVisible(true);
                browser.loadURL(url);
            }
        });
    }

    public static void writeNewToken(String token) {
        tClient.writeTokenToConfig(token);
    }


    public static void main(String[] args)
    {
        tClient = TrelloClient.GetInstance();

        if (!tClient.configExist())
        {
            System.out.println("Config file does not exist");

            updateJsonToken();

            return;
        }

        try
        {
            if (args.length > 0 && args[0].equals("-c"))
            {
                tClient.initialize(args[1]);
            }
            else
            {
                tClient.initialize();
            }
        }
        catch (Exception e)
        {
            System.out.println(e);

            updateJsonToken();

            return;
        }

        if (args.length > 0 && args[0].equals("-?"))
        {
            String NEW_LINE = System.getProperty("line.separator");
            System.out.println(
                    "trello client 0.1" + NEW_LINE
                    + "-? this help " + NEW_LINE
                    + "-b show boards i have access to" + NEW_LINE );
            return;
        }

        if (args.length > 0 && args[0].equals("-b"))
        {
            try
            {
                showMyBoardsAndLists();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            return;
        }

        boolean configHasLists = tClient.loadLists();

        if (!configHasLists)
        {
            JOptionPane.showMessageDialog(null, "you dont have any lists specified in your config. run again with -b option", "No lists", JOptionPane.ERROR_MESSAGE);
            return;
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UIServer.createAndShowGUI();
            }
        });
    }

    private static void showMyBoardsAndLists() throws Exception
    {
        for (Board b : tClient.getMyBoards())
        {
            System.out.println("Board: " + b.getName() + " - " + b.getId());
            for (org.trello4j.model.List l : tClient.getListsFromBoard(b))
            {
                System.out.println("    List: " + l.getName() + " - " + l.getId());
            }
            System.out.println();
        }
    }
}

package Server;


import Common.Log;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MailServer {

    private final ArrayList<String> username ;
    private final Hashtable<String,File> mailbox ;
    private final Hashtable<String,File> unpulledFile;

    private final ObservableList<Log> logsList;
    private final ListProperty<Log> logs;

    /**
     *
     * @param username name of mailbox
     * @param mailbox contains files that contains mails of all mail address
     *
     */
    public MailServer(ArrayList<String> username,Hashtable<String,File> mailbox, Hashtable<String,File> unpulledFile){
        this.mailbox = mailbox;
        this.unpulledFile = unpulledFile;
        this.username = username;
        List<Log> list = Collections.synchronizedList(new ArrayList<Log>());
        logsList = FXCollections.observableArrayList(list);
        this.logs = new SimpleListProperty<>();
        logs.set(logsList);
    }


    /**
     * Server listening on a port and generate socket managed by thread
     *
     */


    public void startServer(){
        Executor pool = Executors.newFixedThreadPool(5);
        try {
            ServerSocket serverSocket = new ServerSocket(4445);
            while (true) {
                try {
                    System.out.println("[SERVER] waiting for connection...");
                    Socket client = serverSocket.accept();
                    System.out.println("[SERVER] connection succeed");
                    HandleClientRequest h = new HandleClientRequest(client,username,mailbox,unpulledFile,this);
                    Runnable r = (h::handleRequest);
                    pool.execute(r);
                } catch (IOException e) { e.printStackTrace();}
            }
        }
        catch (IOException e){ e.printStackTrace();}
    }

    public ListProperty<Log> logsProperty() {
        return this.logs;
    }


}

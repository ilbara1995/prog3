package Client;

import Common.Email;
import javafx.application.Platform;
import javafx.scene.control.Alert;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class Client {
    private Socket server = null;
    private final String id;//clientname
    private final MailClient m;


    final int MAX_ATTEMPTS = 5;

    /**
     * Create new client.
     * @param id type String, useful to print message.
     */

    public Client(String id,MailClient m){
        this.id = id;
        this.m = m;
    }

    /**
     * Up to 5 attempts to communicate withs server. after every failed attempts wait a second.
     * @param host address on which server is listening.
     * @param port port on which server is listening.
     * @param op   operation to execute.
     */
    public void communicate(String host, int port, String op, Email e){
        int attempts = 0;
        boolean success = false;

        while(attempts < MAX_ATTEMPTS && !success) {
            attempts += 1;
            System.out.println("[Client "+ this.id +"] Attempts nr. " + attempts);
            success = tryCommunication(host, port,e,op);
            if(success) continue;
            try { Thread.sleep(1000);
            } catch (InterruptedException ex) { ex.printStackTrace();}
        }
        if (!success){
            Platform.runLater(() ->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Server offline please wait...");
                alert.showAndWait();});
        }
    }

    /**
     * communication protocol
     * 1)client send his name to server
     * 2)client read unpulled value sent from server
     * 3)client send op to server
     */

    /**
     * try to communicate with server,on success return true otherwise false
     * @param host address on which server is listening.
     * @param port port on which server is listening.
     * @param e email
     * @param op operation type
     * @return true on success false otherwise
     */
    private boolean tryCommunication(String host, int port, Email e,String op) {
        try {
            connectToServer(host, port);
            PrintWriter pw = new PrintWriter(server.getOutputStream(),true);
            BufferedReader bf = new BufferedReader(new InputStreamReader(server.getInputStream()));
            //send clientname
            pw.println(this.id);
            System.out.println("[CLIENT "+this.id+"] sent client name  -> "+this.id);
            //get unpulled value
            String tmp = bf.readLine();
            //if unpulled val is 1 send new mail notification
            if(tmp.equals("1")) {
               System.out.println("get unpulled value from server = "+tmp);
               m.sendNewMailNotification();}
            System.out.println("[CLIENT "+this.id+"] server reply received -> "+tmp);
            //send operation
            pw.println(op);
            System.out.println("[CLIENT "+this.id+"] written op-> "+op);

            switch (op) {
                case "send":
                    if(!appendEmailtoServer(e)){
                        Platform.runLater(() ->{
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setContentText("email address not registered");
                            alert.showAndWait();});
                    }
                    else{
                        System.out.println("[CLIENT "+this.id+"] sent email to server, id ->"+MailClient.getIdMailSent());
                        //assign and increment local id for sent email
                        int id = MailClient.getIdMailSent();
                        MailClient.setIdMailSent(++id);
                        e.setId(id);
                        Platform.runLater(() -> { m.appendSentEmailOnFile(e);});
                    }
                    break;
                case "remove":
                    appendEmailtoServer(e);
                    break;
                case "refresh":
                    synchronized (m.inboxProperty().get()){
                    ArrayList<Email> l = readEmailList();
                        if(l != null){
                            Platform.runLater(() -> {
                                for(Email email: l){ //cycle array and do add if id doesn't already exist
                                    if(!m.containsId( m.inboxProperty().get(), email.getId())) m.inboxProperty().get().add(email);
                                }
                            }); System.out.println("[CLIENT] refreshed mail list");
                        }
                    }
                    break;
            } return true;
        } catch (IOException se) {
            se.printStackTrace();
            return false;
        } finally {
            if(server != null && (!server.isClosed())) {
                try {server.close();}
                catch (IOException ex) {ex.printStackTrace();}
            }
        }
    }

    private void connectToServer(String host, int port) throws IOException {
            this.server = new Socket(host, port);
            System.out.println("[Client.Client "+ this.id + "] Connected");
    }

    /**
     *
     * write email on socket
     * @return false if email address doesnt exist
     */
    private boolean appendEmailtoServer(Email e) {
        try {
            ObjectOutputStream o = new ObjectOutputStream(server.getOutputStream());
            o.flush();
            ObjectInputStream i = new ObjectInputStream(server.getInputStream());
            o.writeObject(e);
            o.flush();
            Boolean s = (Boolean) i.readObject();
            closeObjectConnections(o,i);
            return s;
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }

    }

    /**
     *
     * Close connection stream and socket
     *
     */
    private void closeObjectConnections(ObjectOutputStream o,ObjectInputStream i) {
        if (server != null) {
            try {
                o.close();
                i.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *
     * Close connection stream and socket
     *
     */

    private void closeObjectInputConnections(ObjectInputStream i) {
        if (server != null) {
            try {
                i.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     *
     * read email list from socket
     *
     */
    private ArrayList<Email> readEmailList(){
        ArrayList <Email> m = null;
        try {
            ObjectInputStream i = new ObjectInputStream(server.getInputStream());
            m = (ArrayList<Email>) i.readObject();
            closeObjectInputConnections(i);
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return m;
    }
}

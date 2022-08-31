package Server;


import Common.Email;
import Common.Log;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

public class HandleClientRequest {

    private final Socket client;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;


    private String clientName;
    private final ArrayList<String> username; //list containing name of existing emails
    private final Hashtable<String,File> mailbox; //containing key->email address value-> file
    private final Hashtable<String,File> unpulledFile;
    private final MailServer mailServer;


    public HandleClientRequest(Socket s,ArrayList<String> mailboxName,Hashtable<String,File> mailbox,Hashtable<String,File> unpulledFile,MailServer m) throws IOException {
        this.client = s;
        this.username = mailboxName;
        this.mailbox = mailbox;
        this.unpulledFile = unpulledFile;
        this.mailServer = m;
    }

    /**
     * getters
     */

    public String getClientName() { return clientName;}

    /**
     * methods
     */

    protected void handleRequest() {
        try {
            createAndAppendLog("connected with a new client ");
            String request = getNameAndRequestFromClient();
            Email e;
            switch (request) {
                case "send" -> { //write email on serv side file of receiver mail address
                    openObjectStream();//
                    e = (Email) objectIn.readObject();
                    Boolean val = checkMailDest(e); //true if email address exist false otherwise
                    objectOut.writeObject(val);//last message before closing object stream
                    objectOut.flush();
                    closObjectStream();
                    if (val) {
                        appendEmailOnFile(e);
                        //write 1 on unpulled file
                        for(String r: e.getReceivers()){ setUnpulledFile(r); }//set unpulled file to 1 for every receiver of this email
                    }
                }
                case "remove" -> {//remove email from file server side
                    openObjectStream();
                    e = (Email) objectIn.readObject();
                    objectOut.writeObject(true);
                    objectOut.flush();
                    closObjectStream();
                    removeEmailFromFile(e.getId());
                }
                case "refresh" -> { //check if there is new emails inside file server side and update local list of email
                    synchronized (mailbox.get(clientName)) {
                        ArrayList<Email> l;
                        File toRead = mailbox.get(clientName);
                        if (checkIfFileIsEmpty()) { l = new ArrayList<>();}
                        else l = readFromFile(toRead);
                        writeListOfEmailOnSocket(l);
                    }
                    createAndAppendLog("email refreshed in client " + getClientName());
                }
            }
        }
        catch(IOException | ClassNotFoundException ex) {ex.printStackTrace(); }
        finally {
            try {
                if(!client.isClosed())client.close();//close socket
            } catch (IOException e) { e.printStackTrace();}
        }
        createAndAppendLog("connection closed with client "+getClientName());
    }

    /**
     *
     * Open object stream
     *
     */
    private void openObjectStream() {
        try {
            this.objectIn = new ObjectInputStream(this.client.getInputStream());
            this.objectOut = new ObjectOutputStream(this.client.getOutputStream());
            this.objectOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Close object stream
     *
     */
    private void closObjectStream(){
        try {
            if(objectIn != null )objectIn.close();
            if(objectOut != null)objectOut.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Print all email field
     *
     */

    private void printEmail(Email e){
        System.out.println("sent from-> "+e.getSender()+";");
        System.out.println("object -> "+e.getSubject()+";");
        System.out.print("sent to ->");
        e.getReceivers().forEach((s) -> System.out.print(s+";"));
        System.out.println( "text ->\n"+e.getText()+";");
        System.out.println("id -> "+e.getId()+";");
        System.out.println("date -> "+e.getSendingDate()+";");
    }

    /**
     *
     * Print an ArrayList of email
     *
     */
    private void printListEmail(ArrayList<Email> l){
        System.out.println("********************************");
        for(Email em: l){printEmail(em);}
        System.out.println("********************************");
    }

    /**
     *
     * check if email address of receiver exists
     *
     */

    private boolean checkMailDest(Email e){
        for(String r: e.getReceivers()){
            if(!username.contains(r)){
                createAndAppendLog("Error Receiver doesn't exist "+getClientName());
                return false;
            }}
        return true;
    }
    /**
     *
     * Remove email from file using email unique id
     *
     */
    private void removeEmailFromFile(int id){
        synchronized (mailbox.get(clientName)) {
            try {
                System.out.println("removing id-> "+id);
                File f = mailbox.get(clientName);
                ArrayList<Email> l = readFromFile(f);
                //cerco index mail da rimuovere
                int index = findEmailIndex(l,id);
                System.out.println("found index email to remove-> "+index);
                //trovato lindex la rimuovo
                if(index!=-1){//rimuovo da file la email
                    l.remove(index);
                    writeOnFile(f,l);
                    createAndAppendLog("removed email from client "+getClientName());
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                createAndAppendLog("email not removed from client "+getClientName());
            }
        }
    }

    /**
     *
     * Write on file a new arraylist without the selected mail
     *
     */
    private void writeOnFile(File f,ArrayList<Email> l) throws IOException {
        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f));
        o.flush();
        o.writeObject(l);
        o.flush();
        o.close();
    }

    /**
     *
     * Find index of email with a given id
     *
     */
    private int findEmailIndex(ArrayList<Email> l,int id){
        int index = 0;
        for(Email e: l) {
            if(e.getId() == id) return index;
            else index++;
        }
        return -1;
    }


    /**
     *
     * write on file that contains emails
     *
     */
    private boolean appendEmailOnFile(Email e){
        for(String filename:e.getReceivers()) {
            synchronized (mailbox.get(filename)){
                try {
                    ArrayList<Email> emptyArray = new ArrayList<>();
                    File toWrite = mailbox.get(filename);
                    BufferedReader br = new BufferedReader(new FileReader(toWrite));
                    String tmp = br.readLine();
                    br.close();
                    if(tmp == null) {//empty file
                        writeOnFile(toWrite,e,emptyArray);//scrive arraylist su file
                        ArrayList<Email> read = readFromFile(toWrite);
                        printListEmail(read);}
                    else {// not empty file
                        ArrayList<Email> read = readFromFile(toWrite);//legge arraylist mail da file e lo salva in read
                        printListEmail(read);
                        writeOnFile(toWrite,e,read);}
                    createAndAppendLog("sent email from client "+getClientName());
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                    createAndAppendLog("failed sending email "+getClientName());
                    return false;}
            }
        }
        return true;
    }


/**
 *
 * Write on file a new arraylist with the email sent inside (need:file,email,Arraylist<Email>) return void
 *
 */
private void writeOnFile(File f,Email e,ArrayList<Email> l) throws IOException {
    e.setId(getIdFromFIle());//get free id and update write new free id on file
    l.add(e);
    ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f));
    o.flush();
    o.writeObject(l);
    o.flush();
    o.close();
}

/**
 *
 * Read Arraylist of email from a file (need: file) return Arraylist<Email>
 *
 */
private ArrayList<Email> readFromFile(File f) throws IOException, ClassNotFoundException {
    ArrayList<Email> l;
    ObjectInputStream i = new ObjectInputStream(new FileInputStream(f));
    l = (ArrayList<Email>) i.readObject();
    i.close();
    return l;
}
/**
 *
 * Check if file is empty return true if file is empty
 *
 */

private boolean checkIfFileIsEmpty() throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(mailbox.get(clientName)));
    String tmp = br.readLine();
    br.close();
    return tmp == null;
}

/**
 *
 * Write list of email on sokcet
 *
 */

private void writeListOfEmailOnSocket(ArrayList<Email> l) throws IOException {
    objectOut = new ObjectOutputStream(client.getOutputStream());
    objectOut.flush();
    objectOut.writeObject(l);
    objectOut.flush();
    if (objectOut != null) objectOut.close();
}

/**
 *
 * cmunication protocol between server and client
 * 1) read client name 2)answer to client with unpulled value 3) read request
 *
 */

    private String getNameAndRequestFromClient() throws IOException {
        BufferedReader bfr = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter pw = new PrintWriter(client.getOutputStream(),true);
        //get clientname
        clientName = bfr.readLine();
        System.out.println("[SERVER] Client.Client name riceived-> "+clientName);
        //send unpulled value
        int unpulled = getUnpulledFromFile();
        System.out.println("valore letto da file unpulled == "+unpulled);
        pw.println(Integer.toString(unpulled));//prova a cambiare con solo unpulled
        System.out.println("[SERVER] sent unpulled value to client");
        //get request from client
        String request = bfr.readLine();
        System.out.println("[SERVER] Client.Client request ->" + request);
        return request;
    }

/**
 *
 * leggo file che contiene id incremento id e lo scrivo su file
 *
 */

private int getIdFromFIle() { //legge id da file lo incrementa e lo restituisce
    int id = 0;
    synchronized (mailbox.get("id")){
        try {
            FileInputStream fi;
            FileOutputStream fo;
            File f = mailbox.get("id");
            if (f.length() == 0) {
                fo = new FileOutputStream(f);
                fo.write(id+1);
                System.out.println("written id-> "+(id+1));
            } else {
                fi = new FileInputStream(f);
                id = fi.read();
                System.out.println("read id-> "+id);
                fi.close();
                fo = new FileOutputStream(f);
                fo.write(id+1);
                System.out.println("written id->"+(id+1));
            }
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    return id;
}

/**
 * get a int value from a file if it's 1 means that there are unpulled emails (useful for notifications)
 * if found 0 on file return 0 ,otherwise if found 1 return 1 but write 0 on file
 */
private int getUnpulledFromFile(){
    int val = 0;
    synchronized(unpulledFile.get(clientName)) {
        try {
            FileInputStream fi;
            FileOutputStream fo;
            File f = unpulledFile.get(clientName);
            fi = new FileInputStream(f);
            val = fi.read();
            System.out.println("read 1 = "+ val);
            fi.close();
            if(val == 1){
                fo = new FileOutputStream(f);
                fo.write(0);
                fo.flush();
                System.out.println("written id->"+(0));
                fo.close();}
        } catch (IOException e) { e.printStackTrace(); }
    }
    return val;
}
/**
 * Set Unpulled file to 1 indicating that there are unpulled emails fro client
 * (useful to understand when need to send new mail notification to client)
 */
private void setUnpulledFile(String client){
    synchronized(unpulledFile.get(client)) {
        try {
            File f = unpulledFile.get(client);
            FileOutputStream fo;
            fo = new FileOutputStream(f);
            fo.write(1);
            fo.flush();
            System.out.println("written id-> "+1);
            fo.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}

/**
 * Create a new log and add it ot observable list of table view
 */

protected void createAndAppendLog(String message){
    Log log = new Log(message);
    mailServer.logsProperty().get().add(log);
}



}

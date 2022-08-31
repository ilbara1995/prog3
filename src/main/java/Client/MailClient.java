package Client;


import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Common.Email;


import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Pattern;



public class MailClient {


    /** this class contains whole mail account of a user **/
    private static int idMailSent = 0;
    private final File sentEmailFile;
    private final ArrayList<Email> sentList;
    private final StringProperty emailAddress;
    private final ListProperty<Email> inbox;
    private final ListProperty<Email> sent;

    private final ObservableList<Email> inboxContent;
    private final ObservableList<Email> sentEmail;

    private final Executor pool ; //threadpool to manage client request like send remove
    private final ScheduledExecutorService updateMail;//trhead executed every tot seconds to read new emails
    private final ClientController controller;

    /** constructor **/

    public MailClient(String username,ClientController c){
        this.controller = c;
        this.sentEmailFile = new File("src/main/java/Client/sentEmail/"+username+".txt");
        this.emailAddress = new SimpleStringProperty(username);
        this.inboxContent = FXCollections.observableList(new ArrayList<>());
        this.sentEmail = FXCollections.observableList(sentList = new ArrayList<>());
        this.inbox = new SimpleListProperty<>();
        this.sent = new SimpleListProperty<>();
        this.inbox.set(inboxContent);
        this.sent.set(sentEmail);
        this.pool = Executors.newFixedThreadPool(3);
        this.updateMail = Executors.newScheduledThreadPool(1);
    }
    /** getters and setters **/

    public static int getIdMailSent() { return idMailSent;}

    public static void setIdMailSent(int idMailSent) { MailClient.idMailSent = idMailSent;}

    /** methods **/

    /**
     * @return      list of email
     *
     */
    public ListProperty<Email> inboxProperty() {
        return inbox;
    }

    /**
     *
     * return email sent
     */

    public ListProperty<Email>  sentProperty() { return sent; }

    /**
     *
     * @return   email address
     *
     */
    public StringProperty emailAddressProperty() {
        return emailAddress;
    }



    /**
     *
     * controlla se email è presente nella list se presente ritorno true se assente false
     *
     */


    public boolean containsId(ObservableList<Email> l ,int id){
        System.out.println("cerco id-> "+id);
        for(Email e: l){if(e.getId() == id)return true;}
        return false;
    }
    /**
     *
     *  delete from received mail selected email
     *
     */
    public void deleteEmail(Email e) {
        synchronized (inboxContent) { inboxContent.remove(e);}//remove locally
        Client client = new Client(this.emailAddress.get(), this);
        Runnable r = (()->client.communicate("127.0.0.1", 4445,"remove",e));//remove server side
        pool.execute(r);
    }

    /**
     *
     * delete from received mail selected email
     *
     */
    public void deleteSentEmail(Email e){
        synchronized (sentEmail){sent.remove(e);}
        writeOnEmailSentFile(sentEmailFile);
        System.out.println("Success\n");
    }


    /**
     *
     * cyclic read list of email from file
     *
     */
    public void refreshEmailList(){
        Client client = new Client(this.emailAddress.get(), this);
        System.out.println("[CLIENT] email address ->"+this.emailAddress.get());
        Runnable r = (()->client.communicate("127.0.0.1", 4445,"refresh",null));
        System.out.println("[CLIENT "+emailAddressProperty().get()+"] creating new task to referesh received emails list");
        updateMail.scheduleAtFixedRate(r,0,10, TimeUnit.SECONDS);
    }

    /**
     * Validate email before sending
     */

    public boolean validateEmail(Email e){
        String regex = "[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
        for (int i = 0; i < e.getReceivers().size(); i++) {
            String input = e.getReceivers().get(i);
            if (!Pattern.matches(regex, input)) return false;
        }
        return Pattern.matches(regex, e.getSender());
    }

    /**
     * Create a new task and pass it ot a thread pool that will send email to server and wait his response
     */
    public void send(Email e){
        Client client = new Client(this.emailAddress.get(), this);
        System.out.println("[CLIENT] email address ->"+this.emailAddress.get());
        Runnable r = (()->client.communicate("127.0.0.1", 4445,"send",e));
        System.out.println("[CLIENT "+emailAddressProperty().get()+"] Creating new thread to send email");
        pool.execute(r);
    }

    /**
     *
     * Read Arraylist of email from a file (need: file) return Arraylist<Email>
     *
     */
    public ArrayList<Email> readFromFile() throws IOException, ClassNotFoundException {
        sentEmailFile.createNewFile();
        ArrayList<Email> l;
        ObjectInputStream i = new ObjectInputStream(new FileInputStream(sentEmailFile));
        l = (ArrayList<Email>) i.readObject();
        i.close();
        return l;
    }

    /**
     * init Arraylist of sent mail
     */

    public void initSentMail(){
        try {
            sentEmailFile.createNewFile();
            if(!checkIfFileIsEmpty()) {//if file is empty there is nothing to read
                ArrayList<Email> l = this.readFromFile();
                if (l != null) {
                    for (Email e : l) { this.sentProperty().get().add(e);}
                    setIdMailSent(l.size());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**add sent email to list of sent email and append it to on file **/

    public void appendSentEmailOnFile(Email e){
        synchronized (sentEmail){this.sentProperty().get().add(e);}// add to arraylist
        writeOnEmailSentFile(sentEmailFile);// write arraylist of email on file
    }

    /**
     *
     * Write on file a new arraylist without the selected mail
     *
     */
    private void writeOnEmailSentFile(File f){
        ObjectOutputStream o;
        try {
            o = new ObjectOutputStream(new FileOutputStream(f));
            o.flush();
            synchronized (sentEmail){o.writeObject(sentList);}
            o.flush();
            o.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Check if file is empty return true if file is empty
     *
     */

    private boolean checkIfFileIsEmpty() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(sentEmailFile));
        String tmp = br.readLine();
        br.close();
        return tmp == null;
    }

    /**
     * notifiy new mail incoming, call a controller method that create new mail notification
     */

    public void sendNewMailNotification(){ Platform.runLater(controller::notifyNewEmail); }




}

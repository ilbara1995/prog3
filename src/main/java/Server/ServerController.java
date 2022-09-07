package Server;

import Common.Log;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class ServerController {

    @FXML
    private TableView<Log> tableLog;
    @FXML
    private TableColumn<Log,String> messageCol;
    @FXML
    private TableColumn<Log,Date> timeCol;


    private MailServer model;

    @FXML
    public void initialize(){

        if (this.model != null) throw new IllegalStateException("Model can only be initialized once");

        ArrayList<String> username = new ArrayList<>();
        Hashtable<String, File> mailbox = new Hashtable<>();
        Hashtable<String,File> unpulledFile = new Hashtable<>();


        File barluc = null;
        File barlucUnpulled;
        File beator = null;
        File beatorUnpulled;
        File rosator = null;
        File rosatorUnpulled;
        File id = null;



        barluc = new File("src/main/resources/receivedEmails/barluc@yahoo.it.txt");
        barlucUnpulled = new File("src/main/resources/receivedEmails/barlucUnpulled.txt");
        beator = new File("src/main/resources/receivedEmails/beator@yahoo.it.txt");
        beatorUnpulled = new File("src/main/resources/receivedEmails/beatorUnpulled.txt");
        rosator = new File("src/main/resources/receivedEmails/rosator@yahoo.it.txt");
        rosatorUnpulled = new File("src/main/resources/receivedEmails/rosatorUnpulled.txt");
        id = new File("src/main/resources/receivedEmails/id.txt");



        mailbox.put("barluc@yahoo.it",barluc);
        unpulledFile.put("barluc@yahoo.it",barlucUnpulled);
        mailbox.put("beator@yahoo.it",beator);
        unpulledFile.put("beator@yahoo.it",beatorUnpulled);
        mailbox.put("rosator@yahoo.it",rosator);
        unpulledFile.put("rosator@yahoo.it",rosatorUnpulled);
        mailbox.put("id",id);
        username.add("barluc@yahoo.it");
        username.add("beator@yahoo.it");
        username.add("rosator@yahoo.it");

        //init file unpulled
        for(String s:username){
            try {
                if(unpulledFile.get(s).createNewFile() || checkIfFileIsEmpty(unpulledFile.get(s)))initUnpulledFile(unpulledFile.get(s));//doesn't init file if file exists but is empty
            } catch (IOException e) { e.printStackTrace();}
        }

        model = new MailServer(username,mailbox,unpulledFile);

        messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        tableLog.setItems(model.logsProperty().get());
        tableLog.itemsProperty().bind(model.logsProperty());

        Runnable r = (() -> model.startServer());
        Thread serverThread = new Thread(r);
        serverThread.start();
    }

    /**
     *
     * @param f that need to be initialized with value 0 only (only if it has been created for the first time)
     */
    private void initUnpulledFile(File f){
        try {
            FileOutputStream fo;
            fo = new FileOutputStream(f);
            fo.write(0);
            fo.flush();
            System.out.println("written val -> " + (0));
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Check if file is empty return true if file is empty
     *
     */

    private boolean checkIfFileIsEmpty(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String tmp = br.readLine();
        br.close();
        return tmp == null;
    }
}

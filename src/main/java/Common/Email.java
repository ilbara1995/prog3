package Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Email implements Serializable {
    private int id;
    private final String sender;
    private final List<String> receivers;
    private final String subject;
    private final String text;
    private final Date sendingDate;


    /**
     * Class constructor.
     *
     * @param sender        sender email
     * @param receivers     emails of receivers
     * @param subject       email object
     * @param text          email text
     */

    public Email(String sender, List<String> receivers, String subject, String text){
        this.id = 0;//default values
        this.sender = sender;
        this.receivers = new ArrayList<>(receivers);
        this.subject = subject;
        this.text = text;
        this.sendingDate = new Date();
    }

    /**
     * methods
     */

    public String getSender() {
        return sender;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public Date getSendingDate() { return sendingDate; }

    public int getId() { return id; }

    public void setId(int i){this.id = i;}

    @Override
    public String toString() {
        return String.join(" - ", List.of(this.sender,this.subject,this.sendingDate.toString()));
    }


}

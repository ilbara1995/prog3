package Client.SubController;

import Client.ClientController;
import Client.MailClient;
import Common.Email;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReplyController {

    /**
     * labels
     */

    @FXML
    private Label labelFromRight;
    @FXML
    private Label labelObjectRight;
    @FXML
    private Label labelFieldToRight;

    /**
     * text filed and area
     */

    @FXML
    private TextArea replyMailContent;

    /**
     * fields
     */

    private MailClient model;
    private ClientController mainController;

    /**
     * getters
     */

    public Label getLabelFromRight() {return labelFromRight;}

    public Label getLabelObjectRight() {return labelObjectRight;}

    public Label getLabelFieldToRight() { return labelFieldToRight;}

    public TextArea getReplyMailContent() {return replyMailContent;}

    public void initModel(MailClient model,ClientController mainController) {
        if (this.model != null)  throw new IllegalStateException("Model can only be initialized once");
        this.model = model ;
        this.mainController = mainController;
    }


    /**
     * methods
     */

    @FXML
    protected void onSendButtonClick(){
        String[] receivers = labelFieldToRight.getText().trim().split(";");//insert receivers divided by ";"
        for(String s: receivers){System.out.println("primo elem "+ s +" /n");}
        List<String> l = new ArrayList<>(Arrays.asList(receivers));
        for(String dest: receivers){System.out.println("dest -> "+dest+"/n");}
        Email toSend = new Email(labelFromRight.getText(),l,labelObjectRight.getText().trim(),replyMailContent.getText().trim());
        mainController.sendEmail(toSend);
    }

}

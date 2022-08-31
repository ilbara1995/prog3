package Client.SubController;

import Client.ClientController;
import Client.MailClient;
import Common.Email;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewMailController {


    /**
     * labels
     */

    @FXML
    private Label labelFromRight;

    /**
     * text filed and area
     */


    @FXML
    private TextField textFieldObjectRight;

    @FXML
    private TextField textFieldToRight;

    @FXML
    private TextArea newMailContent;

    /**
     * fields
     */

    private MailClient model;
    private ClientController mainController;
    /**
     * getters and setters
     */


    public Label getLabelFromRight() { return labelFromRight;}
    public TextField getTextFieldObjectRight() { return textFieldObjectRight; }
    public TextField getTextFieldToRight() { return textFieldToRight; }
    public TextArea getNewMailContent() { return newMailContent;}


    /**
     * pass model to the sub-controller
     *
     */
    public void initModel(MailClient model,ClientController mainController) {
        if (this.model != null)  throw new IllegalStateException("Model can only be initialized once");
        this.model = model ;
        this.mainController = mainController;
    }


    @FXML
    protected void onSendButtonClick(){
        String[] receivers = textFieldToRight.getText().trim().split(";");//insert receivers divided by ";"
        List<String> l = new ArrayList<>(Arrays.asList(receivers));
        Email toSend = new Email(labelFromRight.getText(),l,textFieldObjectRight.getText().trim(),newMailContent.getText().trim());
        mainController.sendEmail(toSend);
    }


}

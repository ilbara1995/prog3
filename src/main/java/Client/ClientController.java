package Client;

import Client.SubController.FwdController;
import Client.SubController.NewMailController;
import Client.SubController.ReplyController;
import Common.Email;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientController {

    private boolean sent = false;
    private boolean received = false;
    /**
     * view element
     */

    @FXML
    private SplitPane splitPane;
    @FXML
    private BorderPane rightBorderPane;

    private BorderPane newMailBorderPane;

    private BorderPane fwdBorderPane;

    private BorderPane replyBorderPane;
    /**
     * Text area/field and labels
     */

    @FXML
    private Label lblUsername;

    @FXML
    private TextField txtFrom;

    @FXML
    private TextField txtTo;

    @FXML
    private TextField txtSubject;

    @FXML
    private TextArea txtEmailContent;
    @FXML
    private ListView<Email> lstEmail;

    /**
     * sub-controllers
     */


    private FwdController fwdC;

    private NewMailController nMC;

    private ReplyController rplyC;

    private MailClient model;


    private Email selectedEmail;
    private Email emptyEmail;


    @FXML
    public void initialize(){
        if (this.model != null) throw new IllegalStateException("Model can only be initialized once");
        String name = "";
        String chose;
        Scanner in = new Scanner(System.in);
        System.out.println("Scegli client mail: inserisci 1 o 2 o 3");
        chose = in.nextLine();
        boolean exit = true;
        while(exit) {
            switch (chose) {
                case "1" -> {
                    name = "barluc@yahoo.it";
                    exit = false;
                }
                case "2" -> {
                    name = "beator@yahoo.it";
                    exit = false;
                }
                case "3" -> {
                    name = "rosator@yahoo.it";
                    exit = false;
                }
            }
        }
        model = new MailClient(name,this);

        selectedEmail = null;

        lstEmail.setOnMouseClicked(this::showSelectedEmail);

        lstEmail.setVisible(false);

        lblUsername.textProperty().bind(model.emailAddressProperty());

        emptyEmail = new Email("", List.of(""), "", "");

        updateDetailView(emptyEmail);

        model.initSentMail();

        model.refreshEmailList();

        /**
         * create sub-controllers and sub view and pass them model and main controller
         */
        createFwdViewAndController("fwd.fxml");
        fwdC.initModel(model,this);
        createNewMailViewAndController("newMail.fxml");
        nMC.initModel(model,this);
        createReplyViewAndController("reply.fxml");
        rplyC.initModel(model,this);
    }


    /**
     * create sub view and and sub controllers
     */

    protected void createNewMailViewAndController(String filename){
        try {
            URL borderPaneNewMail = getClass().getResource("/fxml/"+filename);
            FXMLLoader fxmlLoader = new FXMLLoader(borderPaneNewMail);
            this.newMailBorderPane = fxmlLoader.load();
            this.nMC = fxmlLoader.getController();
        } catch (IOException e){e.printStackTrace();}
    }

    protected void createFwdViewAndController(String filename){
        try {
            URL borderPaneNewMail = getClass().getResource("/fxml/"+filename);
            FXMLLoader fxmlLoader = new FXMLLoader(borderPaneNewMail);
            this.fwdBorderPane = fxmlLoader.load();
            this.fwdC = fxmlLoader.getController();
        } catch (IOException e){e.printStackTrace();}
    }

    protected void createReplyViewAndController(String filename){
        try {
            URL borderPaneNewMail = getClass().getResource("/fxml/"+filename);
            FXMLLoader fxmlLoader = new FXMLLoader(borderPaneNewMail);
            this.replyBorderPane = fxmlLoader.load();
            this.rplyC = fxmlLoader.getController();
        } catch (IOException e){e.printStackTrace();}
    }

    /**
     * change right view with a new border pane
     */

    protected void changeRightBorderPane(BorderPane subview){
        splitPane.getItems().remove(1);
        splitPane.getItems().add(subview);
    }

    /**
     * Forward the selected email
     *  /**
     *          * modifiche:
     *          * 1)sender -> sono io
     *          * 2)receivers -> vuoto
     *          * 3)object -> "Fwd" + selectedEmail.getSubject();
     *          * 4)text -> "-----Forwarded message-----\n" + selectedEmail.getText();
     *          * optionally before text -> "From: "+selectedEmail.getSender()+"\n";
     *          *                                  "Date: " + selectedEmail.getDate();
     *          *                                  "Subject" + selectedEmail.getSubject();
     *          *                                  "To: " + model.getAddressProperty().get();
     *          */
    @FXML
    protected void onForwardButtonClick(){
        if(selectedEmail != null){
            String text = "\n\n"+"-----Forwarded message-----\n"+selectedEmail.getText()+"\n"+"Da; "+
                    selectedEmail.getSender()+"\n"+"Date: "+selectedEmail.getSendingDate()+"\n"+
                "Subject: "+selectedEmail.getSubject()+"\n"+"To: "+ String.join("; ", selectedEmail.getReceivers());
            updateFwdView(new Email(model.emailAddressProperty().get(),List.of(""),selectedEmail.getSubject(), text));
            changeRightBorderPane(fwdBorderPane);
            lstEmail.setVisible(false);
        }
    }

    /**
     *  Answer to the selected email to all senders
     */
    @FXML
    protected void onReplyAllButtonClick(){
        if(selectedEmail != null && selectedEmail.getSender().length() > 1) {
            ArrayList<String> receivers = new ArrayList<>();
            receivers.add(selectedEmail.getSender());
            receivers.addAll(selectedEmail.getReceivers());
            receivers.remove(model.emailAddressProperty().get());
            updateRplyView(new Email(model.emailAddressProperty().get(), receivers, selectedEmail.getSubject(), ""));
            changeRightBorderPane(replyBorderPane);
            lstEmail.setVisible(false);
        }
    }

    /**
     * change list view by clicking incoming mail label
     */
    @FXML
    protected void onIncomingMailLabelClick(){
        if(sent)sent = false;
        received = true;
        lstEmail.setVisible(true);
        lstEmail.itemsProperty().unbind();
        synchronized (model.inboxProperty().get()){
            lstEmail.itemsProperty().set(model.inboxProperty().get());
            lstEmail.itemsProperty().bind(model.inboxProperty());}
        changeRightBorderPane(rightBorderPane);
        updateDetailView(emptyEmail);
    }

    /**
     * change list view by clicking sent mail label
     */
    @FXML
    protected void onSentMailLabelClick(){
        if(received)received = false;
        sent = true;
        lstEmail.setVisible(true);
        lstEmail.itemsProperty().unbind();
        synchronized (model.sentProperty().get()){
            lstEmail.itemsProperty().set(model.sentProperty().get());
            lstEmail.itemsProperty().bind(model.sentProperty());}
        changeRightBorderPane(rightBorderPane);
        updateDetailView(emptyEmail);
    }
    /**
     * Answer to the selected mail and change right view
     */
    @FXML
    protected void onReplyButtonClick(){
        if(selectedEmail != null){
            updateRplyView(new Email(model.emailAddressProperty().get(),List.of(selectedEmail.getSender()),selectedEmail.getSubject(),""));
            changeRightBorderPane(replyBorderPane);
            lstEmail.setVisible(false);
        }
    }

    /**
     * Change right view with new mail view
     * */
    @FXML
    protected void onNewButtonClick(){
            changeRightBorderPane(newMailBorderPane);
            updateNewMAilView(new Email(model.emailAddressProperty().get(), List.of(""),"",""));
            lstEmail.setVisible(false);
    }

    /**
     * Delete selected email
     */
    @FXML
    protected void onDeleteButtonClick() {
        if(selectedEmail != null) {
            if(received)model.deleteEmail(selectedEmail);
            else model.deleteSentEmail(selectedEmail);
            updateDetailView(emptyEmail);
        }
    }

    /**
     * Show selected email inside view
     */
    @FXML
    protected void showSelectedEmail(MouseEvent mouseEvent) {
        Email email = lstEmail.getSelectionModel().getSelectedItem();
        selectedEmail = email;
        updateDetailView(email);
    }


    /**
     * update view with selected email
     */
    protected void updateDetailView(Email email) {
        if(email != null) {
            txtFrom.setText(email.getSender());
            txtTo.setText(String.join(";", email.getReceivers()));
            txtSubject.setText(email.getSubject());
            txtEmailContent.setText(email.getText());
        }
    }

    /**
     * Update new mail view with selected email
     */

    protected void updateNewMAilView(Email e) {
        if(e != null) {
            nMC.getLabelFromRight().setText(e.getSender());
            nMC.getTextFieldToRight().setText(String.join(";", e.getReceivers()));
            nMC.getTextFieldObjectRight().setText(e.getSubject());
            nMC.getNewMailContent().setText(e.getText());
        }
    }

    /**
     * Update fwd mail view
     */
    protected void updateFwdView(Email e){
        if(e != null) {
            fwdC.getLabelFromRight().setText(e.getSender());
            fwdC.getLabelObjectRight().setText(e.getSubject());
            fwdC.getFwdMailContent().setText(e.getText());
        }
    }

    /**
     *  Update reply mail view
     */
    protected void updateRplyView(Email e){
        if(e != null) {
            rplyC.getLabelFromRight().setText(e.getSender());
            rplyC.getLabelFieldToRight().setText(String.join(";", e.getReceivers()));
            rplyC.getLabelObjectRight().setText(e.getSubject());
            rplyC.getReplyMailContent().setText(e.getText());
        }
    }

    public void sendEmail(Email toSend) {
        if (model.validateEmail(toSend)) model.send(toSend);
        else {
            Alert  alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Invalid email pattern");
            alert.showAndWait();
            System.out.println("[CLIENT-> " + model.emailAddressProperty().get() + "] invio email fallito formato destinatario scorretto");
        }
    }

    /**
     *  Generate a notification when a new icnoming email is detected
     */

    public void notifyNewEmail(){
        Notifications.create()
                .text("Nuove mail in arrivo")
                .title("Notifica")
                .hideAfter(Duration.seconds(3))
                .owner(lblUsername.getScene().getWindow())
                .showInformation();
    }

}
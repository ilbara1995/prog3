module demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires controlsfx;

    exports Client to javafx.graphics;
    exports Server to javafx.graphics;
    exports Common to javafx.graphics;

    opens Client to javafx.fxml;
    opens Client.SubController to javafx.fxml ;
    opens Server to javafx.fxml;
    opens fxml to javafx.fxml;
    opens Common to javafx.base;

}
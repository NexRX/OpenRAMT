package Controller;

import Controller.Library.SideButton;
import Controller.Library.WizardPane;
import Model.AppPermission;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;

/**
 * The side bar controller. Handles all tasks relating to the sidebar.
 */
public class SideBarController extends VBox {

    /**
     * Key holds reference name for the button stored in here.
     */
    private final HashMap<Integer, SideButton> sideButtons = new HashMap<>();

    WizardPane wizard;

    /**
     * Constructs the VBox and loads its FXML file.
     */
    public SideBarController(WizardPane wizard) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/SideBar.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.wizard = wizard;

        SideButton sBtnDefault = new SideButton("Welcome", AppPermission.NONE);
        sBtnDefault.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            System.out.println("Changing Pane to Default");
            this.wizard.changePane(0);
        });
        sideButtons.put(0, sBtnDefault); // Make a constructor that can add a different default button.

        getChildren().add(sBtnDefault); // Add our default button.

    }

    /**
     * Adds a button to the side bar. The button must be passed with an index (integer value) that is associated with a
     * index to the desired node in the wizard. This is because the index should be the same as it is used internally to
     * display the pane inside the Wizard.
     * @param sBtn The button which should be added to the side bar.
     * @param index The index for the button internally that corresponds to the index of a pane inside the wizard.
     * @return True if added successfully. False if the index is already used including 0 which is the default buttons
     * index.
     */
    public boolean addButton(SideButton sBtn, Integer index) {
        if (sideButtons.get(index) != null || index == 0) {return false;} // If key is default or taken, false.

        getChildren().add(index, sBtn);

        sBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            System.out.println("Changing Pane to " + index.toString());
            this.wizard.changePane(index);
        });

        return true;
    }

    /**
     * Similar to addButton, this method sets an already existing button with a new button. Effectively replacing the old
     * button with a new one if needed. Note if a button doesn't exist this method does nothing but return false.
     * @param sBtn The button which should be set to the side bar.
     * @param index The index for the button internally that corresponds to the index of a pane inside the wizard.
     * @return True if set successfully. False if the index is already used including 0 which is the default buttons
     * index.
     */
    public boolean setButton(SideButton sBtn, Integer index) {
        if (sideButtons.get(index) == null || index == 0) {return false;} // If key is default or taken, false.

        getChildren().add(index, sBtn);

        sBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            System.out.println("Changing Pane to " + index.toString());
            this.wizard.changePane(index);
        });

        return true;
    }

    /**
     * Removes a button previously added button using the addButton method from the stash.
     * @param index The index for the button internally that corresponds to the index of a pane inside the wizard.
     * @return True if added successfully. False if the index is already used including 0 which is the default buttons
     * index.
     */
    public boolean removeButton(Integer index) {
        if (sideButtons.get(index) == null || index == 0) {return false;} // If key is default or taken, false.

        sideButtons.remove(index);
        getChildren().remove(index);

        return true;
    }

    private boolean changeWizardPane(int index) {
        try {
            wizard.changePane(index);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }


}



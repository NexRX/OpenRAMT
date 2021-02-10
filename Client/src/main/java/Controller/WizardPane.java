package Controller;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import java.util.HashMap;
import java.util.List;

public class WizardPane extends Pane { // Essentially will allow you to only have one node shown at a time and is enforced by this class. automatically disabling other panes

    protected final ReadOnlyIntegerWrapper activeChild = new ReadOnlyIntegerWrapper(0);


    public WizardPane() {
        super();

        // handle future nodes
        applyEventHandlers();
    }

    public WizardPane(Node... children) {
        super();

        // Disable all currently nodes
        for (Node node : children) {
            getChildren().add(node);
            node.managedProperty().bind(node.visibleProperty());
            node.setVisible(false);
        }

        // handle future nodes
        applyEventHandlers();

        // Set default visible to first added.
        System.out.println(getChildren().toString());
        getChildren().get(0).setVisible(true);
    }

    protected void applyEventHandlers() {
        getChildren().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Node addedNode : c.getAddedSubList()) {
                        addedNode.managedProperty().bind(addedNode.visibleProperty());
                        addedNode.setVisible(false);
                    }
                }
            }
        });
    }

    public ReadOnlyIntegerProperty getActiveChildProperty() { return activeChild.getReadOnlyProperty(); }
    public int activeChild() {return activeChild.get();}

    public void changePane(int index) {
        // hide current
        getChildren().get(activeChild()).setVisible(false);

        // show index
        activeChild.set(index);
        System.out.println(getChildren().toString());
        getChildren().get(index).setVisible(true);
    }

}

package Controller.Library;


import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/**
 * Manages children nodes and others methods to display them in a way which acts as a UI Wizard.
 */
public class WizardPane extends AnchorPane {

    protected final ReadOnlyIntegerWrapper activeChild = new ReadOnlyIntegerWrapper(0);

    /**
     * Creates a Wizard Pane which will have no children. No nodes will be shown and will have to be added later.
     */
    public WizardPane() {
        super();

        // handle future nodes
        applyEventHandlers();
    }

    /**
     * Creates a wizard pane which which contains any numbers of given nodes and adds them as children. The first given
     * child or only given child will be set as visible. The order of children corresponds to their index as children
     * within the children property.
     * @param children The nodes to be displayed within the pane.
     */
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
        activeChild.set(0);
        getChildren().get(0).setVisible(true);
    }

    /**
     * Applies the event handlers. These event handlers are currently:
     * -Binds managedProperty to visibleProperty for all newly added children.
     */
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

    /**
     * Changes the currently displayed pane by setting the currently registered active pane to not visible and then
     * makes the given index the active node in the children property.
     *
     * WARNING: This is only guaranteed to deliver the expected behaviour if this method is solely used to change the
     * active pane. Example, set a pane to visible by manually will not update the variable that tracks teh active pane.
     * You can use noPane() to display none and reflect that internally which would fix any issue like the examples.
     * @param index
     */
    public void changePane(int index) {
        // hide current
        getChildren().get(activeChild()).setVisible(false);

        // show index
        activeChild.set(index);
        System.out.println(index);
        getChildren().get(index).setVisible(true);
    }

    /**
     * Iterates through the children to guaranty that no pane is visible. This method is there for somewhat a hard reset
     * of the active pane.
     */
    public void resetPane() {
        getChildren().forEach(node -> node.setVisible(false));
    }

}

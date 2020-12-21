package Controller.Library;

import com.jfoenix.controls.JFXButton;

import java.security.Permissions;

public class SideButton extends JFXButton {
    private final Permissions permission;

    public SideButton(Permissions permission) {this.permission = permission;}

    public Permissions getPermission() {return permission;}
}

package Controller.Library;

import Model.General.AppPermission;

public interface AppPermissionRestrictable {
    AppPermission getAppPermission();
    
    boolean authorise();
}

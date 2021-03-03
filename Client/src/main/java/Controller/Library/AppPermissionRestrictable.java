package Controller.Library;

import Model.AppPermission;

public interface AppPermissionRestrictable {
    AppPermission getAppPermission();
    
    boolean authorise();
}

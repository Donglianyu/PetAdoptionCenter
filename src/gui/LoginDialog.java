package gui;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * LoginDialog has been removed from active use in favor of an embedded login panel
 * inside MainFrame. This lightweight stub remains only to avoid build errors if
 * any external code still references the class. Instantiating this stub will
 * throw an exception to make its deprecated status explicit.
 */
@Deprecated
public class LoginDialog extends JDialog {
    public LoginDialog(JFrame parent, List<?> userList, Map<String, ?> adopterMap,
                       Object petManager, Object appManager) {
        super(parent, "Login - removed", true);
        throw new UnsupportedOperationException("LoginDialog is deprecated. Use the embedded login in MainFrame instead.");
    }
}


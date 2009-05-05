//
// $Id$

package client.account;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

import com.threerings.gwt.util.CookieUtil;

import com.threerings.msoy.web.gwt.Args;
import com.threerings.msoy.web.gwt.WebCreds;
import com.threerings.msoy.web.gwt.WebUserService;
import com.threerings.msoy.web.gwt.Pages;
import com.threerings.msoy.web.gwt.WebUserServiceAsync;

import client.shell.CShell;
import client.shell.Page;
import client.ui.MsoyUI;
import client.util.Link;
import client.util.InfoCallback;
import client.util.ServiceUtil;

/**
 * Displays account information.
 */
public class AccountPage extends Page
{
    @Override // from Page
    public void onHistoryChanged (Args args)
    {
        String action = args.get(0, "");
        if (action.equals("deleted")) {
            setContent(null, MsoyUI.createLabel(_msgs.confirmDeleteDone(), "infoLabel"));
            CShell.frame.closeClient();
            if (!CShell.isGuest()) {
                CShell.frame.logoff();
            }
            // redirect away from this page after 2 seconds because if we leave them here and they
            // try to log in to a different account, it will result in confusion
            new Timer() {
                public void run () {
                    Link.go(Pages.LANDING, "");
                }
            }.schedule(2000);

        } else if (action.equals("create") && (CShell.isGuest() || CShell.isPermaguest())) {
            setContent(_msgs.createTitle(), new CreateAccountPanel(CShell.isPermaguest(), false));

        } else if (action.equals("reg") && !CShell.isMember()) {
            if (args.get(1, "").equals("v")) {
                // TODO: validate the email address then somehow get a session token - I'm thinking
                // this will take the form of another LogonPanel subclass that requests validation
                // and sets Session.justCreated so that the profile configuration page gets shown

            } else {
                setContent(_msgs.createTitle(), new CreateAccountPanel(false, true));
            }

        } else if (action.equals("edit")) {
            if (CShell.isMember()) {
                setContent(_msgs.editTitle(), new EditAccountPanel());
            } else {
                Link.go(Pages.ACCOUNT, "create"); // guests/permaguests have to register first
            }

        } else if (action.equals("delete")) {
            boolean isLoggedIn = !CShell.isGuest();
            // TODO: WTF? isGuest only seems to work if we change Page and return here, but we
            // don't want to do that. We want them to log in, then return directly to the delete
            // page. So just use the creds cookie for now.
            isLoggedIn = isLoggedIn || CookieUtil.get(WebCreds.credsCookie()) != null;
            if (isLoggedIn) {
                setContent(_msgs.confirmDeleteTitle(), new ConfirmDeletePanel(args.get(1, "")));
            } else {
                // redirect guests so logging on will brink them back here
                Link.go(Pages.ACCOUNT, "create");
            }

        } else if (action.equals("config")) {
            setContent(_msgs.configTitle(), new ConfigAccountPanel());

        } else if (action.equals("optout")) {
            setContent(new OptOutPanel(args.get(1, ""), args.get(2, 0)));

        } else if (action.equals("optoutg")) {
            setContent(new OptOutPanel(args.get(1, "")));

        } else if (action.equals("resetpw")) {
            setContent(new ResetPasswordPanel(args));

        } else if (action.equals("logon") || CShell.isGuest()) {
            setContent(_msgs.logonPageTitle(), new LogonPagePanel());

        } else if (action.equals("welcome")) {
            setContent(_msgs.welcomeTitle(), new WelcomePanel());

        } else if (action.equals("v")) {
            final int memberId = args.get(1, 0);
            final String code = args.get(2, "");
            _usersvc.validateEmail(memberId, code, new InfoCallback<Boolean>() {
                public void onSuccess (Boolean valid) {
                    String msg; 
                    if (valid) {
                        msg = _msgs.emailValidated();
                        if (memberId == CShell.getMemberId()) {
                            CShell.frame.emailUpdated(CShell.creds.accountName, true);
                        }
                    } else {
                        msg = _msgs.emailInvalid();
                    }
                    setContent(_msgs.editTitle(), MsoyUI.createLabel(msg, "infoLabel"));
                }
            });

        } else {
            Link.go(Pages.ME, "");
        }
    }

    @Override
    public Pages getPageId ()
    {
        return Pages.ACCOUNT;
    }

    protected static final AccountMessages _msgs = GWT.create(AccountMessages.class);
    protected static final WebUserServiceAsync _usersvc = (WebUserServiceAsync)
        ServiceUtil.bind(GWT.create(WebUserService.class), WebUserService.ENTRY_POINT);
}

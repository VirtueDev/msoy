//
// $Id$

package client.landing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

import client.shell.FBConnect;
import client.shell.FBLogonPanel;
import client.shell.LogonPanel;
import client.shell.ShellMessages;
import client.ui.MsoyUI;
import client.ui.RegisterPanel;
import client.util.InfoCallback;

public class LandingDjPanel extends SimplePanel
{
    public LandingDjPanel (boolean light)
    {
        final String scheme = light ? "light" : "dark";
        setStyleName("landingDj " + scheme);
        FlowPanel content = MsoyUI.createFlowPanel("Content");
        this.add(content);

        // logo
        content.add(MsoyUI.createImage("/images/landing/monsterave/logo_big_white.png", "Logo"));

        // login box
        FBLogonPanel fbLogon = new FBLogonPanel(
            "/images/account/fbconnect_big.png", "/images/ui/bar_loader.gif");
        fbLogon.setStyleName("Connect");
        content.add(fbLogon);

        // https://developers.facebook.com/docs/reference/plugins/facepile/
        final Frame facepile = new Frame();
        facepile.setStyleName("FacePile");
        facepile.getElement().setAttribute("scrolling", "no");
        facepile.getElement().setAttribute("frameborder", "0");
        facepile.getElement().setAttribute("allowTransparency", "true");
        FBConnect.wait(new InfoCallback<Void>() {
            public void onSuccess (Void _) {
                String appId = FBConnect.getKey();
                facepile.setUrl("http://www.facebook.com/plugins/facepile.php?app_id=" + appId +
                    "&colorscheme=" + scheme);
            }
        });
        content.add(facepile);

        // text and copyright
        FlowPanel footer = MsoyUI.createFlowPanel("Footer");
        footer.add(MsoyUI.createHTML(_msgs.djFooter(), "Info"));
        footer.add(LandingCopyright.addFinePrint(MsoyUI.createFlowPanel("Copyright")));
        content.add(footer);
    }

    protected FlowPanel _registerPlaceholder;
    protected RegisterPanel _register;

    protected static final LandingMessages _msgs = GWT.create(LandingMessages.class);
    protected static final ShellMessages _cmsgs = GWT.create(ShellMessages.class);
}

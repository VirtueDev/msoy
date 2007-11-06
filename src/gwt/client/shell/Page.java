//
// $Id$

package client.shell;

import java.util.ArrayList;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.msoy.web.data.WebCreds;

import client.util.MsoyUI;

/**
 * Handles some standard services for a top-level MetaSOY page.
 */
public abstract class Page
{
    /** Used to dynamically create the appropriate page when we are loaded. */
    public static interface Creator {
        public Page createPage ();
    }

    /** Indicates whether we are currently displaying a Flash applet over the parts of the page
     * where popups might show up. */
    public static boolean displayingFlash = false;

    /** Indicates whether we are currently displaying a Java applet over the parts of the page
     * where popups might show up. */
    public static boolean displayingJava = false;

    // constants for our various pages
    public static final String ADMIN = "admin";
    public static final String CATALOG = "catalog";
    public static final String GAME = "game";
    public static final String GROUP = "group";
    public static final String INVENTORY = "inventory";
    public static final String MAIL = "mail";
    public static final String PROFILE = "profile";
    public static final String SWIFTLY = "swiftly";
    public static final String WHIRLED = "whirled";
    public static final String WORLD = "world";
    public static final String WRAP = "wrap";

    // constants for our top-level page elements
    public static final String NAVIGATION = "navigation";
    public static final String CONTENT = "content";
    public static final String SEPARATOR = "seppy";
    public static final String CLIENT = "client";

    /** The maximum width of our content UI, the remainder is used by the world client. */
    public static final int CONTENT_WIDTH = 700;

    /** The width of the separator bar displayed between the client and the content. */
    public static final int SEPARATOR_WIDTH = 8;

    /**
     * Notes the history token for the current page so that it can be restored in the event that we
     * open a normal page and then later close it.
     *
     * @return true if we're displaying a different page (or the same page with different
     * arguments) than the last time we entered "showing client" mode.
     */
    public static boolean setShowingClient (
        boolean clientIsFlash, boolean clientIsJava, String closeToken)
    {
        // determine whether or not we're showing a new page
        boolean newPage = !closeToken.equals(_closeToken);

        // note the current history token so that we can restore it if needed
        _closeToken = closeToken;

        // note whether we need to hack our popups
        displayingFlash = clientIsFlash;
        displayingJava = clientIsJava;

        // clear out our content and the expand/close controls
        RootPanel.get(CONTENT).clear();
        RootPanel.get(CONTENT).setWidth("0px");

        // clear out the divider
        RootPanel.get(SEPARATOR).clear();
        RootPanel.get(SEPARATOR).setWidth("0px");

        // have the client take up all the space
        RootPanel.get(CLIENT).setWidth("100%");

        return newPage;
    }

    /**
     * Returns true if we need to do our popup hackery, false if not.
     */
    public static boolean needPopupHack ()
    {
        // if we're displaying a Java applet, we always need the popup hack, but for Flash we only
        // need it on Linux
        return displayingJava || (displayingFlash && isLinux());
    }

    /**
     * Called when the page is first resolved to initialize its bits.
     */
    public void init ()
    {
        // initialize our services and translations
        initContext();

        // create our content manipulation buttons (these are shared by all pages)
        if (_closeContent == null) {
            _closeContent = MsoyUI.createActionLabel("", "CloseBox", new ClickListener() {
                public void onClick (Widget sender) {
                    CShell.app._page.closePage();
                }
            });
            _minimizeContent = MsoyUI.createActionLabel("", "Minimize", new ClickListener() {
                public void onClick (Widget sender) {
                    CShell.app._page.setContentMinimized(true, null);
                }
            });
            _maximizeContent = MsoyUI.createActionLabel("", "Maximize", new ClickListener() {
                public void onClick (Widget sender) {
                    CShell.app._page.setContentMinimized(false, null);
                }
            });
            _separatorLine = MsoyUI.createLabel("", "Separator");
        }
    }

    /**
     * Let the currently showing client reign supreme.
     */
    public void closePage ()
    {
        if (_closeToken != null) {
            new SlideContentOff().start(new Command() {
                public void execute () {
                    History.newItem(_closeToken);
                }
            });
        }
    }

    /**
     * Called when the user has navigated to this page. A call will immediately follow to {@link
     * #onHistoryChanged} with the arguments passed to this page or the empty string if no
     * arguments were supplied.
     */
    public void onPageLoad ()
    {
    }

    /**
     * Called when the user navigates to this page for the first time, and when they follow {@link
     * Application#createLink} links within tihs page.
     */
    public abstract void onHistoryChanged (Args args);

    /**
     * Called when the user navigates away from this page to another page. Gives the page a chance
     * to shut anything down before its UI is removed from the DOM.
     */
    public void onPageUnload ()
    {
    }

    /**
     * Sets the title of the browser window and the page (displayed below the Whirled logo).
     */
    public void setPageTitle (String title)
    {
        setPageTitle(title, null);
    }

    /**
     * Sets the title and subtitle of the browser window and the page. The subtitle is displayed to
     * the right of the title in the page and tacked onto the title for the browser window.
     */
    public void setPageTitle (String title, String subtitle)
    {
        if (_tabs == null) {
            createContentContainer();
        }
        _tabs.setText(0, 0, title);
        if (subtitle != null) {
            _tabs.setText(0, 1, subtitle);
            title += " - " + subtitle;
        } else {
            _tabs.setHTML(0, 1, "&nbsp;");
        }
        Window.setTitle(CShell.cmsgs.windowTitle(title));
    }

    /**
     * Clears any open client and restores the content display.
     */
    public void clearClient (boolean deferred)
    {
        if (deferred) {
            DeferredCommand.add(new Command() {
                public void execute () {
                    clearClient(false);
                }
            });
        } else {
            WorldClient.clearClient(true);
            clientWasCleared();
        }
    }

    /**
     * Minimizes or maximizes the page content. NOOP if the content min/max interface is not being
     * displayed.
     */
    public void setContentMinimized (boolean minimized, Command onComplete)
    {
        if (minimized && _minimizeContent.isAttached()) {
            RootPanel.get(SEPARATOR).remove(_minimizeContent);
            RootPanel.get(SEPARATOR).remove(_separatorLine);
            RootPanel.get(SEPARATOR).add(_maximizeContent);
            RootPanel.get(SEPARATOR).add(_separatorLine);
            new SlideContentOff().start(onComplete);

        } else if (!minimized && _maximizeContent.isAttached()) {
            RootPanel.get(SEPARATOR).remove(_maximizeContent);
            RootPanel.get(SEPARATOR).remove(_separatorLine);
            RootPanel.get(SEPARATOR).add(_minimizeContent);
            RootPanel.get(SEPARATOR).add(_separatorLine);
            new SlideContentOn().start(onComplete);

        } else if (onComplete != null) {
            // no action needed, just run the onComplete
            onComplete.execute();
        }
    }

    /**
     * Called during initialization to give our entry point and derived classes a chance to
     * initialize their respective context classes.
     */
    protected void initContext ()
    {
    }

    /**
     * Returns the identifier of this page (used for navigation).
     */
    protected abstract String getPageId ();

    /**
     * Clears out any existing content and sets the specified widget as the main page content.
     */
    protected void setContent (Widget content)
    {
        setContent(content, false, false);
    }

    /**
     * Returns the content widget last configured with {@link #setContent}.
     */
    protected Widget getContent ()
    {
        return (_content != null && _content.isCellPresent(1, 0)) ? _content.getWidget(1, 0) : null;
    }

    /**
     * Called when the client is removed from view. Clears the close button if one is showing.
     */
    protected void clientWasCleared ()
    {
        _closeToken = null;
        if (_content != null) {
            RootPanel.get(SEPARATOR).clear();
        }
    }

    /**
     * Clears out any existing content, creates a new Flash object from the definition, and
     * sets it as the new main page content. Returns the newly-created content as a widget.
     */
    protected HTML setFlashContent (String definition)
    {
        // Please note: the following is a work-around for an IE7 bug. If we create a Flash object
        // node *before* attaching it to the DOM tree, IE will silently fail to register
        // the Flash object's callback functions for access from JavaScript. To make this work,
        // create an empty node first, add it to the DOM tree, and then initialize it with
        // the Flash object definition.
        // Also see: WidgetUtil.embedFlashObject()
        HTML control = new HTML();
        setContent(control, true, false);
        control.setHTML(definition);
        return control;
    }

    /**
     * Clears out any existing content and sets the specified Java applet as the main page content.
     */
    protected void setJavaContent (Widget content)
    {
        setContent(content, false, true);
    }

    /**
     * Clears out any existing content and sets the specified widget as the main page content.
     */
    protected void setContent (Widget content, boolean contentIsFlash, boolean contentIsJava)
    {
        displayingFlash = contentIsFlash;
        displayingJava = contentIsJava;
        RootPanel.get(CONTENT).clear();

        // clear out any content height overrides
        setContentStretchHeight(false);

        // create our content container if need be
        if (_content == null) {
            createContentContainer();
        }

        // if we're displaying the client or we have a minimized page, unminimize things first
        if (_maximizeContent.isAttached() ||
            (_closeToken != null && !_minimizeContent.isAttached())) {
            RootPanel.get(SEPARATOR).clear();
            FlowPanel closeBox = new FlowPanel();
            closeBox.setStyleName("CloseBoxHolder");
            closeBox.add(_closeContent);
            RootPanel.get(SEPARATOR).add(closeBox);
            RootPanel.get(SEPARATOR).add(_minimizeContent);
            RootPanel.get(SEPARATOR).add(_separatorLine);
            new SlideContentOn().start(null);

        } else {
            RootPanel.get(CONTENT).add(_content);
            RootPanel.get(CONTENT).setWidth(CONTENT_WIDTH + "px");
        }

        // now set our content
        _content.setWidget(1, 0, content);

        // if there isn't anything in the tabs/subtitle area, we need something there to cause IE
        // to properly use up the space
        if (_tabs.getWidget(0, 1) == null && _tabs.getText(0, 1).length() == 0) {
            _tabs.setHTML(0, 1, "&nbsp;");
        }
    }

    protected void setContentStretchHeight (boolean stretch)
    {
        String height = stretch ? "99%" : ""; // fucking browsers
        RootPanel.get("ctable").setHeight(height);
        RootPanel.get(CONTENT).setHeight(height);
    }

    protected void createContentContainer ()
    {
        _content = new FlexTable();
        _content.setCellPadding(0);
        _content.setCellSpacing(0);
        _content.setWidth("100%");
        _content.setHeight("100%");

        // a separate table for this entire row, so that
        // we can set individual cell widths correctly
        _tabs = new FlexTable();
        _tabs.setCellPadding(0);
        _tabs.setCellSpacing(0);
        _tabs.setWidth("100%");
        _tabs.getFlexCellFormatter().setStyleName(0, 0, "pageHeaderTitle");
        _tabs.getFlexCellFormatter().setStyleName(0, 1, "pageHeaderContent");
        _content.setWidget(0, 0, _tabs);

        _content.getFlexCellFormatter().setHeight(1, 0, "100%");
        _content.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
    }

    protected void setPageTabs (Widget tabs)
    {
        if (_tabs == null) {
            createContentContainer();
        }
        _tabs.setWidget(0, 1, tabs);
    }

    /**
     * Called when we the player logs on while viewing this page. The default implementation
     * redisplays the current page with the current args (by calling {@link #onHistoryChanged}.
     */
    protected void didLogon (WebCreds creds)
    {
        History.onHistoryChanged(History.getToken());
    }

    /**
     * Called when the player logs off while viewing this page. The default implementation
     * redisplays the current page with the current args (by calling {@link #onHistoryChanged}).
     */
    protected void didLogoff ()
    {
        History.onHistoryChanged(History.getToken());
    }

    protected static native boolean isLinux () /*-{
        return (navigator.userAgent.toLowerCase().indexOf("linux") != -1);
    }-*/;

    protected abstract class Slider extends Timer
    {
        public void start (Command onComplete) {
            _onComplete = onComplete;
            scheduleRepeating(25);
        }

        protected void done () {
            cancel();
            if (_onComplete != null) {
                _onComplete.execute();
            }
        }

        protected Command _onComplete;
        protected static final int FRAMES = 5;
    }

    protected class SlideContentOff extends Slider
    {
        public SlideContentOff () {
            RootPanel.get(CONTENT).clear();
            WorldClient.setMinimized(false);
        }

        public void run () {
            if (_startWidth >= _endWidth) {
                RootPanel.get(CONTENT).setWidth("0px");
                RootPanel.get(CLIENT).setWidth(_endWidth + "px");
                done();

            } else {
                RootPanel.get(CONTENT).setWidth((_availWidth - _startWidth) + "px");
                RootPanel.get(CLIENT).setWidth(_startWidth + "px");
                _startWidth += _deltaWidth;
            }
        }

        protected int _availWidth = Window.getClientWidth() - SEPARATOR_WIDTH;
        protected int _startWidth = Math.max(_availWidth - CONTENT_WIDTH, 0);
        protected int _endWidth = _availWidth;
        protected int _deltaWidth = (_endWidth - _startWidth) / FRAMES;
    }

    protected class SlideContentOn extends Slider
    {
        public void run () {
            if (_startWidth <= _endWidth) {
                RootPanel.get(CONTENT).add(_content);
                RootPanel.get(CONTENT).setWidth(CONTENT_WIDTH + "px");
                RootPanel.get(CLIENT).setWidth(_endWidth + "px");
                WorldClient.setMinimized(true);
                done();

            } else {
                RootPanel.get(CONTENT).setWidth((_availWidth - _startWidth) + "px");
                RootPanel.get(CLIENT).setWidth(_startWidth + "px");
                _startWidth += _deltaWidth;
            }
        }

        protected int _availWidth = Window.getClientWidth() - SEPARATOR_WIDTH;
        protected int _endWidth = Math.max(_availWidth - CONTENT_WIDTH, 0);
        protected int _startWidth = _availWidth;
        protected int _deltaWidth = (_endWidth - _startWidth) / FRAMES;
    }

    protected FlexTable _content, _tabs;

    protected static String _closeToken;
    protected static Label _closeContent, _minimizeContent, _maximizeContent, _separatorLine;
}

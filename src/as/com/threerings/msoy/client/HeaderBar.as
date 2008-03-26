//
// $Id$

package com.threerings.msoy.client {

import flash.events.Event;
import flash.events.MouseEvent;

import mx.core.ScrollPolicy;
import mx.core.UIComponent;

import mx.containers.HBox;
import mx.containers.VBox;

import mx.controls.Label;

import flexlib.containers.ButtonScrollingCanvas;

import com.threerings.flash.TextFieldUtil;
import com.threerings.flex.CommandButton;
import com.threerings.flex.CommandLinkButton;

import com.threerings.util.CommandEvent;
import com.threerings.util.Log;

import com.threerings.msoy.chat.client.ChatTabBar;

import com.threerings.msoy.client.EmbedDialog;

public class HeaderBar extends HBox
{
    public static const HEIGHT :int = 20;

    public function HeaderBar (ctx :MsoyContext, chatTabs :ChatTabBar) 
    {
        _ctx = ctx;
        _tabs = chatTabs;
        styleName = "headerBar";

        verticalScrollPolicy = ScrollPolicy.OFF;
        horizontalScrollPolicy = ScrollPolicy.OFF;

        percentWidth = 100;
        height = HEIGHT;

        // TODO: should we be doing this?
        addEventListener(Event.ADDED_TO_STAGE, function (evt :Event) :void {
            _ctx.getMsoyClient().setWindowTitle(getChatTabs().locationName);
        });
    }

    public function getChatTabs () :ChatTabBar
    {
        return _tabs;
    }

    public function setLocationName (loc :String) :void
    {
        _loc.text = loc;
        _loc.validateNow();
        // allow text to center under the whirled logo if its not too long.
        _loc.width = Math.max(WHIRLED_LOGO_WIDTH, _loc.textWidth + TextFieldUtil.WIDTH_PAD);

        if (_tabsContainer.parent != this) {
            _tabs.locationName = Msgs.CHAT.get("l.game_channel");
        }
    }

    /**
     * Shows or clears the owner link. Passing "" for the owner will clear the link.
     */
    public function setOwnerLink (owner :String, onClick :Function = null, arg :Object = null) :void 
    {
        while (_owner.numChildren > 0) {
            _owner.removeChildAt(0);
        }
        if (owner != "") {
            var nameLink :CommandLinkButton = new CommandLinkButton(
                Msgs.GENERAL.get("m.room_owner", owner), onClick, arg);
            nameLink.styleName = "headerLink";
            nameLink.enabled = !_ctx.getMsoyClient().isEmbedded();
            _owner.addChild(nameLink);
        }
    }

    /**
     * Shows or clears the comment link. Passing null for the onClick function will clear the link.
     */
    public function setInstructionsLink (onClick :Function , arg :Object = null) :void
    {
        _instructionsLink.setCallback(onClick, arg);
        _instructionsVisible = (onClick != null);
        _instructionsLink.visible = _instructionsVisible;
        _instructionsLink.includeInLayout = _instructionsVisible;
    }

    /**
     * Shows or clears the comment link. Passing null for the onClick function will clear the link.
     */
    public function setCommentLink (onClick :Function, arg :Object = null) :void
    {
        _commentLink.setCallback(onClick, arg);
        _commentVisible = (onClick != null);
        _commentLink.visible = _commentVisible;
        _commentLink.includeInLayout = _commentVisible;
    }

    public function setEmbedVisible (visible :Boolean) :void
    {
        _embedVisible = visible;
        _embedLink.includeInLayout = _embedLink.visible = visible;
    }

    public function miniChanged () :void
    {
        if (_ctx.getTopPanel().isMinimized()) {
            for each (var comp :UIComponent in _extras) {
                comp.includeInLayout = comp.visible = false;
            }
            stretchSpacer(false);
        } else {
            for each (comp in _extras) {
                if (comp == _embedLink) {
                    comp.includeInLayout = comp.visible = _embedVisible;
                } else if (comp == _commentLink) {
                    comp.includeInLayout = comp.visible = _commentVisible;
                } else if (comp == _instructionsLink) {
                    comp.includeInLayout = comp.visible = _instructionsVisible;
                } else {
                    comp.includeInLayout = comp.visible = true;
                }
            }
            stretchSpacer(true);
        }
    }

    /**
     * Grab the tabs and the associated trimmings in the container that contains them, so they
     * can be moved somewhere else.
     */
    public function removeTabsContainer () :UIComponent
    {
        if (_tabsContainer.parent == this) {
            removeChild(_tabsContainer);
        }
        _loc.visible = _loc.includeInLayout = true;
        _loc.validateNow();
        // allow text to center under the whirled logo if its not too long.
        _loc.width = Math.max(WHIRLED_LOGO_WIDTH, _loc.textWidth + TextFieldUtil.WIDTH_PAD);
        stretchSpacer(false);
        return _tabsContainer;
    }

    public function replaceTabsContainer () :void
    {
        if (_tabsContainer.parent != null) {
            _tabsContainer.parent.removeChild(_tabsContainer);
        }
        _tabs.locationName = null;
        if (_loc.parent == this) {
            _loc.visible = _loc.includeInLayout = false;
        }
        addChildAt(_tabsContainer, 1);
    }

    public function stretchSpacer (stretch :Boolean) :void
    {
        var mini :Boolean = _ctx.getTopPanel().isMinimized();
        var ownTabs :Boolean = _tabsContainer.parent == this;
        var stretchTabs :Boolean = !(stretch && ownTabs && !mini);
        var stretchSpacer :Boolean = (stretch || !ownTabs) && !mini;
        if (stretchTabs == isNaN(_tabsContainer.percentWidth)) {
            _tabsContainer.percentWidth = stretchTabs ? 100 : NaN;
        }
        if (stretchSpacer == isNaN(_spacer.percentWidth)) {
            _spacer.percentWidth = stretchSpacer ? 100 : NaN;
        }
    }

    override protected function createChildren () :void
    {
        super.createChildren();

        _loc = new Label();
        _loc.styleName = "locationName";
        _loc.width = WHIRLED_LOGO_WIDTH;
        _loc.visible = _loc.includeInLayout = false;
        addChild(_loc);

        _tabsContainer = new TabsContainer(this);
        _tabsContainer.horizontalScrollPolicy = ScrollPolicy.OFF;
        addChild(_tabsContainer);

        _tabsContainer.addChild(_tabs);

        _owner = new HBox();
        _owner.styleName = "headerBox";
        _owner.percentHeight = 100;
        addChild(_owner);
        _extras.push(_owner);

        _spacer = new Spacer(this);
        addChild(_spacer);

        var controlBox :HBox = new HBox();
        controlBox.styleName = "headerBox";
        controlBox.percentHeight = 100;
        addChild(controlBox);

        _instructionsLink = new CommandLinkButton(Msgs.GENERAL.get("b.instructions"));
        _instructionsLink.styleName = "headerLink";
        controlBox.addChild(_instructionsLink);
        setInstructionsLink(null);
        _extras.push(_instructionsLink);

        _commentLink = new CommandLinkButton(Msgs.GENERAL.get("b.comment"));
        _commentLink.styleName = "headerLink";
        controlBox.addChild(_commentLink);
        setCommentLink(null);
        _extras.push(_commentLink);

        _embedLink = new CommandLinkButton(Msgs.GENERAL.get("b.share"), function () :void {
                new EmbedDialog(_ctx);
            });
        _embedLink.styleName = "headerLink";
        controlBox.addChild(_embedLink);
        setEmbedVisible(false);
        _extras.push(_embedLink);

        var closeBox :VBox = new VBox();
        closeBox.styleName = "headerCloseBox";
        controlBox.addChild(closeBox);
        var closeBtn :CommandButton = new CommandButton();
        closeBtn.setCommand(MsoyController.CLOSE_PLACE_VIEW);
        closeBtn.styleName = "closeButton";
        closeBox.addChild(closeBtn);
    }

    private static const log :Log = Log.getLog(HeaderBar);

    protected static const WHIRLED_LOGO_WIDTH :int = 124;

    protected var _ctx :MsoyContext;

    protected var _loc :Label;
    protected var _owner :HBox;
    protected var _spacer :HBox;

    protected var _instructionsVisible :Boolean;
    protected var _instructionsLink :CommandLinkButton;

    protected var _commentVisible :Boolean;
    protected var _commentLink :CommandLinkButton;

    protected var _embedVisible :Boolean;
    protected var _embedLink :CommandLinkButton;

    protected var _tabs :ChatTabBar;

    /** Bits that get removed when in minimized view */
    protected var _extras :Array = [];

    protected var _tabsContainer :TabsContainer;
}
}

import mx.containers.HBox;

import com.threerings.msoy.client.HeaderBar;

class Spacer extends HBox
{
    public function Spacer (headerBar :HeaderBar) 
    {
        _headerBar = headerBar;

        setStyle("borderThickness", 0);
        setStyle("borderStyle", "none");
        percentWidth = 100;
    }

    override public function setActualSize (w :Number, h :Number) :void
    {
        super.setActualSize(w, h);
        _headerBar.stretchSpacer(w != 0);
    }

    protected var _headerBar :HeaderBar;
}

class TabsContainer extends HBox
{
    public function TabsContainer (headerBar :HeaderBar)
    {
        _headerBar = headerBar;

        setStyle("borderThickness", 0);
        setStyle("borderStyle", "none");
        setStyle("horizontalGap", 0);
        explicitMinWidth = 0;
    }

    override public function setActualSize (w :Number, h :Number) :void
    {
        super.setActualSize(w, h);
        if (w > getExplicitOrMeasuredWidth()) {
            _headerBar.stretchSpacer(true);
        }
    }

    protected var _headerBar :HeaderBar;
}

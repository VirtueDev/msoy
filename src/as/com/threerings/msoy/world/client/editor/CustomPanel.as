//
// $Id$

package com.threerings.msoy.world.client.editor {

import flash.display.DisplayObject;
import flash.display.Shape;

import mx.core.UIComponent;

import mx.containers.Canvas;

import com.threerings.msoy.world.client.FurniSprite;

public class CustomPanel extends UIComponent
{
    /**
     * Create a panel for holding custom configuration options.
     *
     * @param hiders a list of UIComponents that should be hidden if there is no custom
     * config panel.
     */
    public function CustomPanel (... hiders)
    {
        _hiders = hiders;
        maxWidth = 400;
        maxHeight = 300;
    }

    /**
     * Update the sprite for which we're displaying a custom panel.
     */
    public function updateDisplay (target :FurniSprite) :void
    {
        if (target == _target) {
            // yes, this happens all the goddamn time.
            // TODO: fix editor.
            return;
        }

        if (_userPanel != null) {
            removeChild(_userPanel);
            _userPanel = null;
        }

        // assign the new target
        _target = target;

        if (target != null) {
            _userPanel = target.getCustomConfigPanel();
            if (_userPanel != null) {
                // NOTE: we do not do any masking, because that breaks certain popups
                // (fl component ColorPicker, anyway)
                // and because this only appears if the user specifically requests it.
                // We may want to reconsider this in the future.
                addChild(_userPanel);
                setActualSize(_userPanel.width, _userPanel.height);
            }
        }

        recheckVisibility();
    }

    override public function get numChildren () :int
    {
        // TODO: we do this to avoid a bunch of annoying shit in flexland.
        // What we should do is create a bulletproof flex component for hosting regular
        // DisplayObjects, and use that in a lot of places, including LayeredContainer
        return 0;
    }

    /**
     * Figure out if we should be showing.
     */
    protected function recheckVisibility () :void
    {
        var vis :Boolean = (_userPanel != null);
        for each (var comp :UIComponent in _hiders) {
            comp.visible = vis;
            comp.includeInLayout = vis;
        }
    }

    override protected function createChildren () :void
    {
        super.createChildren();

        recheckVisibility();
    }

    protected var _hiders :Array;

    protected var _target :FurniSprite;

    protected var _userPanel :DisplayObject;
}
}

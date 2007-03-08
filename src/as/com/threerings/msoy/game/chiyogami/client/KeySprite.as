package com.threerings.msoy.game.chiyogami.client {

import flash.display.DisplayObject;
import flash.display.MovieClip;
import flash.display.Sprite;

import flash.ui.Keyboard;

public class KeySprite extends Sprite
{
    public static const WIDTH :int = 50;
    public static const HEIGHT :int = 50;

    //public static const PAD :int = 9;

    public function KeySprite (key :int, clazz :Class)
    {
        _key = key;

        _arrow = (new clazz() as MovieClip);

        _arrow.x = WIDTH/2;
        _arrow.y = HEIGHT/2;
        addChild(_arrow);

        updateVis();
    }

    public function getKey () :int
    {
        return _key;
    }

    public function setHit (hit :Boolean) :void
    {
        _hit = hit;
        updateVis();
    }

    protected function updateVis () :void
    {
        _arrow.gotoAndStop(_hit ? 1 : 3);
    }

    /** The key we're representing. */
    protected var _key :int;

    protected var _arrow :MovieClip;

    protected var _hit :Boolean = false;
}
}

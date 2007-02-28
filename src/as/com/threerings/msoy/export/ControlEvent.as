//
// $Id$

package com.threerings.msoy.export {

import flash.events.Event;

public class ControlEvent extends Event
{
    /** An event type dispatched when an Actor has had its appearance
     * changed. Your code should react to this event and possibly redraw
     * the actor, taking into account the orientation and whether the
     * actor is moving.
     * name: unused
     * value: unused
     */
    public static const APPEARANCE_CHANGED :String = "appearanceChanged";

    /** An event type dispatched when an event is triggered.
     * name: The event name
     * value: unused
     */
    public static const EVENT_TRIGGERED :String = "eventTriggered";

    /** An event type dispatched when this avatar speaks.
     * name: unused
     * value: unused
     */
    public static const AVATAR_SPOKE :String = "avatarSpoke";

    /** An event type dispatched when this avatar is playing a custom action.
     * name: action name
     * value: action argument
     */
    public static const ACTION_TRIGGERED :String = "actionTriggered";

    /** An event type dispatched when this client-side instance of the item
     * has gained "control" over the other client-side instances.
     * name: unused
     * value: unused
     */
    public static const GOT_CONTROL :String = "gotControl";

    /** An event type dispatched when the memory has changed.
     * name: memory name
     * value: memory value
     */
    public static const MEMORY_CHANGED :String = "memoryChanged";

    /**
     * Retrieve the event target, which will be the WhirledControl instance that
     * dispatched this event.
     */
    override public function get target () :Object
    {
        return super.target;
    }

    /**
     * Retrieve the 'name' for this event, which is a String value
     * whose meaning is determined by the event type.
     */
    public function get name () :String
    {
        return _name;
    }

    /**
     * Retrieve the object 'value' for this event, which is a value
     * whose meaning is determined by the event type.
     */
    public function get value () :Object
    {
        return _value;
    }

    /**
     * Create a new ControlEvent.
     */
    public function ControlEvent (
        type :String, name :String = null, value :Object = null)
    {
        super(type);
        _name = name;
        _value = value;
    }

    // documentation inherited from Event
    override public function clone () :Event
    {
        return new ControlEvent(type, _name, _value);
    }

    /** Internal storage for our name property. */
    protected var _name :String;

    /** Internal storage for our value property. */
    protected var _value :Object;
}
}

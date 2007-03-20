//
// $Id$

package client.util;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.impl.PopupImpl;

/**
 * Mozilla implementation of {@link PopupImpl} that does the right thing with Flash and other
 * non-well-behaved layers.
 */
public class PopupImplMozilla extends PopupImpl
{
    public native void onHide (Element popup) /*-{
        var frame = popup.__frame;
        if (frame) {
            $doc.body.removeChild(frame);
            popup.__frame = null;
        }
    }-*/;

    public native void onShow (Element popup) /*-{
        if (!@client.shell.Page::needPopupHack) {
            return;
        }

        var frame = $doc.createElement('iframe');
        frame.scrolling = 'no';
        frame.frameBorder = 0;
        frame.style.position = 'absolute';
        frame.style.display = 'block';

        // stick these way up above everything else just for safety
        popup.style.zIndex = 2000;
        frame.style.zIndex = 1999;

        popup.__frame = frame;
        frame.style.left = popup.offsetLeft + "px";
        frame.style.top = popup.offsetTop + "px";
        frame.style.width = popup.offsetWidth + "px";
        frame.style.height = popup.offsetHeight + "px";
        popup.parentNode.insertBefore(frame, popup);
    }-*/;
}

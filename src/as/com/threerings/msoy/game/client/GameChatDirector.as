//
// $Id$

package com.threerings.msoy.game.client {

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.chat.client.ChatDisplay;
import com.threerings.crowd.chat.client.SpeakService;
import com.threerings.crowd.chat.data.ChatMessage;

import com.threerings.msoy.client.BaseContext;
import com.threerings.msoy.data.MsoyCodes;

import com.threerings.msoy.chat.client.ChatOverlay;
import com.threerings.msoy.chat.client.HistoryList;

/**
 * Does some special business for handling chat on the game server side of things.
 */
public class GameChatDirector extends ChatDirector
{
    public function GameChatDirector (gctx :GameContext)
    {
        super(gctx, gctx.getBaseContext().getMessageManager(), MsoyCodes.CHAT_MSGS);
        _mctx = gctx.getBaseContext();
    }

    public function getGameHistory () :HistoryList
    {
        return _gameHistory;
    }

    // from ChatDirector
    override public function pushChatDisplay (display :ChatDisplay) :void
    {
        if (display is ChatOverlay) {
            (display as ChatOverlay).setHistory(_gameHistory);
            _mctx.getTopPanel().setActiveOverlay(display as ChatOverlay);
        }
        super.pushChatDisplay(display);

        // redirect chat to this chat director
        grabChatControl();
    }

    // from ChatDirector
    override public function addChatDisplay (display :ChatDisplay) :void
    {
        if (display is ChatOverlay) {
            (display as ChatOverlay).setHistory(_gameHistory);
            _mctx.getTopPanel().setActiveOverlay(display as ChatOverlay);
        }
        super.addChatDisplay(display);

        // redirect chat to this chat director
        grabChatControl();
    }

    // from ChatDirector
    override public function removeChatDisplay (display :ChatDisplay) :void
    {
        super.removeChatDisplay(display);

        if (_displays.size() == 0) {
            // restore chat to the default director
            releaseChatControl();
        }
    }

    // from ChatDirector
    override public function clearDisplays () :void
    {
        super.clearDisplays();
        _gameHistory.clear();
    }

    // from ChatDirector
    override public function requestChat (speakSvc :SpeakService, text :String, 
        record :Boolean) :String
    {
        if (_mctx.getTopPanel().getHeaderBar().getChatTabs().
                getCurrentController() != null) {
            // if there is a tab other than game active, let the other chat director handle it.
            return _mctx.getMsoyChatDirector().requestChat(speakSvc, text, record);
        }
        return super.requestChat(speakSvc, text, record);
    }

    protected function grabChatControl () :void
    {
        _mctx.getTopPanel().getControlBar().setChatDirector(this);
        _mctx.getTopPanel().getHeaderBar().getChatTabs().setChatDirector(this);
    }

    protected function releaseChatControl () :void
    {
        _mctx.getTopPanel().getControlBar().setChatDirector(null);
        _mctx.getTopPanel().getHeaderBar().getChatTabs().setChatDirector(null);
        _mctx.getTopPanel().setActiveOverlay(null);
    }

    // from ChatDirector
    override protected function dispatchPreparedMessage (msg :ChatMessage) :void
    {
        _gameHistory.addMessage(msg);
        super.dispatchPreparedMessage(msg);
    }

    protected var _mctx :BaseContext;
    protected var _gameHistory :HistoryList = new HistoryList();
}
}

package com.peterlocher.chatapp

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.get
import kotlinx.android.synthetic.main.text_message_view.view.*

class TextMessageView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        LayoutInflater.from(context).inflate(R.layout.text_message_view, this)
    }

    enum class Side { LEFT, RIGHT }
    var side: Side = Side.LEFT

    /*  Shifts MessageView to the right side of the screen
    *   Styles it as an outgoing message
    * */
    fun toRightMessage() {
        if (side == Side.LEFT) {
            side = Side.RIGHT
            val filler = messageLayout[1]
            messageLayout.removeViewAt(1)
            messageLayout.addView(filler, 0)
            messageText.setBackgroundColor(context.getColor(R.color.colorTextMessageOwn))
            messageHeader.gravity = Gravity.START
        }
    }

    /*  Shifts MessageView to the left side of the screen
    *   Styles it as an incoming message
    * */
    fun toLeftMessage() {
        if (side == Side.RIGHT) {
            side = Side.LEFT
            val filler = messageLayout[0]
            messageLayout.removeViewAt(0)
            messageLayout.addView(filler)
            messageText.setBackgroundColor(context.getColor(R.color.colorTextMessage))
            messageHeader.gravity = Gravity.END
        }
    }
}
package fi.valtteri.birdwatcher.ui.addentry

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText

class SpeciesSelectionEditText : TextInputEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int): super(context, attributeSet, defStyle)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        performClick()
        return false
    }

    override fun performClick(): Boolean {
        super.performClick()
        callOnClick()
        return true
    }


}
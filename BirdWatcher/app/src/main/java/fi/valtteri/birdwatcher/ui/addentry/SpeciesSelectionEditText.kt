package fi.valtteri.birdwatcher.ui.addentry

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber

class SpeciesSelectionEditText : TextInputEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int): super(context, attributeSet, defStyle)

    interface OpenSpeciesSelection {
        fun open()
    }

    private var openSpeciesSelection: OpenSpeciesSelection? = null

    fun setOpenSpeciesSelection(openSpeciesSelection: OpenSpeciesSelection) {
        this.openSpeciesSelection = openSpeciesSelection
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                Timber.d("DOWN")
                return true
            }
            MotionEvent.ACTION_UP -> {
                Timber.d("UP")
                openSpeciesSelection?.open()
                return true
            }
            else -> {
                Timber.d("OTHER")
            return super.onTouchEvent(event)

            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        openSpeciesSelection?.open()
        return true
    }



}
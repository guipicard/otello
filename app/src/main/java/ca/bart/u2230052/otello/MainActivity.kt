package ca.bart.u2230052.otello

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Parcelable
import android.util.Log
import ca.bart.u2230052.otello.databinding.ActivityMainBinding
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import androidx.core.view.get
import androidx.core.view.children

@Parcelize
data class Cell(var toggled:Boolean = false, var player:Int = 0) : Parcelable

@Parcelize
data class Model(var grid:Array<Cell>) : Parcelable

class MainActivity : Activity() {
    companion object {
        const val COLUMNS = 8
        const val ROWS = 8
        const val TAG = "OTELLO"
        const val KEY_STATE = "KEY_STATE"

        val Pair<Int, Int>.x : Int
            get() = first
        val Pair<Int, Int>.y : Int
            get() = second

        var TURN = 1;

        var choices = MutableList(0){MutableList(0){Int} }

        fun Pair<Int, Int>.toIndex() = x + y * COLUMNS
        fun Int.toCoordinates() = Pair(this % COLUMNS, this / COLUMNS)
        fun Int.GetRight() = this + 1
        fun Int.GetLeft() = this - 1
        fun Int.GetUp() = this - 8
        fun Int.GetDown() = this + 8
        fun Int.GetUpRight() = this - 7
        fun Int.GetUpLeft() = this - 9
        fun Int.GetDownRight() = this + 9
        fun Int.GetDownLeft() = this - 7
    }

    var model = Model(Array(ROWS * COLUMNS) {Cell()})

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "onCreate($savedInstanceState)")
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        for (index in 0 until binding.grid.childCount) {
            binding.grid[index].setOnClickListener {onDiscClicked(index)}
        }
    }

    fun NextTurn() {
        TURN++;
        if (TURN == 5) TURN = 1
    }

    fun refresh() {
        binding.grid.children.withIndex().zip(model.grid.asSequence()).forEach { (indexedView, cell) ->
            indexedView.value.setBackgroundResource(
                if (cell.toggled)
                    if (cell.player == 1)
                        R.drawable.blue_disc
                    else if (cell.player == 2)
                        R.drawable.green_disc
                    else if (cell.player == 3)
                        R.drawable.red_disc
                    else if (cell.player == 4)
                        R.drawable.yellow_disc
                    else
                        R.drawable.black_disc
                else R.drawable.black_disc
            )
        }
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart()")
        super.onRestart()
    }

    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState($outState)")
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_STATE, model)
//            outState.putString(TEXT_STATE, binding.textView.text.toString())
//            outState.putString(EDIT_STATE, binding.editText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "onRestoreInstanceState($savedInstanceState)")
        super.onRestoreInstanceState(savedInstanceState)

        val model =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                savedInstanceState.getParcelable(KEY_STATE, Model::class.java)
            else
//            text_value = savedInstanceState.getString(TEXT_STATE).toString()
//            edit_value = savedInstanceState.getString(EDIT_STATE).toString()
        model?.let {
//            this.model1 = it
            refresh()
        }
    }



    fun onDiscSelected() {
        for (index in 0 until 8)
        {

        }

    }

    fun onDiscClicked(index: Int) {
        Log.d(TAG, "onDiscClicked($index): ${index.toCoordinates()}")

        model.grid[index].toggled = true

        refresh()
    }

    fun HighlightChoices(index: Int) {

    }
}

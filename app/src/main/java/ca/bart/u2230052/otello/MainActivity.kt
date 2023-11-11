package ca.bart.u2230052.otello

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible
import ca.bart.u2230052.otello.databinding.ActivityMainBinding
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Parcelize
data class Players(
    var num: Int = 0,
    var cellAlive: Int = 0,
    var dead: Boolean = false,
    var deadBy: Int = 0,
    var backgroundResId: Int = R.drawable.black_disc,
    var isAi: Boolean = false,
    var ownedCells: MutableList<Int>
) : Parcelable

@Parcelize
data class Cell(
    var toggled: Boolean = true,
    var player: Int = 0,
    var selected: Boolean = false,
    var highlighted: Boolean = false,
    var backgroundResId: Int = R.drawable.black_disc
) : Parcelable

@Parcelize
data class Model(
    var grid: Array<Cell>,
    var players: MutableList<Players>,
    var highlightedPositions: MutableList<Int>,
    var cellsToChange: MutableList<MutableList<Int>>,
    var skipsInRow: Int = 0
) : Parcelable

class MainActivity : Activity() {
    companion object {
        const val COLUMNS = 8
        const val ROWS = 8
        const val TAG = "OTELLO"
        const val KEY_STATE = "KEY_STATE"

        var SELECTED: Int = 0

        val Pair<Int, Int>.x: Int
            get() = first
        val Pair<Int, Int>.y: Int
            get() = second

        var TURN = 1

        fun Pair<Int, Int>.toIndex() = x + y * COLUMNS
        fun Int.toCoordinates() = Pair(this % COLUMNS, this / COLUMNS)
        fun Pair<Int, Int>.GetRight() = Pair(this.x + 1, this.y)
        fun Pair<Int, Int>.GetLeft() = Pair(this.x - 1, this.y)
        fun Pair<Int, Int>.GetUp() = Pair(this.x, this.y - 1)
        fun Pair<Int, Int>.GetDown() = Pair(this.x, this.y + 1)
        fun Pair<Int, Int>.GetUpRight() = Pair(this.x + 1, this.y - 1)
        fun Pair<Int, Int>.GetUpLeft() = Pair(this.x - 1, this.y - 1)
        fun Pair<Int, Int>.GetDownRight() = Pair(this.x + 1, this.y + 1)
        fun Pair<Int, Int>.GetDownLeft() = Pair(this.x - 1, this.y + 1)
    }

    var player1 = Players(
        1, 1, false, 0, R.drawable.red_disc, false, MutableList(1) { 27 }
    )
    var player2 = Players(
        2, 1, false, 0, R.drawable.green_disc, false, MutableList(1) { 28 }
    )
    var player3 = Players(
        3, 1, false, 0, R.drawable.blue_disc, false, MutableList(1) { 35 }
    )
    var player4 = Players(
        4, 1, false, 0, R.drawable.yellow_disc, false, MutableList(1) { 36 }
    )

    var model = Model(
        Array(ROWS * COLUMNS) { Cell() },
        mutableListOf(player1, player2, player3, player4),
        mutableListOf(),
        mutableListOf()
    )

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate($savedInstanceState)")
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        for (index in 0 until binding.game.grid.childCount) {
            if (index == 27) {
                model.grid[index].player = 1
                model.grid[index].backgroundResId = R.drawable.red_disc
            } else if (index == 28) {
                model.grid[index].player = 2
                model.grid[index].backgroundResId = R.drawable.green_disc
            } else if (index == 35) {
                model.grid[index].player = 3
                model.grid[index].backgroundResId = R.drawable.blue_disc
            } else if (index == 36) {
                model.grid[index].player = 4
                model.grid[index].backgroundResId = R.drawable.yellow_disc
            } else {
                model.grid[index].toggled = false
                model.grid[index].player = 0
            }
            binding.game.grid[index].setOnClickListener { onDiscClicked(index) }
        }

        binding.player1Skip.setOnClickListener { onSkip() }
        binding.player2Skip.setOnClickListener { onSkip() }
        binding.player3Skip.setOnClickListener { onSkip() }
        binding.player4Skip.setOnClickListener { onSkip() }

        binding.aiToggle1.setOnCheckedChangeListener { buttonView, isChecked ->
            model.players[0].isAi = isChecked
            if (buttonView.isChecked) {
                binding.player1Skip.setOnClickListener { playLoop() }
                binding.player1Skip.setText(R.string.ai_play)
            } else {
                binding.player1Skip.setOnClickListener { onSkip() }
                binding.player1Skip.setText(R.string.skip_turn)
            }
        }
        binding.aiToggle2.setOnCheckedChangeListener { buttonView, isChecked ->
            model.players[1].isAi = isChecked
            if (buttonView.isChecked) {
                binding.player2Skip.setOnClickListener { playLoop() }
                binding.player2Skip.setText(R.string.ai_play)
            } else {
                binding.player2Skip.setOnClickListener { onSkip() }
                binding.player2Skip.setText(R.string.skip_turn)
            }
        }
        binding.aiToggle3.setOnCheckedChangeListener { buttonView, isChecked ->
            model.players[2].isAi = isChecked
            if (buttonView.isChecked) {
                binding.player3Skip.setOnClickListener { playLoop() }
                binding.player3Skip.setText(R.string.ai_play)
            } else {
                binding.player3Skip.setOnClickListener { onSkip() }
                binding.player3Skip.setText(R.string.skip_turn)
            }
        }
        binding.aiToggle4.setOnCheckedChangeListener { buttonView, isChecked ->
            model.players[3].isAi = isChecked
            if (buttonView.isChecked) {
                binding.player4Skip.setOnClickListener { playLoop() }
                binding.player4Skip.setText(R.string.ai_play)
            } else {
                binding.player4Skip.setOnClickListener { onSkip() }
                binding.player4Skip.setText(R.string.skip_turn)
            }
        }

        refresh()
    }

    fun refresh() {
        Log.d(TAG, "refresh()")
        binding.game.grid.children.withIndex().zip(model.grid.asSequence())
            .forEach { (indexedView, cell) ->
                indexedView.value.setBackgroundResource(cell.backgroundResId)
            }
        binding.player1Txt.text = model.players[0].cellAlive.toString()
        binding.player2Txt.text = model.players[1].cellAlive.toString()
        binding.player3Txt.text = model.players[2].cellAlive.toString()
        binding.player4Txt.text = model.players[3].cellAlive.toString()

        binding.player1Skip.isClickable = TURN == 1
        binding.player2Skip.isClickable = TURN == 2
        binding.player3Skip.isClickable = TURN == 3
        binding.player4Skip.isClickable = TURN == 4

        if (model.players[TURN - 1].dead) {
            binding.slash.isVisible = true
            binding.turnCircle2.isVisible = true
            binding.turnCircle2?.setBackgroundResource(
                when (model.players[TURN - 1].deadBy) {
                    1 -> R.drawable.red_disc
                    2 -> R.drawable.green_disc
                    3 -> R.drawable.blue_disc
                    4 -> R.drawable.yellow_disc
                    else -> R.drawable.black_disc
                }
            )
        } else {

            binding.slash.isVisible = false
            binding.turnCircle2.isVisible = false
        }
        binding.turnCircle1?.setBackgroundResource(
            when (TURN) {
                1 -> R.drawable.red_disc
                2 -> R.drawable.green_disc
                3 -> R.drawable.blue_disc
                4 -> R.drawable.yellow_disc
                else -> R.drawable.black_disc
            }
        )

        if (model.players[0].cellAlive + model.players[1].cellAlive + model.players[2].cellAlive + model.players[3].cellAlive == COLUMNS * ROWS) {
            showGameOverDialog()
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
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "onRestoreInstanceState($savedInstanceState)")
        super.onRestoreInstanceState(savedInstanceState)

        val savedModel =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                savedInstanceState.getParcelable(KEY_STATE, Model::class.java)
            else
                null

        savedModel?.let {
            Log.d(TAG, it.toString())
            model = it
            refresh()
        }
    }

    fun onDiscSelected(index: Int) {
        Log.d(TAG, "onDiscSelected(index: $index)")

        val currentCell = index.toCoordinates()
        var currentPlayer = TURN
        if (model.players[TURN - 1].dead) {
            currentPlayer = model.players[TURN - 1].deadBy
        }
        SELECTED = index

        for (direction in 0 until 8) {
            var nextCell = when (direction) {
                0 -> currentCell.GetRight()
                1 -> currentCell.GetLeft()
                2 -> currentCell.GetUp()
                3 -> currentCell.GetDown()
                4 -> currentCell.GetUpRight()
                5 -> currentCell.GetUpLeft()
                6 -> currentCell.GetDownRight()
                7 -> currentCell.GetDownLeft()
                else -> currentCell
            }

            if (nextCell.x in 0 until COLUMNS && nextCell.y in 0 until ROWS) {
                val nextIndex = nextCell.toIndex()


                if (model.grid[nextIndex].toggled && model.grid[nextIndex].player != currentPlayer) {
                    var validMoveFound = false
                    val changedIndexes = mutableListOf<Int>(index)

                    while (nextCell.x in 0 until COLUMNS && nextCell.y in 0 until ROWS) {
                        val tempIndex = nextCell.toIndex()
                        changedIndexes.add(tempIndex)

                        if (!model.grid[tempIndex].toggled) {
                            validMoveFound = true
                            model.highlightedPositions.add(tempIndex)
                            model.grid[tempIndex].backgroundResId = R.drawable.white_disc
                            model.grid[tempIndex].highlighted = true
                            break
                        } else if (model.grid[tempIndex].player == currentPlayer) {
                            break
                        }

                        nextCell = when (direction) {
                            0 -> nextCell.GetRight()
                            1 -> nextCell.GetLeft()
                            2 -> nextCell.GetUp()
                            3 -> nextCell.GetDown()
                            4 -> nextCell.GetUpRight()
                            5 -> nextCell.GetUpLeft()
                            6 -> nextCell.GetDownRight()
                            7 -> nextCell.GetDownLeft()
                            else -> nextCell
                        }
                    }

                    if (validMoveFound) {
                        model.cellsToChange.add(changedIndexes)
                    }
                }
            }
        }
    }

    fun onDiscUnselected() {
        Log.d(TAG, "onDiscUnselected()")

        model.highlightedPositions.forEach { highlightedIndex ->
            model.grid[highlightedIndex].highlighted = false
            model.grid[highlightedIndex].backgroundResId =
                when (model.grid[highlightedIndex].player) {
                    1 -> R.drawable.red_disc
                    2 -> R.drawable.green_disc
                    3 -> R.drawable.blue_disc
                    4 -> R.drawable.yellow_disc
                    else -> R.drawable.black_disc
                }
        }
        model.highlightedPositions.clear()
        model.cellsToChange.clear()
    }


    fun onDiscClicked(index: Int) {
        Log.d(TAG, "onDiscClicked(index: $index)")
        if (!getPlayer().isAi) {
            selectCell(index)
        }
    }

    fun playLoop() {
        Log.d(TAG, "playLoop()")
        var currentPlayer = TURN
        if (getPlayer().dead) {
            currentPlayer = model.players[currentPlayer - 1].deadBy
        }
        var player = model.players[currentPlayer - 1]
        var ownedList = player.ownedCells
        var loops = 0
        while (model.highlightedPositions.size < 1) {
            if (loops > 8) {
                ownedList.forEach { cell ->
                    selectCell(cell)
                    if (model.highlightedPositions.size > 0) {
                        ownedList = model.highlightedPositions
                        var randomIndex = Random.nextInt(ownedList.size)
                        var randomValue = ownedList[randomIndex]
                        selectCell(randomValue)
                        refresh()
                        return
                    }
                }
                onDiscUnselected()
                onSkip()
                refresh()
                return
            } else {
                loops++
                var randomIndex = Random.nextInt(ownedList.size)
                var randomValue = ownedList[randomIndex]
                selectCell(randomValue)
            }
        }
        ownedList = model.highlightedPositions
        var randomIndex = Random.nextInt(ownedList.size)
        var randomValue = ownedList[randomIndex]
        selectCell(randomValue)
    }

    fun selectCell(index: Int) {
        Log.d(TAG, "selectCell($index)")
        var currentPlayer = TURN
        if (getPlayer().dead) {
            currentPlayer = model.players[currentPlayer - 1].deadBy
        }

        if (model.grid[index].highlighted) {
            applyMoveColors(index)
            model.grid[index].selected = false
            onDiscUnselected()
            nextTurn()
            refresh()
            return
        }

        if (model.grid[index].player == currentPlayer) {
            onDiscUnselected()
            model.grid[index].selected = true
            onDiscSelected(index)
            refresh()
            return
        }
        onDiscUnselected()
        refresh()
    }

    fun applyMoveColors(index: Int) {
        Log.d(TAG, "applyMoveColors($index)")

        val selectedCell = model.grid[SELECTED]
        val highlights = model.highlightedPositions

        highlights?.let {
            for (i in it.indices) {
                val moveIndex = it[i]
                if (moveIndex == index) {
                    val toChange = model.cellsToChange[i]
                    toChange.forEach {
                        killCell(it)
                        spawnCell(it)
                        model.grid[it].player = TURN
                        model.grid[it].toggled = true
                        model.grid[it].highlighted = false
                        model.grid[it].backgroundResId = when (selectedCell.player) {
                            1 -> R.drawable.red_disc
                            2 -> R.drawable.green_disc
                            3 -> R.drawable.blue_disc
                            4 -> R.drawable.yellow_disc
                            else -> R.drawable.black_disc
                        }
                    }
                }
            }
        }
    }

    fun spawnCell(index: Int) {
        var player = getPlayer()
        player.cellAlive++
        player.dead = false
        player.deadBy = 0
        player.ownedCells.add(index)
    }

    fun killCell(index: Int) {
        for (player in model.players) {
            if (player.backgroundResId == model.grid[index].backgroundResId) {
                player.cellAlive--
                player.ownedCells.remove(index)
                if (player.cellAlive <= 0) {
                    player.dead = true
                    player.deadBy = TURN
                }
                break
            }
        }
    }

    fun showGameOverDialog() {
        Log.d(TAG, "endGame")
        val builder = AlertDialog.Builder(this@MainActivity)

        builder?.setTitle(R.string.over_title)
        builder?.setMessage(R.string.over_sub)

        builder?.setPositiveButton(R.string.ai_yes) { dialog, which ->
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            dialog.dismiss()
            startActivity(intent)
        }

        builder?.setNegativeButton(R.string.ai_no) { dialog, which ->
            dialog.dismiss()
            finishAffinity()
        }

        var dialog = builder?.create()
        dialog?.show()
    }

    fun getPlayer(): Players {
        return model.players[TURN - 1]
    }

    fun nextTurn() {
        TURN++
        if (TURN == 5) TURN = 1
        model.skipsInRow = 0
    }

    fun onSkip() {
        Log.d(TAG, "onSkip()")
        onDiscUnselected()
        TURN++
        if (TURN == 5) TURN = 1
        model.skipsInRow++
        if (model.skipsInRow == 4) {
            showGameOverDialog()
        }
    }
}
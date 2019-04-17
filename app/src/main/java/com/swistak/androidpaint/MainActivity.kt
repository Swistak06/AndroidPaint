package com.swistak.androidpaint

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import yuku.ambilwarna.AmbilWarnaDialog






class MainActivity : AppCompatActivity() {

    val REQUEST_WRITE_EXTERNAL = 1
    private lateinit var embossMenuItem : MenuItem
    private lateinit var blurMenuItem : MenuItem


    fun changeUndoArrowColor(){
        if(paintView.getPaths().size  == 0)
            UndoButton.setIcon(R.drawable.undo)
    }

    fun switchBrushAndEraserIcons( isEraserEnabled : Boolean){
        if(isEraserEnabled)
            EraserBrushButton.setIcon(R.drawable.eraser)
        else
            EraserBrushButton.setIcon(R.drawable.brush)
    }

    fun hideMenu(){
        right_menu.collapse()
    }

    private fun showClearDialog(){
        val dial = AlertDialog.Builder(this)

        dial.setTitle("Warning!")
        dial.setMessage("Are you sure to clear board?")
        dial.setPositiveButton("Clear"){
            _,_ ->
            paintView.clear()
            UndoButton.setIcon(R.drawable.undo_grey)
        }
        dial.setNegativeButton("Cancel"){
                dialog, _ -> dialog.cancel()
        }
        dial.show()
    }
    private fun showHelpDialog(){
        val dial = AlertDialog.Builder(this)

        dial.setTitle("Help")
        dial.setMessage("You can clear the board by shaking your smartphone!")
        dial.setCancelable(true)
        dial.setPositiveButton("Got it"){
            dialog,_ -> dialog.cancel()
        }
        dial.show()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val paint = paintView
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        paintView.setCurrentActivity(this)
        paint.init(metrics)
        setUpButtonListeners()
    }

    private fun setUpButtonListeners(){
        UndoButton.setOnClickListener {
            paintView.undo()
            if(paintView.getPaths().size == 0)
                UndoButton.setIcon(R.drawable.undo_grey)
        }

        ClearButton.setOnClickListener{
            showClearDialog()
        }

        SaveButton.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_EXTERNAL
                )
            }
            showSavingFileDialog()
        }

        EraserBrushButton.setOnClickListener {
            paintView.switchBetweenBrushAndEraser()
        }

        ColorPickButton.setOnClickListener {
            openColorPicker()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.top_menu, menu)
        embossMenuItem = menu!!.findItem(R.id.emboss)
        blurMenuItem = menu.findItem(R.id.blur)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.normal -> {
                paintView.normal()
                embossMenuItem.isChecked = false
                blurMenuItem.isChecked = false
                return true
            }
            R.id.emboss -> {
                item.isChecked = !item.isChecked
                blurMenuItem.isChecked = false
                paintView.emboss()
                return true
            }
            R.id.blur -> {
                item.isChecked = !item.isChecked
                embossMenuItem.isChecked = false
                paintView.blur()
                return true
            }
            R.id.clear -> {
                showBrushSizeDialog()
                return true
            }
            R.id.help ->{
                showHelpDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showBrushSizeDialog(){
        val dial = AlertDialog.Builder(this)
        val seek = SeekBar(this);
        dial.setMessage("Brush size: ")
        seek.max = 100

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                //dial.setMessage("Progress : $i")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        dial.setTitle("Set brush size")
        dial.setView(seek)
        dial.setCancelable(true)
        dial.setPositiveButton("Change"){
            _,_ ->
            //changeSizeMethod
        }
        dial.setNegativeButton("Calncel"){
            dialog,_ -> dialog.cancel()
        }

        dial.show()
    }

    private fun showSavingFileDialog(){
        var fileName: String
        val builder = AlertDialog.Builder(this)
        builder.setTitle("File name")

        val input = EditText(this)

        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) {
                _, _ -> fileName = input.text.toString()
                paintView.saveAsImage(fileName)
            Toast.makeText(this,"Save completed",Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun openColorPicker() {
        val colorPicker = AmbilWarnaDialog(this, paintView.getCurrentBrushColor(), object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                paintView.setBrushColor(color)
            }
        })
        colorPicker.show()
    }


}

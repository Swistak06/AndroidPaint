package com.swistak.androidpaint

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.EditText




class MainActivity : AppCompatActivity() {

    val REQUEST_WRITE_EXTERNAL = 1


    fun changeUndoArrowColor(){
        if(paintView.getPaths().size  == 0)
            UndoButton.setIcon(R.drawable.undo)
    }

    fun switchBrushAndEraserIcons( isEraserEnabled : Boolean){
        if(isEraserEnabled)
            EraserBrushButton.setIcon(R.drawable.brush)
        else
            EraserBrushButton.setIcon(R.drawable.eraser)
    }

    fun hideMenu(){
        right_menu.collapse()
    }

    var dialogClickListener: DialogInterface.OnClickListener =
        DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    paintView.clear()
                    UndoButton.setIcon(R.drawable.undo_grey)
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
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
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
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
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.normal -> {
                paintView.normal()
                return true
            }
            R.id.emboss -> {
                paintView.emboss()
                return true
            }
            R.id.blur -> {
                paintView.blur()
                return true
            }
            R.id.clear -> {
                Log.d("kappa","Clear button clicked")
                paintView.clear()
                return true
            }
            R.id.undo ->{
                paintView.undo()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showSavingFileDialog(){
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
}

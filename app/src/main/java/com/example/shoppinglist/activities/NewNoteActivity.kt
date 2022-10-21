package com.example.shoppinglist.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.method.TextKeyListener.clear
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.ActivityNewNoteBinding
import com.example.shoppinglist.entities.NoteItem
import com.example.shoppinglist.fragments.NoteFragment
import com.example.shoppinglist.utils.HtmlManager
import com.example.shoppinglist.utils.MyTouchListener
import com.example.shoppinglist.utils.TimeManager
import com.example.shoppinglist.utils.TimeManager.getCurrentTime
import java.text.SimpleDateFormat
import java.util.*

class NewNoteActivity : AppCompatActivity() {
    private lateinit var defPref: SharedPreferences
    lateinit var binding: ActivityNewNoteBinding
    private var note: NoteItem? = null
    private  var pref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewNoteBinding.inflate(layoutInflater)
        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())
        setContentView(binding.root)

        actionBarSettings()
        getNote()
        init()
        setTextSize()
        onClickColorPicker()
        actionMenuCallback()
    }
    private fun onClickColorPicker() = with(binding){
        imBlack.setOnClickListener{
            setColorForSelectedText(R.color.picker_black)
        }
        imBlue.setOnClickListener{
            setColorForSelectedText(R.color.picker_blue)
        }
        imRed.setOnClickListener{
            setColorForSelectedText(R.color.picker_red)
        }
        imOrange.setOnClickListener{
            setColorForSelectedText(R.color.picker_orange)
        }
        imYellow.setOnClickListener{
            setColorForSelectedText(R.color.picker_yellow)
        }
        imGreen.setOnClickListener{
            setColorForSelectedText(R.color.picker_green)
        }
    }

    private fun init(){
        binding.colorPicker.setOnTouchListener(MyTouchListener())
        pref = PreferenceManager.getDefaultSharedPreferences(this)
    }
    private fun getNote(){
        val sNote = intent.getSerializableExtra(NoteFragment.NEW_NOTE_KEY)
        if(sNote != null) {
            note = sNote as NoteItem
            fillNote()
        }
    }
    private fun fillNote() = with(binding) {
        edTitle.setText(note?.title)
        edDescription.setText(HtmlManager.getFromHtml(note?.content!!).trim())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.new_note_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_save){
            setMainResult()

        } else if (item.itemId == androidx.appcompat.R.id.home){
            finish()
        } else if (item.itemId == R.id.id_bold) {
            setBoldForSelectedText()
        } else if (item.itemId == R.id.id_colorPicker) {
            if(binding.colorPicker.isShown){
                closeColorPicker()
            } else {
                openColorPicker()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setBoldForSelectedText() = with(binding){
        val startPos = edDescription.selectionStart
        val endPos = edDescription.selectionEnd

        val style = edDescription.text.getSpans(startPos, endPos, StyleSpan::class.java)
        var boldStyle: StyleSpan? = null
        if(style.isNotEmpty()){
            edDescription.text.removeSpan(style[0])
        } else {
          boldStyle = StyleSpan(Typeface.BOLD)
        }
        edDescription.text.setSpan(boldStyle, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        edDescription.text.trim()
        edDescription.setSelection(startPos)

    }

    private fun setColorForSelectedText(colorId:Int) = with(binding){
        val startPos = edDescription.selectionStart
        val endPos = edDescription.selectionEnd

        val style = edDescription.text.getSpans(startPos, endPos, ForegroundColorSpan::class.java)

        if(style.isNotEmpty()) edDescription.text.removeSpan(style[0])

        edDescription.text.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@NewNoteActivity, colorId)),
                startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        edDescription.text.trim()
        edDescription.setSelection(startPos)

    }

    private fun setMainResult(){
        var editState = "new"
        val tempNote: NoteItem? = if (note == null){
            createNewNote()

        } else {
            editState = "update"
            updateNote()
        }
        val i = Intent().apply {
            putExtra(NoteFragment.NEW_NOTE_KEY, tempNote)
            putExtra(NoteFragment.EDIT_STATE_KEY, editState)
        }
        setResult(RESULT_OK, i)
        finish()
    }
    private fun updateNote(): NoteItem? = with(binding){
        return note?.copy(
            title = edTitle.text.toString(),
            content = HtmlManager.toHtml(edDescription.text)
        )

    }
    private fun createNewNote(): NoteItem{
        return NoteItem(
        null,
        binding.edTitle.text.toString(),
            HtmlManager.toHtml(binding.edDescription.text),
            TimeManager.getCurrentTime(),
            "")
    }


    private fun actionBarSettings(){
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }
    private fun openColorPicker(){
        binding.colorPicker.visibility = View.VISIBLE
        val openAnim = AnimationUtils.loadAnimation(this, R.anim.open_color_picker)
        binding.colorPicker.startAnimation(openAnim)
    }
    private fun closeColorPicker(){
        val openAnim = AnimationUtils.loadAnimation(this, R.anim.close_color_picker)
        openAnim.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.colorPicker.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })
        binding.colorPicker.startAnimation(openAnim)
    }
    private fun actionMenuCallback(){
        val actionCallback = object : ActionMode.Callback{
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menu?.clear()
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menu?.clear()
                return true
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {

            }

        }
        binding.edDescription.customSelectionActionModeCallback = actionCallback
    }
    private fun setTextSize() = with(binding){
        edTitle.setTextSize(pref?.getString("title_size_key","16"))
        edDescription.setTextSize(pref?.getString("content_size_key","14"))
    }
    private fun EditText.setTextSize(size: String?){
       if (size != null) this.textSize = size.toFloat()
    }
    private fun getSelectedTheme():Int{
        return if (defPref.getString("theme_key", "blue") == "blue"){
            R.style.Theme_NewNoteBlue
        }else {
            R.style.Theme_NewNoteGreen
        }
    }

}
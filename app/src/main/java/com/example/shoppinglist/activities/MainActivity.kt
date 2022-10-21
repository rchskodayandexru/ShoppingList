package com.example.shoppinglist.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.ActivityMainBinding
import com.example.shoppinglist.dialogs.NewListDialog
import com.example.shoppinglist.fragments.FragmentManager
import com.example.shoppinglist.fragments.NoteFragment
import com.example.shoppinglist.fragments.ShopListNameFragment
import com.example.shoppinglist.settings.SettingsActivity

class MainActivity : AppCompatActivity(), NewListDialog.Listener {
    lateinit var binding: ActivityMainBinding
    private lateinit var defPref: SharedPreferences
    private var currentMenuItemId = R.id.shop_list
    private var currentTheme = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = defPref.getString("theme_key", "blue").toString()
        setTheme(getSelectedTheme())
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setBottomNavListener()
    }
    private  fun setBottomNavListener(){
        binding.bNav.setOnItemSelectedListener {
            when (it.itemId){
                R.id.settings->{
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.notes->{
                    currentMenuItemId = R.id.notes
                    FragmentManager.setFragment(NoteFragment.newInstance(), this)

                }
                R.id.shop_list->{
                    currentMenuItemId = R.id.shop_list
                    FragmentManager.setFragment(ShopListNameFragment.newInstance(), this)
                }
                R.id.new_item->{
                    //NewListDialog.showDialog(this,this)

                    FragmentManager.currentFrag?.onClickNew()

                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bNav.selectedItemId = currentMenuItemId
        if(defPref.getString("theme_key", "blue") != currentTheme) recreate()
    }
    private fun getSelectedTheme():Int{
        return if (defPref.getString("theme_key", "blue") == "blue"){
            R.style.Theme_ShoppingListBlue
        }else {
            R.style.Theme_ShoppingListGreen
        }
    }
    override fun onClick(name: String) {
        
    }
}
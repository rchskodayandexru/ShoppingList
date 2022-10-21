package com.example.shoppinglist.fragments

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.shoppinglist.R
import com.example.shoppinglist.activities.MainActivity

object FragmentManager {
    var currentFrag:BaseFragment? = null
    fun setFragment(newFrag: BaseFragment, activity: AppCompatActivity){
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.placeHolder, newFrag)
        transaction.commit()
        currentFrag = newFrag
    }
}
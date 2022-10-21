package com.example.shoppinglist.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.view.MenuItemCompat.expandActionView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.ActivityShopListBinding
import com.example.shoppinglist.db.MainViewModel
import com.example.shoppinglist.db.ShopListItemAdapter
import com.example.shoppinglist.dialogs.EditListItemDialog
import com.example.shoppinglist.entities.LibraryItem
import com.example.shoppinglist.entities.ShopListItem
import com.example.shoppinglist.entities.ShopListNameItem
import com.example.shoppinglist.utils.ShareHelper

class ShopListActivity : AppCompatActivity(), ShopListItemAdapter.Listener {
    private lateinit var binding: ActivityShopListBinding
    private var shopListNameItem: ShopListNameItem? = null
    private lateinit var saveItem: MenuItem
    private var edItem: EditText?=null
    private var adapter: ShopListItemAdapter? = null
    private lateinit var textWatcher: TextWatcher


    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.MainViewModelFactory((applicationContext as MainApp).database)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        listItemObserver()
        initRcView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shop_list_menu, menu)
        saveItem = menu?.findItem(R.id.save_item)!!
        val newItem = menu?.findItem(R.id.new_item)
        edItem = newItem.actionView.findViewById(R.id.edNewShopItem) as EditText
        newItem.setOnActionExpandListener(expandActionView())
        saveItem.isVisible =false
        textWatcher = textWatcher()
        return true
    }
    private  fun textWatcher(): TextWatcher{
        return object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                mainViewModel.getAllLibraryItem("%$p0%")
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.save_item){
            addNewShopItem(edItem?.text.toString())
        } else if(item.itemId == R.id.delete_list){
            mainViewModel.deleteShopList(shopListNameItem?.id!!, true)
            finish()
        } else if(item.itemId == R.id.clear_list){
            mainViewModel.deleteShopList(shopListNameItem?.id!!, false)

        }
        else if(item.itemId == R.id.share_list){
            startActivity(Intent.createChooser(
                ShareHelper.shareShopList(adapter?.currentList!!, shopListNameItem?.name!!),
                "Share by"
            ))

        }

        return super.onOptionsItemSelected(item)
    }
    private fun addNewShopItem(name: String){
        if (name.isEmpty()) return
        val item = ShopListItem(
            null,
                name,
            null,
            false,
            shopListNameItem?.id!!,
            0
        )
        edItem?.setText("")
        mainViewModel.insertShopItem(item)
    }
    private fun listItemObserver(){
        mainViewModel.getAllItemsFromList(shopListNameItem?.id!!).observe(this,{
            adapter?.submitList(it)
            binding.tvEmpty.visibility = if(it.isNotEmpty()){
                View.GONE
            } else {
                View.VISIBLE
            }
        })
    }

    private fun libraryItemObserver(){
       mainViewModel.libraryItems.observe(this,{
           val tempShopList = ArrayList<ShopListItem>()
           it.forEach{item->
               val shopItem = ShopListItem(
                   item.id,
                   item.name,
               "",
                   false,
                   0,
                   1
               )
                tempShopList.add(shopItem)
           }
           adapter?.submitList(tempShopList)
           binding.tvEmpty.visibility = if(it.isNotEmpty()){
               View.GONE
           } else {
               View.VISIBLE
           }
       })
    }

    private fun initRcView() = with(binding){
       adapter = ShopListItemAdapter(this@ShopListActivity)
        rcView.layoutManager = LinearLayoutManager(this@ShopListActivity)
        rcView.adapter = adapter
    }
    private fun expandActionView(): MenuItem.OnActionExpandListener{
        return object : MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                saveItem.isVisible =true
                edItem?.addTextChangedListener(textWatcher)
                libraryItemObserver()
                mainViewModel.getAllItemsFromList(shopListNameItem?.id!!).removeObservers(this@ShopListActivity)
                mainViewModel.getAllLibraryItem("%%")
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                saveItem.isVisible =false
                edItem?.removeTextChangedListener(textWatcher)
                invalidateOptionsMenu()
                mainViewModel.libraryItems.removeObservers(this@ShopListActivity)
                edItem?.setText("")
                listItemObserver()
                return true
            }

        }
    }
    private fun init(){
        shopListNameItem = intent.getSerializableExtra(SHOP_LIST_NAME) as ShopListNameItem

    }
    companion object{
        const val SHOP_LIST_NAME = "shop_list_name"
    }

    override fun onClickItem(shopListItem: ShopListItem, state: Int) {
        when(state){
            ShopListItemAdapter.CHECK_BOX -> mainViewModel.updateListItem(shopListItem)
            ShopListItemAdapter.EDIT -> editListItem(shopListItem)
            ShopListItemAdapter.EDIT_LIBRARY_ITEM -> editLibraryItem(shopListItem)
            ShopListItemAdapter.ADD -> addNewShopItem(shopListItem.name)
            ShopListItemAdapter.DELETE_LIBRARY_ITEM -> {
                mainViewModel.deleteLibraryItem(shopListItem.id!!)
                mainViewModel.getAllLibraryItem("%${edItem?.text.toString()}%")
            }
        }
    }
    private fun editListItem(item:ShopListItem){
        EditListItemDialog.showDialog(this, item, object : EditListItemDialog.Listener{
            override fun onClick(item: ShopListItem) {
                mainViewModel.updateListItem(item)
            }

        } )

    }
    private fun editLibraryItem(item:ShopListItem){
        EditListItemDialog.showDialog(this, item, object : EditListItemDialog.Listener{
            override fun onClick(item: ShopListItem) {
                mainViewModel.updateLibraryItem(LibraryItem(item.id, item.name))
                mainViewModel.getAllLibraryItem("%${edItem?.text.toString()}%")
            }

        } )

    }
private fun saveItemCount(){
    var checkedItemCount = 0
    adapter?.currentList?.forEach{
        if (it.itemChecked) checkedItemCount++
    }
    val tempShopListItem  = shopListNameItem?.copy(
        allItemCounter = adapter?.itemCount!!,
        checkedItemsCounter = checkedItemCount
    )
    mainViewModel.updateListName(tempShopListItem!!)
}
    override fun onBackPressed() {
        saveItemCount()
        super.onBackPressed()
    }

}
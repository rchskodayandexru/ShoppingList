package com.example.shoppinglist.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppinglist.activities.MainApp
import com.example.shoppinglist.activities.ShopListActivity
import com.example.shoppinglist.databinding.FragmentShopListNamesBinding
import com.example.shoppinglist.db.MainViewModel
import com.example.shoppinglist.db.ShopListNameAdapter
import com.example.shoppinglist.dialogs.DeleteDialog
import com.example.shoppinglist.dialogs.NewListDialog
import com.example.shoppinglist.entities.ShopListNameItem
import com.example.shoppinglist.utils.TimeManager


class ShopListNameFragment : BaseFragment(), ShopListNameAdapter.Listener {
    private lateinit var binding: FragmentShopListNamesBinding
    private lateinit var adapter: ShopListNameAdapter


    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModel.MainViewModelFactory((context?.applicationContext as MainApp).database)
    }
    override fun onClickNew() {
        //startActivity(Intent(activity, NewNoteActivity::class.java))
        NewListDialog.showDialog(activity as AppCompatActivity, object : NewListDialog.Listener{
            override fun onClick(name: String) {
                val shopListName = ShopListNameItem(
                    null,
                    name,
                    TimeManager.getCurrentTime(),
                    0,
                    0,
                    ""
                )
                mainViewModel.insertShopListName(shopListName)
            }

        }, "")


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShopListNamesBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        observer()
    }

    private fun initRcView() = with(binding){
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = ShopListNameAdapter(this@ShopListNameFragment)
        rcView.adapter = adapter
    }

    private fun observer(){
        mainViewModel.allShopListNames.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
    }




    companion object {

        @JvmStatic
        fun newInstance() = ShopListNameFragment()

    }

    override fun deleteItem(id: Int) {
        DeleteDialog.showDialog(context as AppCompatActivity, object : DeleteDialog.Listener{
            override fun onClick() {
                mainViewModel.deleteShopList(id, true)
            }
        })
    }

    override fun editItem(shopListNameItem: ShopListNameItem) {
        NewListDialog.showDialog(activity as AppCompatActivity, object : NewListDialog.Listener{
            override fun onClick(name: String) {
                mainViewModel.updateListName(shopListNameItem.copy(name = name))
            }

        }, shopListNameItem.name)
    }

    override fun onClickItem(shopListNameItem: ShopListNameItem) {
        val i = Intent(activity, ShopListActivity:: class.java).apply {
            putExtra(ShopListActivity.SHOP_LIST_NAME, shopListNameItem)
        }
        startActivity(i)
    }
}
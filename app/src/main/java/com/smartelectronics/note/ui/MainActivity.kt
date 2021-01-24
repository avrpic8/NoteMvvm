package com.smartelectronics.note.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.smartelectronics.note.R
import com.smartelectronics.note.adapter.NoteAdapter
import com.smartelectronics.note.db.Note
import com.smartelectronics.note.db.NoteViewModel
import com.smartelectronics.note.util.Constant
import com.smartelectronics.note.util.PrefUtil
import com.smartelectronics.note.util.showMessage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tool_bar.*


class MainActivity : AppCompatActivity() {

    private var changeRecyclerMode:Boolean = true

    private lateinit var myNoteList:RecyclerView
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.list_type ->{

                changeRecyclerMode = !changeRecyclerMode
                PrefUtil.saveToPrefs(this, "listType", changeRecyclerMode)
                if (changeRecyclerMode)
                    myNoteList.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

                else
                    myNoteList.layoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)


                if (changeRecyclerMode) item.setIcon(R.drawable.ic_grid)
                else item.setIcon(R.drawable.ic_list)
                myNoteList.adapter = noteAdapter
                true
            }

            R.id.list_sort ->{}

            R.id.list_search ->{
                val searchView: SearchView = item.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        noteAdapter.filter.filter(query)
                        return false
                    }
                    override fun onQueryTextChange(newText: String?): Boolean {
                        noteAdapter.filter.filter(newText)
                        return false
                    }
                })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == Constant.ADD_NOTE_REQUEST && resultCode == Activity.RESULT_OK){

            val title = data?.getStringExtra(Constant.EXTRA_TITLE)
            val body  = data?.getStringExtra(Constant.EXTRA_BODY)
            val cardColor = data?.getIntExtra(Constant.EXTRA_CARD_COLOR, 0)
            val notif = data?.getIntExtra(Constant.EXTRA_NOTIFICATION, Constant.DISABLE)
            val favorite = data?.getIntExtra(Constant.EXTRA_FAVORITE, Constant.DISABLE)
            val attachFile = data?.getIntExtra(Constant.EXTRA_ATTACH, Constant.DISABLE)
            val fontSize = data?.getIntExtra(Constant.EXTRA_FONT_SIZE, 18)
            val editedTime = data?.getStringExtra(Constant.EXTRA_EDIT_TIME)
            val photoPaths = data?.getSerializableExtra(Constant.EXTRA_IMAGE_PATHS) as ArrayList<String>
            val videoPaths = data?.getSerializableExtra(Constant.EXTRA_VIDEO_PATHS) as ArrayList<String>

            val newNote = Note(title!!,body!!,cardColor!!,
                               notif!!,favorite!!,attachFile!!,
                               fontSize!!,editedTime!!,photoPaths, videoPaths)
            noteViewModel.insert(newNote)
            showMessage("New note saved")
        }

        if(requestCode == Constant.EDIT_NOTE_REQUEST && resultCode == Activity.RESULT_OK){

            val id = data?.getIntExtra(Constant.EXTRA_ID, -1)
            if(id == -1){
                showMessage("Note Cant be updated")
                return
            }

            val title = data?.getStringExtra(Constant.EXTRA_TITLE)
            val body  = data?.getStringExtra(Constant.EXTRA_BODY)
            val cardColor = data?.getIntExtra(Constant.EXTRA_CARD_COLOR, 0)
            val notif = data?.getIntExtra(Constant.EXTRA_NOTIFICATION, Constant.DISABLE)
            val favorite = data?.getIntExtra(Constant.EXTRA_FAVORITE, Constant.DISABLE)
            val attachFile = data?.getIntExtra(Constant.EXTRA_ATTACH, Constant.DISABLE)
            val fontSize = data?.getIntExtra(Constant.EXTRA_FONT_SIZE, 18)
            val editedTime = data?.getStringExtra(Constant.EXTRA_EDIT_TIME)
            val photoPaths = data?.getSerializableExtra(Constant.EXTRA_IMAGE_PATHS) as ArrayList<String>
            val videoPaths = data?.getSerializableExtra(Constant.EXTRA_VIDEO_PATHS) as ArrayList<String>// check fo change getSerialize

            val updateNote = Note(id!!,title!!,body!!,
                                  cardColor!!,notif!!,favorite!!,
                                  attachFile!!,fontSize!!,editedTime!!,
                                  photoPaths, videoPaths)
            noteViewModel.update(updateNote)
            showMessage("Note updated")
        }
    }

    /// my methods
    private fun initViewModel(){

        noteViewModel = ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory(application)).get(NoteViewModel::class.java)

        noteViewModel.getAllNotes().observe(this, Observer {

            // update RecyclerView
            //noteAdapter.submitList(it)
            noteAdapter.setNote(it as ArrayList<Note>)
            myNoteList.scheduleLayoutAnimation()
        })
    }
    private fun initToolBar(){

        tool_bar.title = resources.getString(R.string.app_name)
        setSupportActionBar(tool_bar)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.lineColor)
            }
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
    private fun initRecyclerView(){

        myNoteList = findViewById(R.id.recycler_note_list)
        changeRecyclerMode = PrefUtil.getFromPrefs(this, "listType", false)

        if (changeRecyclerMode)
            myNoteList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        else
            myNoteList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        myNoteList.setHasFixedSize(false)
        noteAdapter = NoteAdapter(this)
        myNoteList.adapter = noteAdapter

        myNoteList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && btn_fab.visibility == View.VISIBLE) {
                    btn_fab.hide();
                } else if (dy < 0 && btn_fab.visibility != View.VISIBLE) {
                    btn_fab.show();
                }
            }
        })
        noteAdapter.setOnItemClickListener(object :NoteAdapter.OnItemClickListener{
            override fun onItemClicked(note: Note) {

                val intent = Intent(this@MainActivity, AddNoteActivity::class.java)
                with(intent){
                    putExtra(Constant.EXTRA_ID, note.id)
                    putExtra(Constant.EXTRA_TITLE, note.title)
                    putExtra(Constant.EXTRA_BODY, note.description)
                    putExtra(Constant.EXTRA_CARD_COLOR, note.backGroundColor)
                    putExtra(Constant.EXTRA_NOTIFICATION, note.pinedToStatusBar)
                    putExtra(Constant.EXTRA_FAVORITE, note.favorite)
                    putExtra(Constant.EXTRA_FONT_SIZE, note.fontSize)
                    putExtra(Constant.EXTRA_EDIT_TIME, note.editedTime)
                    putExtra(Constant.EXTRA_IMAGE_PATHS, note.imagesPath)
                    putExtra(Constant.EXTRA_VIDEO_PATHS, note.videosPath)
                }
                Log.i("test", note.toString())
                startActivityForResult(intent, Constant.EDIT_NOTE_REQUEST)
            }
        })

        val swipBackGround = ColorDrawable(Color.parseColor("#757575"))
        val deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)

        ItemTouchHelper(object : SimpleCallback(0, LEFT or RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { return false }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition
                val deletedNote: Note = noteAdapter.getNoteAt(position)

                // deleted note
                noteViewModel.delete(deletedNote)

                // undo deleted note
                Snackbar.make(viewHolder.itemView, "your note deleted.", Snackbar.LENGTH_SHORT)
                    .setAction("Undo") {
                        noteViewModel.insert(deletedNote)
                        showMessage("note added again")
                    }.show()
            }
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2

                if (changeRecyclerMode) {
                    if (dX > 0) {
                        swipBackGround.setBounds(itemView.left, itemView.top,
                                Math.round(dX), itemView.bottom)

                        deleteIcon.setBounds(itemView.left + iconMargin, itemView.top + iconMargin,
                            itemView.left + iconMargin + deleteIcon.intrinsicHeight,
                            itemView.bottom - iconMargin)
                    } else {
                        swipBackGround.setBounds(itemView.right + Math.round(dX), itemView.top,
                            itemView.right,
                            itemView.bottom)

                        deleteIcon.setBounds(itemView.right - iconMargin - deleteIcon.intrinsicWidth, itemView.top + iconMargin,
                            itemView.right - iconMargin,
                            itemView.bottom - iconMargin
                        )
                    }
                    swipBackGround.draw(c)
                    c.save()
                    if (dX > 0) c.clipRect(itemView.left, itemView.top, Math.round(dX), itemView.bottom)
                    else c.clipRect(itemView.right + Math.round(dX), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.draw(c)
                    c.restore()
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }).attachToRecyclerView(myNoteList)
    }
    private fun initFabButton(){

        btn_fab.setOnLongClickListener {

            val intent = Intent(this@MainActivity, AddNoteActivity::class.java)
            startActivityForResult(intent, Constant.ADD_NOTE_REQUEST)
            return@setOnLongClickListener true
        }
    }
    private fun init(){
        initViewModel()
        initToolBar()
        initFabButton()
        initRecyclerView()
    }
}

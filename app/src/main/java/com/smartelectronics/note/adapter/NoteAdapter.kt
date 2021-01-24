package com.smartelectronics.note.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.smartelectronics.note.R
import com.smartelectronics.note.db.Note
import com.smartelectronics.note.util.Constant
import kotlinx.android.synthetic.main.note_layout.view.*


class NoteAdapter(context:Context) : RecyclerView.Adapter<NoteAdapter.NoteHolder>(), Filterable{

    private var notes: ArrayList<Note> = arrayListOf()
    private var noteFiltered: ArrayList<Note> = arrayListOf()


    private lateinit var onItemClickListener: OnItemClickListener
    private var mContext: Context = context

    companion object{

        /*val DIFF_CALLBACK: DiffUtil.ItemCallback<Note> = object : DiffUtil.ItemCallback<Note>() {

            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id === newItem.id
            }
            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.title.equals(newItem.title) &&
                        oldItem.description.equals(newItem.description) &&
                        oldItem.backGroundColor === newItem.backGroundColor &&
                        oldItem.pinedToStatusBar == newItem.pinedToStatusBar &&
                        oldItem.favorite == newItem.favorite &&
                        oldItem.fontSize == newItem.fontSize &&
                        oldItem.editedTime == newItem.editedTime
            }
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {

        return NoteHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.note_layout, parent , false))
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {

        //val currentNote: Note = getItem(position)
        val currentNote: Note = noteFiltered[position]
        Log.i("test", currentNote.imagesPath?.toString())
        // set title , description and background color of notes
        holder.itemView.txt_title_note.text  = currentNote.title
        holder.itemView.txt_body_note.text   = currentNote.description
        holder.itemView.note_card.setCardBackgroundColor(currentNote.backGroundColor)

        // set note to notification
        if(currentNote.pinedToStatusBar == Constant.ENABLE) holder.itemView.img_pin_to_notif.visibility = View.VISIBLE
        if(currentNote.pinedToStatusBar == Constant.DISABLE) holder.itemView.img_pin_to_notif.visibility = View.GONE


        // set note favorite
        if(currentNote.favorite == Constant.ENABLE)
            holder.itemView.img_favorite.visibility = View.VISIBLE
        if(currentNote.favorite == Constant.DISABLE) holder.itemView.img_favorite.visibility = View.GONE

        // set note attach
        if(currentNote.attached == Constant.ENABLE)
            holder.itemView.img_attach.visibility = View.VISIBLE
        if(currentNote.attached == Constant.DISABLE) holder.itemView.img_attach.visibility = View.GONE

        // change text size of notes
        holder.itemView.txt_title_note.textSize = currentNote.fontSize.toFloat()
        holder.itemView.txt_body_note.textSize = currentNote.fontSize.toFloat() - 4

        holder.itemView.setOnClickListener {

            if(this.onItemClickListener != null && position != RecyclerView.NO_POSITION)
                onItemClickListener.onItemClicked(currentNote)
        }
    }

    override fun getItemCount(): Int {
        return noteFiltered.size
    }

    override fun getFilter(): Filter {
       return noteFilter
    }

    private val noteFilter: Filter = object :Filter(){

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val resultList = ArrayList<Note>()

            noteFiltered = if(constraint == null || constraint.isEmpty())
                notes
            else{
                val filterPattern = constraint.toString().toLowerCase().trim()
                for(item in noteFiltered){
                    if(item.title.toLowerCase().contains(filterPattern) || item.description.toLowerCase().contains(filterPattern))
                        resultList.add(item)
                }
                resultList
            }
            val result = FilterResults()
            result.values = noteFiltered
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            noteFiltered = results?.values as ArrayList<Note>
            notifyDataSetChanged()
        }
    }

    fun setNote(notes: ArrayList<Note>){
        this.notes = notes
        this.noteFiltered = notes
        notifyDataSetChanged()
    }

    fun getNoteAt(position: Int): Note{
        //return getItem(position)
        return noteFiltered[position]
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.onItemClickListener = listener
    }

    class NoteHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    interface OnItemClickListener{
        fun onItemClicked(note: Note)
    }
}
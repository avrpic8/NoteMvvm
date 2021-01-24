package com.smartelectronics.note.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.smartelectronics.note.R
import com.smartelectronics.note.util.Constant
import com.smartelectronics.note.util.StorageHelper
import kotlinx.android.synthetic.main.note_attachments.view.*


class NoteAttachmentsAdapter(context: Context): RecyclerView.Adapter<NoteAttachmentsAdapter.NoteAttachHolder>() {

    private val mContext: Context = context
    private var attachments: ArrayList<String> = arrayListOf()
    private lateinit var onItemClickListener: OnItemClickListener


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteAttachHolder {

        return NoteAttachHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.note_attachments, parent , false))
    }

    override fun getItemCount(): Int {
        return attachments.size
    }

    override fun onBindViewHolder(holder: NoteAttachHolder, position: Int){

        Glide.with(mContext).load(attachments[position])
            .apply(RequestOptions().centerCrop())
            .into(holder.itemView.img_photo)

        // Listener for open files when click
        holder.itemView.card_holder.setOnClickListener {
            onItemClickListener.onClicked(position)
        }

        // lister for delete files
        holder.itemView.card_holder.setOnLongClickListener {

            onItemClickListener.onLongClicked(position)
            return@setOnLongClickListener true
        }

        if(StorageHelper.getMimeType(attachments[position]) == Constant.MIME_TYPE_VIDEO){
            holder.itemView.img_play.visibility = View.VISIBLE
        }

        if( (StorageHelper.getMimeType(attachments[position]) == Constant.MIME_TYPE_IMAGE) or
            (StorageHelper.getMimeType(attachments[position]) == Constant.MIME_TYPE_AUDIO) or
            (StorageHelper.getMimeType(attachments[position]) == Constant.MIME_TYPE_SKETCH)){
            holder.itemView.img_play.visibility = View.GONE
        }
    }

    fun setAttachments(attachments: ArrayList<String>){
        this.attachments = attachments
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.onItemClickListener = listener
    }

    class NoteAttachHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    interface OnItemClickListener{
        fun onClicked(position: Int)
        fun onLongClicked(position: Int)
    }
}
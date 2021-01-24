package com.smartelectronics.note.ui


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.smartelectronics.note.R
import com.smartelectronics.note.adapter.NoteAttachmentsAdapter
import com.smartelectronics.note.util.Constant
import com.smartelectronics.note.util.IntentChecker
import com.smartelectronics.note.util.StorageHelper
import com.smartelectronics.note.util.showMessage
import kotlinx.android.synthetic.main.activity_add_note.*
import kotlinx.android.synthetic.main.font_size_dialog.view.*
import kotlinx.android.synthetic.main.tool_bar.tool_bar
import org.michaelbel.bottomsheet.BottomSheet
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddNoteActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar

    private lateinit var edtNoteTitle: EditText
    private lateinit var edtNoteBody: EditText

    private lateinit var txtEditedTime: TextView
    private lateinit var txtPicHeader: TextView
    private lateinit var txtVidHeader: TextView

    private var selectedColor:Int = R.color.colorPrimaryDark
    private var pinedToStatusBar: Int = Constant.DISABLE
    private var favorite: Int = Constant.DISABLE
    private var fontSize: Int = 10
    private var prevProcess: Int = 0

    // photo
    private lateinit var currentPhotoPath: String
    private var photoFilePaths: ArrayList<String> = arrayListOf()
    private lateinit var mPhotoFile: File
    private lateinit var picturesAttachList: RecyclerView
    private lateinit var picturesAttachAdapter: NoteAttachmentsAdapter

    // video
    private var videoFilePaths: ArrayList<String> = arrayListOf()
    private lateinit var mVideoFile: File
    private lateinit var videosAttachList: RecyclerView
    private lateinit var videosAttachAdapter: NoteAttachmentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.color_choose){

            MaterialDialog(this).show {
                title(R.string.title_color)
                colorChooser(ColorPalette.Primary,
                    ColorPalette.PrimarySub) { _, color ->
                    selectedColor = color
                    changeTheme(selectedColor)
                }
                positiveButton(R.string.select)
                negativeButton(android.R.string.cancel)
            }
        }

        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {

            when(requestCode){
                Constant.IMAGE_CAPTURE_REQUEST ->{
                    photoFilePaths.add(mPhotoFile.path)
                    Log.i("video", photoFilePaths.toString())
                }
                Constant.GALLERY_PHOTO_REQUEST ->{
                    val selectedImage = data!!.data
                    val name          = StorageHelper.getNameFromUri(this, selectedImage)
                    val storagePath   = StorageHelper.createExternalStoragePrivateFile(this, selectedImage, StorageHelper.getFileExtension(name))

                    photoFilePaths.add(storagePath)
                    Log.i("video", photoFilePaths.toString())
                }
                Constant.VIDEO_CAPTURE_REQUEST ->{
                    videoFilePaths.add(mVideoFile.path)
                    videosAttachAdapter.setAttachments(videoFilePaths)
                    Log.i("video", videoFilePaths.toString())
                }
            }

            picturesAttachAdapter.setAttachments(photoFilePaths)
            checkHeaders()
        }
    }

    /// my methods
    @SuppressLint("NewApi")
    private fun initToolBar(){

        tool_bar.title = ""
        setSupportActionBar(tool_bar)
        tool_bar.setNavigationOnClickListener {
            saveNote()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_chevron_left)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
    private fun initEditText(){

        edtNoteTitle = findViewById(R.id.edt_note_title)
        edtNoteBody  = findViewById(R.id.edt_note_body)
    }
    private fun initTextViews(){
        txtEditedTime = findViewById(R.id.txt_edited_date)
        txtPicHeader  = findViewById(R.id.txt_pictures_header)
        txtVidHeader  = findViewById(R.id.txt_videos_header)
    }
    private fun initRecyclerView(){

        /// =============== Pictures list
        picturesAttachList = findViewById(R.id.recycler_pictures_attach)
        picturesAttachList.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        picturesAttachList.setHasFixedSize(false)

        picturesAttachAdapter = NoteAttachmentsAdapter(this)
        picturesAttachList.adapter = picturesAttachAdapter

        picturesAttachAdapter.setOnItemClickListener(object: NoteAttachmentsAdapter.OnItemClickListener{
            override fun onClicked(position: Int) {
                /*val uri = getSharableUri(photoFilePaths[position])
                val cR = applicationContext.contentResolver
                var mimeType = cR.getType(uri)
                if (mimeType == null) mimeType = getMimeType(uri.toString())*/
                openFile(getSharableUri(photoFilePaths[position]))
            }
            override fun onLongClicked(position: Int) {
                openOptionsDialog(photoFilePaths[position], position)
            }
        })

        /// =============== Videos list
        videosAttachList = findViewById(R.id.recycler_videos_attach)
        videosAttachList.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        picturesAttachList.setHasFixedSize(false)

        videosAttachAdapter = NoteAttachmentsAdapter(this)
        videosAttachList.adapter = videosAttachAdapter

        videosAttachAdapter.setOnItemClickListener(object: NoteAttachmentsAdapter.OnItemClickListener{
            override fun onClicked(position: Int) {
                openFile(getSharableUri(videoFilePaths[position]))
            }
            override fun onLongClicked(position: Int) {
                openOptionsDialog(videoFilePaths[position], position)
            }
        })
    }
    private fun changeTheme(color: Int){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        }
        edt_note_title.setTextColor(color)
        edt_note_title.setHintTextColor(color)

        edt_note_body.setHintTextColor(color)
        edt_note_body.setTextColor(color)

        txtEditedTime.setTextColor(color)
        txtPicHeader.setTextColor(color)
        txtVidHeader.setTextColor(color)
    }
    private fun bottomSheetInit(){

        val itemMore = arrayOf("Favorite", "UnLock", "Notification",
                            "Font Size", "Share", "Copy to...", "Read mode")
        val iconMore = arrayOf(
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_not),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_lock_open),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_notif_on),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_font_size),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_share),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_copy_to_clip_board),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_read_onlyy)
        )

        img_more.setOnClickListener {
            val builder:BottomSheet.Builder = BottomSheet.Builder(this).apply {
                setTitle("Choose Actions...")
                setItems(itemMore, iconMore, DialogInterface.OnClickListener { dialog, which ->

                    when(which){
                        0 -> {
                            favoriteNote()
                        }
                        1 -> { }
                        2 -> {
                            enableNotification(edtNoteTitle.text.toString(),
                                edtNoteBody.text.toString(),
                                intent.getIntExtra(Constant.EXTRA_ID, -1))
                        }
                        3 -> {
                            setFontSize()
                        }
                        4 -> {
                            shareNoteText()
                        }
                        5 -> {
                            copyToClipBoard()
                        }
                        6 -> {
                            readOnlyScreen()
                        }
                    }
                })
                setContentType(BottomSheet.GRID)
            }
            builder.show()
        }


        // attach to note
        val itemAttaches = arrayOf("Camera", "Video", "Recorder")
        val iconAttaches = arrayOf(
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_camera),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_videocam),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_voice))

        img_add_to_text.setOnClickListener {
            val builder:BottomSheet.Builder = BottomSheet.Builder(this).apply {
                setTitle("Choose Actions...")
                setItems(itemAttaches, iconAttaches, DialogInterface.OnClickListener { dialog, which ->

                    when(which){
                        0 -> {selectImage()}
                        1 -> {
                            Dexter.withContext(this@AddNoteActivity)
                                .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
                                .withListener(object: MultiplePermissionsListener{
                                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                                        if(report!!.areAllPermissionsGranted())
                                            dispatchTakeVideoIntent()
                                    }

                                    override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, token: PermissionToken?) {
                                        token?.continuePermissionRequest()
                                    }
                                })
                                .withErrorListener { error -> showMessage("Error occurred!") }
                                .onSameThread()
                                .check()
                        }
                        2 -> { showMessage("Clicked")}
                    }
                })
                setContentType(BottomSheet.GRID)
            }
            builder.show()
        }
    }
    private fun saveNote() {

        val noteTitle  = edtNoteTitle.text.toString()
        val noteBody   = edtNoteBody.text.toString()

        if (noteTitle.trim().isEmpty() || noteBody.trim().isEmpty()) {
            Toast.makeText(this, "Please inset title and your note", Toast.LENGTH_SHORT).show()
            return
        }

        val data = Intent().apply {
            putExtra(Constant.EXTRA_TITLE, noteTitle)
            putExtra(Constant.EXTRA_BODY, noteBody)
            putExtra(Constant.EXTRA_CARD_COLOR, selectedColor)
            putExtra(Constant.EXTRA_NOTIFICATION, pinedToStatusBar)
            putExtra(Constant.EXTRA_FAVORITE, favorite)
            putExtra(Constant.EXTRA_ATTACH, hasAttachFile())
            putExtra(Constant.EXTRA_FONT_SIZE, fontSize)
            putExtra(Constant.EXTRA_EDIT_TIME, getEditedTime())
            putExtra(Constant.EXTRA_IMAGE_PATHS, photoFilePaths)
            putExtra(Constant.EXTRA_VIDEO_PATHS, videoFilePaths)
            Log.i("camera", photoFilePaths.toString())
        }
        val id = intent.getIntExtra(Constant.EXTRA_ID, -1)
        if (id != -1) data.putExtra(Constant.EXTRA_ID, id)

        setResult(RESULT_OK, data)
        finish()
    }
    private fun getDataForEditNote(){
        val data = intent
        if(data.hasExtra(Constant.EXTRA_ID)) {

            edtNoteTitle.setText(data.getStringExtra(Constant.EXTRA_TITLE))
            edtNoteBody.setText(data.getStringExtra(Constant.EXTRA_BODY))
            selectedColor      = data.getIntExtra(Constant.EXTRA_CARD_COLOR, selectedColor)
            pinedToStatusBar   = data.getIntExtra(Constant.EXTRA_NOTIFICATION, 1)
            favorite           = data.getIntExtra(Constant.EXTRA_FAVORITE, 1)
            fontSize           = data.getIntExtra(Constant.EXTRA_FONT_SIZE, 18)
            txtEditedTime.text = "Edited " + data.getStringExtra(Constant.EXTRA_EDIT_TIME)
            photoFilePaths     = data.getSerializableExtra(Constant.EXTRA_IMAGE_PATHS) as ArrayList<String>
            videoFilePaths     = data.getSerializableExtra(Constant.EXTRA_VIDEO_PATHS) as ArrayList<String>
            edtNoteTitle.textSize = fontSize.toFloat()
            edtNoteBody.textSize = fontSize.toFloat() - 4

            changeTheme(selectedColor)
            picturesAttachAdapter.setAttachments(photoFilePaths)
            videosAttachAdapter.setAttachments(videoFilePaths)
        }
    }
    private fun favoriteNote(){
        if(favorite == Constant.ENABLE) {
            favorite = Constant.DISABLE
            return
        }
        if(favorite == Constant.DISABLE) favorite = Constant.ENABLE
    }
    private fun hasAttachFile(): Int{
        return if(photoFilePaths.size > 0)
            Constant.ENABLE
        else
            Constant.DISABLE
    }
    private fun enableNotification(title: String, body: String, id:Int){

        val builder = NotificationCompat.Builder(this).apply {
            setSmallIcon(R.drawable.ic_feather)
            setContentTitle(title)
            setContentText(body)
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if(pinedToStatusBar == Constant.DISABLE){
            notificationManager.notify(id, builder.build())
            pinedToStatusBar = Constant.ENABLE
        }else{
            notificationManager.cancel(id)
            pinedToStatusBar = Constant.DISABLE
        }

    }
    private fun setFontSize(){

        val dialog = MaterialDialog(this).show {
            customView(R.layout.font_size_dialog, scrollable = true)
            positiveButton(text = "DONE")
            negativeButton(text = "CANCEL")
        }

        val view = dialog.getCustomView().apply {
            txt_font_size.text = "$fontSize sp"
            seek_bar_font.progress = fontSize
            seek_bar_font.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                    val diff = progress - prevProcess
                    if(diff > 0) fontSize ++
                    if(diff < 0) fontSize --
                    if(progress == 0) fontSize = 10
                    if(progress == 25) fontSize = 25
                    prevProcess = progress

                    edtNoteTitle.setTextSize(fontSize.toFloat())
                    edtNoteBody.setTextSize(fontSize.toFloat() - 2)
                    txt_font_size.text = "$fontSize sp"

                    Log.i("test", "$progress   $fontSize")
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }
    private fun shareNoteText(){

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${edtNoteTitle.text}" + "\n" + "${edtNoteBody.text}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
    private fun copyToClipBoard(){
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Your text","${edtNoteTitle.text}" + "\n" +"${edtNoteBody.text}")
        clipboard.setPrimaryClip(clip)

        Snackbar.make(parent_layout, "copied to clipboard", Snackbar.LENGTH_LONG).show()
    }
    private fun readOnlyScreen(){

        supportActionBar?.hide()
        edtNoteTitle.isEnabled = false
        edtNoteBody.isEnabled  = false

        Snackbar.make(parent_layout, "Read Mode", Snackbar.LENGTH_INDEFINITE)
            .setAction("Cancel"){
                supportActionBar?.show()
                edtNoteTitle.isEnabled = true
                edtNoteBody.isEnabled  = true
            }.show()
    }
    private fun getEditedTime():String = SimpleDateFormat("MMM dd , HH:mm").format(Date())
    private fun getSharableUri(path: String): Uri{

        val file = File(path)
        return FileProvider.getUriForFile(
            applicationContext,
            applicationContext.packageName + ".authority", file)
    }
    private fun openFile(uri: Uri){

        val data = Intent(Intent.ACTION_VIEW)
        data.setDataAndType(uri, StorageHelper.getMimeType(uri.toString()))
        data.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivity(data)
    }
    private fun removeFile(path: String, position: Int){

        val fileUri = getSharableUri(path)

        val contentResolver = this.contentResolver
        contentResolver.delete(fileUri, null, null)

        if(StorageHelper.getMimeType(path) == Constant.MIME_TYPE_IMAGE){
            photoFilePaths.removeAt(position)
            picturesAttachAdapter.setAttachments(photoFilePaths)
        }

        if(StorageHelper.getMimeType(path) == Constant.MIME_TYPE_VIDEO){
            videoFilePaths.removeAt(position)
            videosAttachAdapter.setAttachments(videoFilePaths)
        }

        checkHeaders()
    }
    private fun removeAllFiles(path: String) {

        if(StorageHelper.getMimeType(path) == Constant.MIME_TYPE_IMAGE) {

            for(i in photoFilePaths.indices){
                val fileUri = getSharableUri(photoFilePaths[i])
                val contentResolver = this.contentResolver
                contentResolver.delete(fileUri, null, null)
            }

            photoFilePaths.clear()
            picturesAttachAdapter.setAttachments(photoFilePaths)
        }

        if(StorageHelper.getMimeType(path) == Constant.MIME_TYPE_VIDEO){

            for(i in videoFilePaths.indices){
                val fileUri = getSharableUri(videoFilePaths[i])
                val contentResolver = this.contentResolver
                contentResolver.delete(fileUri, null, null)
            }

            videoFilePaths.clear()
            videosAttachAdapter.setAttachments(videoFilePaths)
        }

        checkHeaders()
    }
    private fun openOptionsDialog(path: String, position: Int){

        val itemOptions = arrayOf("Share", "Open", "Delete", "Delete All")
        val iconOptions = arrayOf(
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_share),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_open),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_delete_attach),
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_delete_all))

        val builder:BottomSheet.Builder = BottomSheet.Builder(this).apply {
            setTitle("Choose Actions...")
            setItems(itemOptions, iconOptions, DialogInterface.OnClickListener { dialog, which ->
                when(which){
                    0 -> {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.setType(StorageHelper.getMimeType(path))
                        shareIntent.putExtra(Intent.EXTRA_STREAM, getSharableUri(path))
                        startActivity(shareIntent)
                    }
                    1 -> {
                        openFile(Uri.parse(path))
                    }
                    2 -> {
                        removeFile(path, position)
                    }
                    3 -> {

                        val dialogBuilder = AlertDialog.Builder(this@AddNoteActivity).apply {
                            setCancelable(true)
                            setMessage("Are you want to delete all files?")
                            setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                                removeAllFiles(path)
                                dialog.dismiss()
                            })
                            setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                            })
                        }
                        val alert: AlertDialog = dialogBuilder.create()
                        alert.setTitle("Warning")
                        alert.show()
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(selectedColor)
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(selectedColor)
                    }
                }
            })
            setContentType(BottomSheet.LIST)
        }
        builder.show()
    }
    private fun checkHeaders(){

        if(photoFilePaths.size == 0)
            txtPicHeader.visibility = View.INVISIBLE
        if(photoFilePaths.size > 0)
            txtPicHeader.visibility = View.VISIBLE

        if(videoFilePaths.size == 0)
            txtVidHeader.visibility = View.INVISIBLE
        if(videoFilePaths.size > 0)
            txtVidHeader.visibility = View.VISIBLE
    }


    /**
     * Alert dialog for capture or select from galley
     */
    private fun selectImage() {

        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = AlertDialog.Builder(this).apply {

                setItems(items) { dialog: DialogInterface, item: Int ->
                    when {
                        items[item] == "Take Photo" -> requestStoragePermission(true)
                        items[item] == "Choose from Library" -> requestStoragePermission(false)
                        items[item] == "Cancel" -> dialog.dismiss()
                    }
                }
            }
        builder.show()
    }

    /**
     * Requesting multiple permissions (storage and camera) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */

    private fun requestStoragePermission(isCamera: Boolean){

        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA)

            .withListener(object : MultiplePermissionsListener{

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    // check if all permissions are granted
                    if (report!!.areAllPermissionsGranted()) {
                        if (isCamera)
                            dispatchTakePictureIntent()
                        else
                            dispatchGalleryIntent()
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {

                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener { error -> showMessage("Error occurred!") }
            .onSameThread()
            .check()
    }
    private fun dispatchGalleryIntent() {

        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(pickPhoto, Constant.GALLERY_PHOTO_REQUEST)

        /*val filesIntent: Intent
        filesIntent = Intent(Intent.ACTION_GET_CONTENT)
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        filesIntent.addCategory(Intent.CATEGORY_OPENABLE)

        startActivityForResult(filesIntent, Constant.GALLERY_PHOTO_REQUEST)*/

        //filesIntent.type = "*/*"

    }
    private fun dispatchTakePictureIntent() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = StorageHelper.createNewAttachmentFile(this, Constant.MIME_TYPE_IMAGE_EXT)
            } catch (ex: IOException) {
                ex.printStackTrace()
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI = getSharableUri(photoFile.path)

                mPhotoFile = photoFile
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, Constant.IMAGE_CAPTURE_REQUEST)
            }
        }
    }
    private fun dispatchTakeVideoIntent(){

        val takeVideo = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if(!IntentChecker.isAvailable(this, takeVideo, arrayOf(PackageManager.FEATURE_CAMERA))){
            showMessage(this.getString(R.string.feature_not_available_on_this_device    ))
            return
        }

        val f = StorageHelper.createNewAttachmentFile(this, Constant.MIME_TYPE_VIDEO_EXT)
        val videoUri = getSharableUri(f.path)
        mVideoFile = f

        takeVideo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        takeVideo.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
        //takeVideo.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024L * 1024L)
        startActivityForResult(takeVideo, Constant.VIDEO_CAPTURE_REQUEST)
    }


    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     */
    private fun showSettingsDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this).apply {
            setTitle("Need Permissions")
            setMessage(getString(R.string.permission_alert))
            setPositiveButton("GoTO Settings") { dialog, _ ->
                dialog.cancel()
                openSettings()
            }
        }

        builder.show()
    }
    // navigating user to app settings
    private fun openSettings() {
        val intent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, Constant.SETTINGS_REQUEST)
    }

    private fun init(){
        initToolBar()
        initEditText()
        initTextViews()
        initRecyclerView()
        bottomSheetInit()
        getDataForEditNote()
        checkHeaders()
    }
}

package com.smartelectronics.note.util

class Constant{

    companion object{


        const val ENABLE: Int  = 1
        const val DISABLE: Int = 2
        const val DATE_FORMAT_SORTABLE: String = "yyyyMMdd_HHmmss_SSS"
        const val EXTERNAL_STORAGE_FOLDER = "Note"

        const val ADD_NOTE_REQUEST:Int       = 100
        const val EDIT_NOTE_REQUEST:Int      = 200
        const val IMAGE_CAPTURE_REQUEST:Int  = 300
        const val GALLERY_PHOTO_REQUEST:Int  = 400
        const val VIDEO_CAPTURE_REQUEST:Int  = 500
        const val SETTINGS_REQUEST:Int       = 600

        const val EXTRA_ID:String            = "com.smartelectronics.note.ui.EXTRA_ID"
        const val EXTRA_TITLE:String         = "com.smartelectronics.note.ui.EXTRA_TITLE"
        const val EXTRA_BODY:String          = "com.smartelectronics.note.ui.EXTRA_BODY"
        const val EXTRA_CARD_COLOR:String    = "com.smartelectronics.note.ui.EXTRA_CARD_COLOR"
        const val EXTRA_NOTIFICATION:String  = "com.smartelectronics.note.ui.EXTRA_NOTIFICATION"
        const val EXTRA_FAVORITE:String      = "com.smartelectronics.note.ui.EXTRA_FAVORITE"
        const val EXTRA_ATTACH:String        = "com.smartelectronics.note.ui.EXTRA_ATTACH"
        const val EXTRA_FONT_SIZE:String     = "com.smartelectronics.note.ui.EXTRA_FONT_SIZE"
        const val EXTRA_EDIT_TIME:String     = "com.smartelectronics.note.ui.EXTRA_EDIT_TIME"
        const val EXTRA_IMAGE_PATHS:String   = "com.smartelectronics.note.ui.EXTRA_IMAGE_PATHS"
        const val EXTRA_VIDEO_PATHS:String   = "com.smartelectronics.note.ui.EXTRA_VIDEO_PATHS"


        const val MIME_TYPE_IMAGE  = "image/jpeg"
        const val MIME_TYPE_AUDIO  = "audio/amr"
        const val MIME_TYPE_VIDEO  = "video/mp4"
        const val MIME_TYPE_SKETCH = "image/png"
        const val MIME_TYPE_FILES  = "file/*"

        const val MIME_TYPE_IMAGE_EXT   = ".jpeg"
        const val MIME_TYPE_AUDIO_EXT   = ".amr"
        const val MIME_TYPE_VIDEO_EXT   = ".mp4"
        const val MIME_TYPE_SKETCH_EXT  = ".png"
        const val MIME_TYPE_CONTACT_EXT = ".vcf"
    }
}
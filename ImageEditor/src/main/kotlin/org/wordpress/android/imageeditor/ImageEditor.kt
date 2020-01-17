
package org.wordpress.android.imageeditor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.wordpress.android.imageeditor.enums.ImageEditorOperation.CROP

class ImageEditor {
    private val actions: Array<String> = arrayOf(CROP.name) // TODO
    private var imageUrls: ArrayList<String> = ArrayList()

    val isSingleImageAndCapability: Boolean
        get() = imageUrls.size == 1

    /**
     * @param context self explanatory
     * @param contentUri URI of initial media - can be local or remote
     */
    fun edit(
        context: Context,
        contentUri: String
    ) {
        // TODO
        // Temporarily goes to edit image activity
        val intent = Intent(context, EditImageActivity::class.java)

        val bundle = Bundle()

        val startDestination = R.id.home_dest // TODO

        bundle.putInt(EditImageActivity.ARG_START_DESTINATION, startDestination)
        bundle.putString(EditImageActivity.ARG_IMAGE_CONTENT_URI, contentUri)
        bundle.putStringArray(EditImageActivity.ARG_ACTIONS, actions) // TODO: pass in params

        intent.putExtra(EditImageActivity.ARG_BUNDLE, bundle)

        EditImageActivity.startIntent(context, intent)
    }
}

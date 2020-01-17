package org.wordpress.android.imageeditor.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.wordpress.android.imageeditor.EditImageActivity
import org.wordpress.android.imageeditor.R

class MainImageFragment : Fragment() {
    private var contentUri: String? = null
    private var fragmentWasPaused: Boolean = false

    private lateinit var mainImageView: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contentUri = arguments?.getString(EditImageActivity.ARG_IMAGE_CONTENT_URI)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.main_image_fragment, container, false)
        mainImageView = view.findViewById(R.id.main_image)
        progressBar = view.findViewById(R.id.progress_loading)

        return view
    }

    override fun onPause() {
        fragmentWasPaused = true

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        if (fragmentWasPaused) {
            fragmentWasPaused = false
        } else {
            loadImage(contentUri)
        }
    }

    private fun loadImage(mediaUri: String?) {
        mainImageView.visibility = View.VISIBLE

        // TODO
        Glide.with(this)
            .load(mediaUri)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(mainImageView)
    }
}

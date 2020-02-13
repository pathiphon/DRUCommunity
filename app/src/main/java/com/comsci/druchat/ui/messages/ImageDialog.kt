package com.comsci.druchat.ui.messages

import android.view.View
import android.widget.ImageView
import com.adedom.library.extension.loadImage
import com.adedom.library.util.BaseDialogFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.util.KEY_IMAGE

class ImageDialog : BaseDialogFragment<BaseViewModel>(
    { R.layout.dialog_image },
    { R.drawable.ic_image_black },
    { R.string.image }
) {

    override fun initDialog(view: View) {
        super.initDialog(view)

        val image = arguments!!.getString(KEY_IMAGE)

        val ivImage = view.findViewById(R.id.mIvImage) as ImageView

        ivImage.loadImage(image!!)
    }

}

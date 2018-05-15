package com.zhangyf.storypath.bean

import android.graphics.Bitmap
import java.lang.ref.WeakReference

/**
 * model
 *
 * @author zhangyf
 * @date 2018/5/15.
 */
data class StoryBean(val cover: String,
                     val lockStatus: Int,
                     var x: Int,
                     var y: Int,
                     var bitmap: WeakReference<Bitmap>?)


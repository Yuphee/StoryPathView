package com.zhangyf.storypath

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.zhangyf.storypath.bean.StoryBean
import com.zhangyf.storypath.views.StoryPathView
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    private var list: MutableList<StoryBean> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cl_root.setBackgroundResource(R.mipmap.background)
        var storyPathView = StoryPathView(this)
        storyPathView.setMaxColumn(3)
        for (i in 0..17) {
            var bean: StoryBean = if (i > 10) {
                StoryBean("http://onz34txkn.bkt.clouddn.com/1804091636236975.png", 0, 0, 0, null)
            } else {
                StoryBean("http://onz34txkn.bkt.clouddn.com/1804091636236975.png", 1, 0, 0, null)
            }
            list.add(bean)
        }
        storyPathView.setOnPointClickListener({ lock ->
            if (lock) {
                toast("尚未解锁该任务")
            } else {
                toast("跳转其它页面")
            }
        })
        storyPathView.setData(list!!)
        scroll_path_view.addView(storyPathView)
    }
}

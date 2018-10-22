package com.crestron.aurora.otherfun

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.ViewUtil
import kotlinx.android.synthetic.main.activity_view_videos.*
import kotlinx.android.synthetic.main.video_layout.view.*
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.util.*


class ViewVideosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_videos)

        ViewUtil.revealing(findViewById(android.R.id.content), intent)

        class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

            //constructor(@NonNull context: Context, itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId)) {}

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
            }
        }

        view_videos_recyclerview.layoutManager = LinearLayoutManager(this@ViewVideosActivity)
        val dividerItemDecoration = DividerItemDecoration(view_videos_recyclerview.context, (view_videos_recyclerview.layoutManager as LinearLayoutManager).orientation)
        view_videos_recyclerview.addItemDecoration(dividerItemDecoration)
        view_videos_recyclerview.addItemDecoration(ItemOffsetDecoration(20))

        launch {
            val list = getListFiles2(File(FetchingUtils.folderLocation))
            for (i in list) {
                Loged.i("${i.name}")
            }
            runOnUiThread {
                view_videos_recyclerview.adapter = VideoAdapter(list, this@ViewVideosActivity)
            }
        }

    }

    fun openVideo(path: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
        intent.setDataAndType(Uri.parse(path), "video/mp4")
        startActivity(intent)
    }

    private fun getListFiles2(parentDir: File): List<File> {
        val inFiles = arrayListOf<File>()
        val files = LinkedList<File>()
        files.addAll(parentDir.listFiles())
        while (!files.isEmpty()) {
            val file = files.remove()
            if (file.isDirectory) {
                files.addAll(file.listFiles())
            } else if (file.name.endsWith(".mp4")) {
                inFiles.add(file)
            }
        }
        return inFiles
    }

    class VideoAdapter(private var stuff: List<File>,
                       var context: Context) : RecyclerView.Adapter<ViewHolder>() {

        // Gets the number of animals in the list
        override fun getItemCount(): Int {
            return stuff.size
        }

        // Inflates the item views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.video_layout, parent, false))
        }

        // Binds each animal in the ArrayList to a view
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.videoName.text = stuff[position].name
            holder.videoThumbnail.setVideoPath(stuff[position].path)
            holder.videoThumbnail.seekTo(100)
            holder.videoLayout.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(stuff[position].path))
                intent.setDataAndType(Uri.parse(stuff[position].path), "video/mp4")
                context.startActivity(intent)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val videoThumbnail: VideoView = view.video_view!!
        val videoName: TextView = view.video_name!!
        val videoLayout: LinearLayout = view.videos_layout!!

        init {
            setIsRecyclable(false)
        }
    }

}

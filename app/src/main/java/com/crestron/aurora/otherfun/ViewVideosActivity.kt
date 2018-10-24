package com.crestron.aurora.otherfun

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.abdeveloper.library.MultiSelectDialog
import com.abdeveloper.library.MultiSelectModel
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.ViewUtil
import com.crestron.aurora.views.DeleteDialog
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import github.nisrulz.recyclerviewhelper.RVHAdapter
import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_view_videos.*
import kotlinx.android.synthetic.main.video_layout.view.*
import kotlinx.android.synthetic.main.video_with_text.view.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class ViewVideosActivity : AppCompatActivity() {

    interface DeleteVideoListener {
        fun videoDelete(position: Int)
    }

    lateinit var listOfFiles: ArrayList<File>

    lateinit var adapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_videos)

        ViewUtil.revealing(findViewById(android.R.id.content), intent)

        class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
            }
        }

        view_videos_recyclerview.layoutManager = LinearLayoutManager(this@ViewVideosActivity)
        view_videos_recyclerview.addItemDecoration(DividerItemDecoration(view_videos_recyclerview.context, (view_videos_recyclerview.layoutManager as LinearLayoutManager).orientation))
        view_videos_recyclerview.addItemDecoration(ItemOffsetDecoration(20))

        fun getStuff() {
            listOfFiles = getListFiles2(File(FetchingUtils.folderLocation))
            for (i in listOfFiles) {
                Loged.i(i.name)
            }

            val instance = Picasso.Builder(this@ViewVideosActivity)
                    .addRequestHandler(VideoRequestHandler())
                    .build()

            adapter = VideoAdapter(listOfFiles, this@ViewVideosActivity, object : DeleteVideoListener {
                override fun videoDelete(position: Int) {
                    listOfFiles.removeAt(position)
                    adapter.notifyItemRemoved(position)
                }
            }, instance)
            view_videos_recyclerview.adapter = adapter
            val callback = RVHItemTouchHelperCallback(adapter
                    , false
                    , true
                    , true)
            val helper = ItemTouchHelper(callback)
            helper.attachToRecyclerView(view_videos_recyclerview)
        }

        getStuff()

        delete_multiple.setOnClickListener { _ ->
            val multiSelectDialog = MultiSelectDialog()
                    .title("Select the Videos to Delete") //setting title for dialog
                    .titleSize(25f)
                    .positiveText("Delete")
                    .negativeText("Cancel")
                    .setMinSelectionLimit(0) //you can set minimum checkbox selection limit (Optional)
                    .setMaxSelectionLimit(listOfFiles.size) //you can set maximum checkbox selection limit (Optional)
                    .multiSelectList(java.util.ArrayList<MultiSelectModel>().apply {
                        for (i in 0 until listOfFiles.size) {
                            add(MultiSelectModel(i, listOfFiles[i].name))
                        }
                    }) // the multi select model list with ids and name
                    .onSubmit(object : MultiSelectDialog.SubmitCallbackListener {
                        override fun onSelected(selectedIds: java.util.ArrayList<Int>?, selectedNames: java.util.ArrayList<String>?, dataString: String?) {
                            launch {
                                for (f in listOfFiles) {
                                    if (selectedNames!!.any { it == f.name }) {
                                        f.delete()
                                        runOnUiThread {
                                            val index = listOfFiles.indexOf(f)
                                            listOfFiles.remove(f)
                                            adapter.notifyItemRemoved(index)
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancel() {
                            Loged.e("cancelled")
                        }

                    })

            runOnUiThread {

                multiSelectDialog.show(supportFragmentManager, "multiSelectDialog")

            }
        }

    }

    private fun getListFiles2(parentDir: File): ArrayList<File> {
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

    class VideoRequestHandler : RequestHandler() {
        companion object {
            const val SCHEME_VIDEO = "video"
        }

        override fun canHandleRequest(data: Request): Boolean {
            val scheme = data.uri.scheme
            return (SCHEME_VIDEO == scheme)
        }

        override fun load(data: Request, arg1: Int): Result {
            val bm = ThumbnailUtils.createVideoThumbnail(data.uri.path, MediaStore.Images.Thumbnails.MINI_KIND)
            return Result(bm, Picasso.LoadedFrom.DISK)
        }
    }

    class VideoAdapter(private var stuff: ArrayList<File>,
                       var context: Context,
                       val videoListener: DeleteVideoListener,
                       val picasso: Picasso) : RecyclerView.Adapter<ViewHolder>(), RVHAdapter {

        override fun onItemDismiss(position: Int, direction: Int) {
            Loged.e("$direction")
            context.runOnUiThread {
                val listener = object : DeleteDialog.DeleteDialogListener {
                    override fun onDelete() {
                        stuff[position].delete()
                        stuff.removeAt(position)
                        notifyItemRemoved(position)
                    }

                    override fun onCancel() {
                        notifyDataSetChanged()
                    }
                }
                DeleteDialog(context, stuff[position].name, file = stuff[position], listener = listener).show()
            }
        }

        override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            return false
        }

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

            try {
                //Video runtime text
                val mp = MediaPlayer.create(context, Uri.parse(stuff[position].path))
                val duration = mp.duration.toLong()
                mp.release()
                /*convert millis to appropriate time*/
                val runTimeString = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
                /*val formatter = SimpleDateFormat("mm:ss")
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                val dateFormatted = formatter.format(Date(duration))*/
                holder.videoRuntime.text = runTimeString
                context.runOnUiThread {
                    //Thumbnail image
                    picasso.load(VideoRequestHandler.SCHEME_VIDEO + ":" + stuff[position].path)?.into(holder.videoThumbnail)
                }
                //to play video
                holder.videoLayout.setOnClickListener {
                    if (context.defaultSharedPreferences.getBoolean("videoPlayer", false)) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(stuff[position].path))
                        intent.setDataAndType(Uri.parse(stuff[position].path), "video/mp4")
                        context.startActivity(intent)
                    } else {
                        context.startActivity(Intent(context, VideoPlayerActivity::class.java).apply {
                            putExtra("video_path", stuff[position].path)
                            putExtra("video_name", stuff[position].name)
                        })
                    }
                }
                holder.videoLayout.setOnLongClickListener {
                    val listener = object : DeleteDialog.DeleteDialogListener {
                        override fun onDelete() {
                            videoListener.videoDelete(holder.adapterPosition)
                        }
                    }
                    DeleteDialog(context, stuff[position].name, file = stuff[position], listener = listener).show()
                    true
                }
            } catch (e: IllegalStateException) {
                holder.videoRuntime.text = "???"
                holder.videoThumbnail.setImageResource(android.R.drawable.stat_notify_error)
                holder.videoLayout.setOnClickListener {
                    Toast.makeText(context, "Still Downloading", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val videoThumbnail: ImageView = view.video_thumbnail!!
        val videoRuntime: TextView = view.video_runtime!!
        val videoName: TextView = view.video_name!!
        val videoLayout: LinearLayout = view.videos_layout!!

        init {
            setIsRecyclable(true)
        }
    }

}

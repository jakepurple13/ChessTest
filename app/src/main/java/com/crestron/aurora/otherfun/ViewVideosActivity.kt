package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.ViewUtil
import com.crestron.aurora.views.DeleteDialog
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import com.tonyodev.fetch2.Fetch
import github.nisrulz.recyclerviewhelper.RVHAdapter
import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback
import hb.xvideoplayer.MxUtils
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.activity_view_videos.*
import kotlinx.android.synthetic.main.video_layout.view.*
import kotlinx.android.synthetic.main.video_with_text.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class ViewVideosActivity : AppCompatActivity() {

    interface DeleteVideoListener {
        fun videoDelete(position: Int)
    }

    var listOfFiles: ArrayList<File> = arrayListOf()

    var adapter: VideoAdapter? = null

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
        view_videos_recyclerview.setHasFixedSize(true)
        val instance = Picasso.Builder(this@ViewVideosActivity)
                .addRequestHandler(VideoRequestHandler())
                .build()

        fun getStuff() {
            listOfFiles.addAll(getListFiles2(File(FetchingUtils.folderLocation)))
            listOfFiles.sortBy { it.name }
            for (i in listOfFiles) {
                Loged.i(i.name)
            }
            //to get rid of any preferences of any videos that have been deleted else where
            val prefs = defaultSharedPreferences.all.keys
            val fileRegex = "(\\/[^*|\"<>?\\n]*)|(\\\\\\\\.*?\\\\.*)".toRegex()
            val filePrefs = prefs.filter { fileRegex.containsMatchIn(it) }
            for (p in filePrefs) {
                Loged.i(p)
                if (!listOfFiles.any { it.path == p }) {
                    Loged.d(p)
                    defaultSharedPreferences.edit().remove(p).apply()
                }
            }
            if (adapter == null)
                adapter = VideoAdapter(listOfFiles, this@ViewVideosActivity, object : DeleteVideoListener {
                    override fun videoDelete(position: Int) {
                        listOfFiles.removeAt(position)
                    }
                }, instance)
            else {
                val list = listOfFiles
                list.addAll(adapter!!.stuff)
                adapter!!.add(list.distinctBy { it })
            }
            view_videos_recyclerview.swapAdapter(adapter, true)
            val callback = RVHItemTouchHelperCallback(adapter
                    , false
                    , true
                    , true)
            val helper = ItemTouchHelper(callback)
            helper.attachToRecyclerView(view_videos_recyclerview)

            runOnUiThread {
                video_refresh.isRefreshing = false
            }

        }

        getStuff()

        video_refresh.setOnRefreshListener {
            listOfFiles.clear()
            video_refresh.isRefreshing = true
            getStuff()
        }

        delete_multiple.setOnClickListener {
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
                            GlobalScope.launch {
                                for (f in listOfFiles) {
                                    if (selectedNames!!.any { it == f.name }) {
                                        defaultSharedPreferences.edit().remove(f.path).apply()
                                        f.delete()
                                        runOnUiThread {
                                            val index = listOfFiles.indexOf(f)
                                            listOfFiles.remove(f)
                                            adapter!!.notifyItemRemoved(index)
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

        val br = BroadcastReceiverVideoView(object : VideoBroadcast {
            override fun onCall(intent: Intent) {
                adapter!!.notifyDataSetChanged()
            }
        })
        val filter = IntentFilter().apply {
            addAction(ConstantValues.BROADCAST_VIDEO)
        }
        if (Fetch.getDefaultInstance().hasActiveDownloads)
            registerReceiver(br, filter)
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

    override fun onResume() {
        super.onResume()
        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(BroadcastReceiverVideoView())
        } catch (e: IllegalArgumentException) {

        }
        super.onDestroy()
    }

    companion object BroadCastInfo {

        interface VideoBroadcast {
            fun onCall(intent: Intent)
        }

        fun videoCast(context: Context) {
            Intent().also { intent ->
                intent.action = ConstantValues.BROADCAST_VIDEO
                intent.putExtra("data", "Notice me senpai!")
                context.sendBroadcast(intent)
            }
        }
    }

    class BroadcastReceiverVideoView(private val listener: VideoBroadcast? = null) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            listener?.onCall(intent)
            StringBuilder().apply {
                append("Action: ${intent.action}\n")
                append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
                toString().also { log ->
                    Loged.d(log)
                    //Toast.makeText(context, log, Toast.LENGTH_LONG).show()
                }
            }
        }
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

    class VideoAdapter(var stuff: ArrayList<File>,
                       var context: Context,
                       private val videoListener: DeleteVideoListener,
                       val picasso: Picasso) : RecyclerView.Adapter<ViewHolder>(), RVHAdapter {

        override fun onItemDismiss(position: Int, direction: Int) {
            if (stuff.isNotEmpty())
                DeleteDialog(context, stuff[position].name, listener = listener(position)).show()
            else
                notifyDataSetChanged()
        }

        override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            return false
        }

        fun add(list: Collection<File>) {
            stuff.addAll(list)
            stuff = stuff.distinctBy { it } as ArrayList<File>
            notifyDataSetChanged()
        }

        fun listener(position: Int) = object : DeleteDialog.DeleteDialogListener {
            override fun onDelete() {
                /*context.defaultSharedPreferences.edit().remove(stuff[position].path).apply()
                //videoListener.videoDelete(position)
                stuff[position].delete()
                stuff.removeAt(position)
                notifyItemRemoved(position)*/
                remove(position)
            }

            override fun onCancel() {
                notifyDataSetChanged()
            }
        }

        private fun remove(position: Int) {
            context.defaultSharedPreferences.edit().remove(stuff[position].path).apply()
            val f = stuff.removeAt(position)
            if (f.exists())
                f.delete()
            notifyItemRemoved(position)
            //notifyDataSetChanged()
            //videoListener.videoDelete(position)
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
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.videoName.text = "${stuff[position].name} ${if (context.defaultSharedPreferences.contains(stuff[position].path)) "\nat ${MxUtils.stringForTime(context.defaultSharedPreferences.getLong(stuff[position].path, 0))}" else ""}"

            try {
                //Video runtime text
                val mp = MediaPlayer.create(context, Uri.parse(stuff[position].path))
                val duration = mp.duration.toLong()
                mp.release()
                /*convert millis to appropriate time*/
                val runTimeString = if(duration>TimeUnit.HOURS.toMillis(1)) {
                    String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(duration),
                            TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
                } else {
                    String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(duration),
                            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
                }
                holder.videoRuntime.text = runTimeString
                context.runOnUiThread {
                    //Thumbnail image
                    picasso.load(VideoRequestHandler.SCHEME_VIDEO + ":" + stuff[position].path)?.
                            transform(RoundedCornersTransformation(5, 5))?.
                            into(holder.videoThumbnail)
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
                        //MxVideoPlayerWidget.startFullscreen(context, MxVideoPlayerWidget::class.java, stuff[position].path, stuff[position].name)
                    }
                }
                holder.videoLayout.setOnLongClickListener {
                    if (position != RecyclerView.NO_POSITION)
                        DeleteDialog(context, stuff[position].name, file = stuff[position], listener = listener(holder.adapterPosition)).show()
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

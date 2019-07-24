package com.box.shelfview;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import ru.nikartm.support.ImageBadgeView;

class ShelfAdapter extends BaseAdapter {

    private Context mContext;
    private List<ShelfModel> mShelfModels;
    private String internalStorage;
    private String mRawPath;
    private int mTargetWidth;
    private int mTargetHeight;

    ShelfAdapter(Context context, List<ShelfModel> shelfModels) {
        this.mContext = context;
        this.mShelfModels = shelfModels;

        mTargetWidth = Utils.dpToPixels(context, context.getResources().getInteger(R.integer.book_width));
        mTargetHeight = Utils.dpToPixels(context, context.getResources().getInteger(R.integer.book_height));
    }

    @Override
    public int getCount() {
        return mShelfModels.size();
    }

    @Override
    public Object getItem(int position) {
        return mShelfModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0L;
    }

    private static class ViewHolder {
        ImageView imvShelfBackground;
        ImageBadgeView imvBookCover;
        CardView cvBookBackground;
        ProgressBar pgbLoad;
        View vSpineGrey, vSpineWhite;
        TextView title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        // Create views
        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(R.layout.book_shelf_grid_item, parent, false);
            holder = new ViewHolder();
            holder.imvShelfBackground = convertView.findViewById(R.id.shelf_background);
            holder.imvBookCover = convertView.findViewById(R.id.book_cover_imv);
            holder.cvBookBackground = convertView.findViewById(R.id.book_background_cv);
            holder.pgbLoad = convertView.findViewById(R.id.load_pgb);
            holder.vSpineGrey = convertView.findViewById(R.id.spine_grey_view);
            holder.vSpineWhite = convertView.findViewById(R.id.spine_white_view);
            holder.title = convertView.findViewById(R.id.book_title);
            convertView.setTag(holder);
        }

        // Recycler view
        else {
            holder = (ViewHolder) convertView.getTag();
            holder.title.setText("");
        }

        final ShelfModel model = this.mShelfModels.get(position);

        holder.pgbLoad.setVisibility(!model.getShow() ? View.GONE : View.VISIBLE);

        switch (model.getType()) {
            case START:
                holder.imvShelfBackground.setImageResource(R.drawable.grid_item_background_left);
                break;
            case END:
                holder.imvShelfBackground.setImageResource(R.drawable.grid_item_background_right);
                break;
            default:
                holder.imvShelfBackground.setImageResource(R.drawable.grid_item_background_center);
                break;
        }

        loadImageWithPicasso(mContext, model, holder);
        return convertView;
    }

    private void loadImageWithPicasso(Context context, final ShelfModel model, final ViewHolder holder) {
        String bookCover = model.getBookCoverSource();
        if (model.getShow() && !bookCover.equals("")) {

            Callback callback = new Callback() {
                @Override
                public void onSuccess() {
                    holder.cvBookBackground.setVisibility(!model.getShow() ? View.GONE : View.VISIBLE);
                    holder.pgbLoad.setVisibility(View.GONE);
                    holder.vSpineGrey.setVisibility(View.VISIBLE);
                    holder.vSpineWhite.setVisibility(View.VISIBLE);
                    holder.title.setText("");
                    if(!model.getBookTitle().isEmpty())
                        holder.title.setText(model.getBookTitle());
                    else
                        holder.title.setText("");
                }

                @Override
                public void onError(Exception e) {
                    Log.e("ERROR", e.getMessage());
                }
            };

            switch (model.getBookSource()) {
                case FILE:
                    Picasso.get()
                            .load(new File(/*getInternalStorage() +*/ bookCover))
                            .resize(mTargetWidth, mTargetHeight)
                            .into(holder.imvBookCover, callback);
                    break;
                case URL:
                    Picasso.get()
                            .load(bookCover)
                            .placeholder(android.R.drawable.btn_default)
                            .resize(mTargetWidth, mTargetHeight)
                            .into(holder.imvBookCover, callback);
                    break;
                case ASSET_FOLDER:
                    Picasso.get()
                            .load("file:///android_asset/" + bookCover)
                            .resize(mTargetWidth, mTargetHeight)
                            .into(holder.imvBookCover, callback);
                    break;
                case DRAWABLE_NAME:
                    Picasso.get()
                            .load(context.getResources().getIdentifier(bookCover, "drawable", context.getPackageName()))
                            .resize(mTargetWidth, mTargetHeight)
                            .into(holder.imvBookCover, callback);
                    break;
                case DRAWABLE_ID:
                    Picasso.get()
                            .load(Integer.parseInt(bookCover))
                            .resize(mTargetWidth, mTargetHeight)
                            .into(holder.imvBookCover, callback);
                    break;
                case RAW:
                    String path = getRawPath() + bookCover;
                    Uri uri = Uri.parse(path);
                    Picasso.get()
                            .load(uri)
                            .resize(mTargetWidth, mTargetHeight)
                            .into(holder.imvBookCover, callback);
                    break;
                case NONE:
                    Picasso.get()
                            .load(model.getBookCoverSource())
                            .resize(mTargetWidth, mTargetHeight)
                            .into(holder.imvBookCover, callback);
                    break;
            }
            if (model.getBadgeCount() > 0) {
                holder.imvBookCover.setBadgeValue(model.getBadgeCount());
                //context.registerReceiver()
            }
        } else {
            holder.cvBookBackground.setVisibility(View.GONE);
            holder.pgbLoad.setVisibility(View.GONE);
            holder.vSpineGrey.setVisibility(View.VISIBLE);
            holder.vSpineWhite.setVisibility(View.VISIBLE);
        }
    }

    private String getRawPath() {
        if (mRawPath == null) {
            mRawPath = "android.resource://" + mContext.getPackageName() + "/";
        }
        return mRawPath;
    }

    private String getInternalStorage() {
        if (internalStorage == null) {
            this.internalStorage = Environment.getExternalStorageDirectory().toString();
        }
        return internalStorage;
    }
}
package com.crestron.aurora.otherfun;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crestron.aurora.R;
import com.crestron.aurora.views.DeleteDialog;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Status;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import github.nisrulz.recyclerviewhelper.RVHAdapter;

public final class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> implements RVHAdapter {

    @NonNull
    private final List<DownloadData> downloads = new ArrayList<>();
    @NonNull
    private final ActionListener actionListener;

    private final Context context;

    FileAdapter(@NonNull final ActionListener actionListener, Context context) {
        this.actionListener = actionListener;
        this.context = context;
    }

    @NonNull
    public List<DownloadData> getDownloads() {
        return downloads;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.actionButton.setOnClickListener(null);
        holder.actionButton.setEnabled(true);

        final DownloadData downloadData = downloads.get(position);
        String url = "";
        if (downloadData.download != null) {
            url = downloadData.download.getUrl();
        }
        final Uri uri = Uri.parse(url);
        final Status status = downloadData.download.getStatus();
        final Context context = holder.itemView.getContext();

        holder.titleTextView.setText(uri.getLastPathSegment());
        holder.statusTextView.setText(getStatusString(status));

        int progress = downloadData.download.getProgress();
        if (progress == -1) { // Download progress is undermined at the moment.
            progress = 0;
        }
        holder.progressBar.setProgress(progress);
        holder.progressTextView.setText(context.getString(R.string.percent_progress, progress));

        if (downloadData.eta == -1) {
            holder.timeRemainingTextView.setText("");
        } else {
            holder.timeRemainingTextView.setText(FetchingUtils.Fetched.getETAString(downloadData.eta, true));
        }

        if (downloadData.downloadedBytesPerSecond == 0) {
            holder.downloadedBytesPerSecondTextView.setText("");
        } else {
            holder.downloadedBytesPerSecondTextView.setText(FetchingUtils.Fetched.getDownloadSpeedString(downloadData.downloadedBytesPerSecond));
        }

        switch (status) {
            case COMPLETED: {
                holder.actionButton.setText(R.string.view);
                holder.actionButton.setOnClickListener(view -> {
                    Toast.makeText(context, "Downloaded Path:" + downloadData.download.getFile(), Toast.LENGTH_LONG).show();
                    final File file = new File(downloadData.download.getFile());
                    final Uri uri1 = Uri.fromFile(file);
                    final Intent share = new Intent(Intent.ACTION_VIEW);
                    share.setDataAndType(uri1, FetchingUtils.Fetched.getMimeType(context, uri1));
                    context.startActivity(share);
                });
                break;
            }
            case FAILED: {
                holder.actionButton.setText(R.string.retry);
                holder.actionButton.setOnClickListener(view -> {
                    holder.actionButton.setEnabled(false);
                    actionListener.onRetryDownload(downloadData.download.getId());
                });
                break;
            }
            case PAUSED: {
                holder.actionButton.setText(R.string.resume);
                holder.actionButton.setOnClickListener(view -> {
                    holder.actionButton.setEnabled(false);
                    actionListener.onResumeDownload(downloadData.download.getId());
                });
                break;
            }
            case DOWNLOADING:
            case QUEUED: {
                holder.actionButton.setText(R.string.pause);
                holder.actionButton.setOnClickListener(view -> {
                    holder.actionButton.setEnabled(false);
                    actionListener.onPauseDownload(downloadData.download.getId());
                });
                break;
            }
            case ADDED: {
                holder.actionButton.setText(R.string.download);
                holder.actionButton.setOnClickListener(view -> {
                    holder.actionButton.setEnabled(false);
                    actionListener.onResumeDownload(downloadData.download.getId());
                });
                break;
            }
            default: {
                break;
            }
        }

        holder.actionButton.setOnLongClickListener(view -> {
            holder.itemView.performClick();
            return true;
        });

        //Set delete action
        holder.itemView.setOnLongClickListener(v -> {
            final Uri uri12 = Uri.parse(downloadData.download.getUrl());
            /*new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.delete_title, uri12.getLastPathSegment()))
                    .setPositiveButton(R.string.delete, (dialog, which) -> actionListener.onRemoveDownload(downloadData.download.getId()))
                    .setNegativeButton(R.string.cancel, null)
                    .show();*/

            new DeleteDialog(context, context.getString(R.string.delete_title, uri12.getLastPathSegment()), downloadData.download, null, null).show();

            return true;
        });

    }

    public void addDownload(@NonNull final Download download) {
        boolean found = false;
        DownloadData data = null;
        int dataPosition = -1;
        for (int i = 0; i < downloads.size(); i++) {
            final DownloadData downloadData = downloads.get(i);
            if (downloadData.id == download.getId()) {
                data = downloadData;
                dataPosition = i;
                found = true;
                break;
            }
        }
        if (!found) {
            final DownloadData downloadData = new DownloadData();
            downloadData.id = download.getId();
            downloadData.download = download;
            downloads.add(downloadData);
            notifyItemInserted(downloads.size() - 1);
        } else {
            data.download = download;
            notifyItemChanged(dataPosition);
        }
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public void update(@NonNull final Download download, long eta, long downloadedBytesPerSecond) {
        for (int position = 0; position < downloads.size(); position++) {
            final DownloadData downloadData = downloads.get(position);
            if (downloadData.id == download.getId()) {
                switch (download.getStatus()) {
                    case REMOVED:
                    case DELETED: {
                        downloads.remove(position);
                        notifyItemRemoved(position);
                        break;
                    }
                    default: {
                        downloadData.download = download;
                        downloadData.eta = eta;
                        downloadData.downloadedBytesPerSecond = downloadedBytesPerSecond;
                        notifyItemChanged(position);
                    }
                }
                return;
            }
        }
    }

    private String getStatusString(Status status) {
        switch (status) {
            case COMPLETED:
                return "Done";
            case DOWNLOADING:
                return "Downloading";
            case FAILED:
                return "Error";
            case PAUSED:
                return "Paused";
            case QUEUED:
                return "Waiting in Queue";
            case REMOVED:
                return "Removed";
            case NONE:
                return "Not Queued";
            default:
                return "Unknown";
        }
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        DeleteDialog.DeleteDialogListener listener = new DeleteDialog.DeleteDialogListener() {
            @Override
            public void onDelete() {
                downloads.remove(position);
                notifyItemRemoved(position);
            }

            @Override
            public void onCancel() {
                notifyDataSetChanged();
            }
        };
        new DeleteDialog(context, downloads.get(position).download.getFile(), downloads.get(position).download, null, listener).show();

    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView titleTextView;
        final TextView statusTextView;
        public final ProgressBar progressBar;
        final TextView progressTextView;
        public final Button actionButton;
        final TextView timeRemainingTextView;
        final TextView downloadedBytesPerSecondTextView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            statusTextView = itemView.findViewById(R.id.status_TextView);
            progressBar = itemView.findViewById(R.id.progressBar);
            actionButton = itemView.findViewById(R.id.actionButton);
            progressTextView = itemView.findViewById(R.id.progress_TextView);
            timeRemainingTextView = itemView.findViewById(R.id.remaining_TextView);
            downloadedBytesPerSecondTextView = itemView.findViewById(R.id.downloadSpeedTextView);
        }

    }

    public static class DownloadData {
        public int id;
        @Nullable
        public Download download;
        long eta = -1;
        long downloadedBytesPerSecond = 0;

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            if (download == null) {
                return "";
            }
            return download.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof DownloadData && ((DownloadData) obj).id == id;
        }
    }

}
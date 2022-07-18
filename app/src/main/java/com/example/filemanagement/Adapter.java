package com.example.filemanagement;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanagement.databinding.FileItemRowBinding;


import java.util.List;


public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Model> fileList;
    private OnClickListener listener;
    private int position = -1;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Model getFile(int position) {
        if (position < 0) {
            return  null;
        }

        return fileList.get(position);
    }

    public Adapter(List<Model> fileList, OnClickListener listener) {
        this.fileList = fileList;
        this.listener = listener;
    }

    public void setFileList(List<Model> fileList) {
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FileItemRowBinding binding = FileItemRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        switch (viewType) {
            case 0:
                return new DirectoryViewHolder(binding, listener);

            case 1:
                return new FileViewHolder(binding, listener);

        }
        return new FileViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FileViewHolder) {
            ((FileViewHolder) holder).bind(fileList.get(position));
        } else if (holder instanceof DirectoryViewHolder) {
            ((DirectoryViewHolder) holder).bind(fileList.get(position));
        }

        holder.itemView.setOnLongClickListener(view -> {
            setPosition(position);
            return false;
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (fileList.get(position).getType() == Type.DIRECTORY) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final FileItemRowBinding binding;
        private final OnClickListener listener;

        public FileViewHolder(@NonNull FileItemRowBinding binding, OnClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        public void bind(Model model) {
            binding.fileNameTv.setText(model.getFileName());
            if (model.getType() == Type.DIRECTORY) {
                binding.iconIv.setBackgroundResource(R.drawable.folder);
            } else {
                binding.iconIv.setBackgroundResource(R.drawable.file);
            }

            itemView.setOnClickListener(view -> {
                Toast.makeText(itemView.getContext(), "Opening file...", Toast.LENGTH_SHORT).show();
                listener.onItemClick(model);
            });

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Chọn hành động:");
            contextMenu.add(0, view.getId(), 0, "Đổi tên");
            contextMenu.add(0, view.getId(), 0, "Sao chép");
            contextMenu.add(0, view.getId(), 0, "Di chuyển");
            contextMenu.add(0, view.getId(), 0, "Xoá");
        }

    }

    public static class DirectoryViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final FileItemRowBinding binding;
        private final OnClickListener listener;

        public DirectoryViewHolder(@NonNull FileItemRowBinding binding, OnClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        public void bind(Model model) {
            binding.fileNameTv.setText(model.getFileName());
            if (model.getType() == Type.DIRECTORY) {
                binding.iconIv.setBackgroundResource(R.drawable.folder);
            } else {
                binding.iconIv.setBackgroundResource(R.drawable.file);
            }

            itemView.setOnClickListener(view -> {
                Toast.makeText(itemView.getContext(), "Opening directory...", Toast.LENGTH_SHORT).show();
                listener.onItemClick(model);
            });


            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Chọn hành động:");
            contextMenu.add(0, view.getId(), 0, "Đổi tên");
            contextMenu.add(0, view.getId(), 0, "Xoá");
        }
    }
}

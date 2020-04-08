package com.example.rastriyavidyarthisangh.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rastriyavidyarthisangh.POJO.SinglePhotoPojo;
import com.example.rastriyavidyarthisangh.R;

import java.util.List;

public class AdapterPhotos extends RecyclerView.Adapter<AdapterPhotos.AdapterPhotosViewHolder> implements PopupMenu.OnMenuItemClickListener{
    private static final String TAG = "tag adp";

    private Context mContext;
    private List<SinglePhotoPojo> mPhotosList;
    private PhotoOnClickHandler mClickHandler;
    private static String idOfPhoto;

    public interface PhotoOnClickHandler{
        void onClick(List<SinglePhotoPojo> photoPojoList, int position);
        void onLongClick(String idOfPhoto);
    }

    public AdapterPhotos(Context mContext, List<SinglePhotoPojo> mPhotosList, PhotoOnClickHandler clickHandler) {
        this.mContext = mContext;
        this.mPhotosList = mPhotosList;
        this.mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public AdapterPhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = li.inflate(R.layout.list_item_photos, parent, false);
        return new AdapterPhotosViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterPhotosViewHolder holder, final int position) {
        final SinglePhotoPojo singlePhoto = mPhotosList.get(position);

        Glide.with(mContext)
                .load(singlePhoto.getPhoto_url())
                .centerCrop()
                .into(holder.ivPhoto);

        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickHandler.onClick(mPhotosList, position);
            }
        });

        holder.ivPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                idOfPhoto = singlePhoto.getTime_uploaded()+singlePhoto.getWho_uploaded();
                deletePhoto(holder.ivPhoto);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mPhotosList == null) return 0;
        return mPhotosList.size();
    }

    class AdapterPhotosViewHolder extends RecyclerView.ViewHolder{
        ImageView ivPhoto;
        public AdapterPhotosViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPhoto = itemView.findViewById(R.id.ivPhoto);
        }
    }

    public void addSinglePhotoPojo(SinglePhotoPojo singlePhoto){
        mPhotosList.add(0, singlePhoto);
        notifyItemInserted(0);
    }

    public void removeSinglephoto(SinglePhotoPojo singlePhoto){
        int i = removeSinglePhotoHelper(singlePhoto);
        if(i != -1) {
            notifyItemRemoved(i);
        }
    }

    private int removeSinglePhotoHelper(SinglePhotoPojo singlePhoto){
        long timeUploaded = singlePhoto.getTime_uploaded();
        String idOfWhoUploaded = singlePhoto.getWho_uploaded();
        for(int i = 0; i < mPhotosList.size(); i++){
            if(mPhotosList.get(i).getTime_uploaded() == timeUploaded
                    && mPhotosList.get(i).getWho_uploaded().equalsIgnoreCase(idOfWhoUploaded)){
                mPhotosList.remove(i);
                return i;
            }
        }
        return -1;
    }

    private void deletePhoto(ImageView ivPhoto){
        PopupMenu popupMenu = new PopupMenu(mContext, ivPhoto);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_delete_photo, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.menu_delete_photo){
            mClickHandler.onLongClick(idOfPhoto);
        }
        return false;
    }
}

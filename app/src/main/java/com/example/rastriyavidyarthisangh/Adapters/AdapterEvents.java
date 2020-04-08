package com.example.rastriyavidyarthisangh.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rastriyavidyarthisangh.POJO.SingleEventPojo;
import com.example.rastriyavidyarthisangh.R;
import com.example.rastriyavidyarthisangh.ShowFullPhotoActivity;
import com.example.rastriyavidyarthisangh.Utils.DateTimeUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterEvents extends RecyclerView.Adapter<AdapterEvents.AdapterEventsViewHolder> implements PopupMenu.OnMenuItemClickListener{
    private static final String TAG = "tag ae";

    private DatabaseReference mDatabaseReference;
    private Context mContext;
    private List<SingleEventPojo> mEventsList;
    private static String idOfEvent;

    private final AdapterEventsOnClickHandler mOnClickHandler;

    public interface AdapterEventsOnClickHandler{
        void onClick(String idOfEvent);
    }

    public AdapterEvents(Context mContext, List<SingleEventPojo> mEventsList,
                         DatabaseReference databaseReference, AdapterEventsOnClickHandler onClickHandler) {
        this.mContext = mContext;
        this.mEventsList = mEventsList;
        this.mDatabaseReference = databaseReference;
        this.mOnClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public AdapterEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = li.inflate(R.layout.list_item_events, parent, false);
        return new AdapterEventsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterEventsViewHolder holder, int position) {
        final SingleEventPojo singleEvent = mEventsList.get(position);

        holder.tvWhoPosted.setText(singleEvent.getWho_posted());
        holder.tvTimePosted.setText(DateTimeUtils.getDateAndTimeToBeDisplayedInEvent(singleEvent.getTime_posted()));
        holder.tvDescription.setText(singleEvent.getDescription_of_post());
        if(!TextUtils.isEmpty(singleEvent.getEvent_photo_url())){
            Glide.with(mContext)
                    .load(singleEvent.getEvent_photo_url())
                    .centerCrop()
                    .into(holder.ivEventPhoto);
            holder.ivEventPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleEventPhotoClick(singleEvent.getEvent_photo_url());
                }
            });
        }
        holder.ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleProfilePictureClick(singleEvent.getId_of_who_posted());
            }
        });
        loadProfilePictureOfWhoPosted(singleEvent.getId_of_who_posted(), holder.ivProfilePicture);
        holder.ibMoreEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idOfEvent = singleEvent.getTime_posted()+singleEvent.getId_of_who_posted();
                showPopUpMenu(holder.ibMoreEvent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mEventsList == null) return 0;
        return mEventsList.size();
    }

    class AdapterEventsViewHolder extends RecyclerView.ViewHolder{
        TextView tvWhoPosted, tvTimePosted, tvDescription;
        CircleImageView ivProfilePicture;
        ImageView ivEventPhoto;
        ImageButton ibMoreEvent;

        public AdapterEventsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvWhoPosted = itemView.findViewById(R.id.tvWhoPosted);
            tvTimePosted = itemView.findViewById(R.id.tvTimePosted);
            tvDescription = itemView.findViewById(R.id.tvDescriptionOfPost);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            ivEventPhoto = itemView.findViewById(R.id.ivEventImage);
            ibMoreEvent = itemView.findViewById(R.id.ibMoreEvent);
        }
    }

    public void addSingleEvent(SingleEventPojo singleEvent){
        mEventsList.add(0, singleEvent);
        notifyItemInserted(0);
    }

    public void removeSingleEvent(SingleEventPojo singleEvent){
        int i = removeSingleEventHelper(singleEvent);
        if(i != -1) {
            notifyItemRemoved(i);
        }
    }

    private int removeSingleEventHelper(SingleEventPojo singleEvent){
        long timePosted = singleEvent.getTime_posted();
        String idOfWhoPosted = singleEvent.getId_of_who_posted();
        for(int i = 0; i < mEventsList.size(); i++){
            if(mEventsList.get(i).getTime_posted() == timePosted
                    && mEventsList.get(i).getId_of_who_posted().equalsIgnoreCase(idOfWhoPosted)){
                mEventsList.remove(i);
                return i;
            }
        }
        return -1;
    }

    private void loadProfilePictureOfWhoPosted(String idWhoPosted, final ImageView ivProfilePicture){
        DatabaseReference reference = mDatabaseReference
                .child(mContext.getString(R.string.json_object_cwc_members))
                .child(idWhoPosted)
                .child("profile_picture_url");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profile_picture_url = dataSnapshot.getValue(String.class);
                if(TextUtils.isEmpty(profile_picture_url)){
                    ivProfilePicture.setImageDrawable(mContext.getDrawable(R.drawable.empty_profile_picture));
                }else {
                    Glide.with(mContext).load(profile_picture_url).into(ivProfilePicture);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ivProfilePicture.setImageDrawable(mContext.getDrawable(R.drawable.empty_profile_picture));
            }
        });
    }

    private void handleProfilePictureClick(String idWhoPosted){
        DatabaseReference reference = mDatabaseReference
                .child(mContext.getString(R.string.json_object_cwc_members))
                .child(idWhoPosted)
                .child("profile_picture_url");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profile_picture_url = dataSnapshot.getValue(String.class);
                if(!TextUtils.isEmpty(profile_picture_url)){
                    Intent i = new Intent(mContext, ShowFullPhotoActivity.class);
                    i.putExtra("photo_url", profile_picture_url);
                    mContext.startActivity(i);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void showPopUpMenu(ImageButton ivMoreEvent){
        PopupMenu popupMenu = new PopupMenu(mContext, ivMoreEvent);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_view_more_event, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_delete_event :{
                mOnClickHandler.onClick(idOfEvent);
                return true;
            }
//            case R.id.menu_share_event :{
//                int a = 2;
//                return true;
//            }
        }
        return false;
    }

    private void handleEventPhotoClick(String event_photo_url){
        Intent i = new Intent(mContext, ShowFullPhotoActivity.class);
        i.putExtra("photo_url", event_photo_url);
        mContext.startActivity(i);
    }
}

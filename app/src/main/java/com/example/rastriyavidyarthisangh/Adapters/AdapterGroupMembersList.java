package com.example.rastriyavidyarthisangh.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rastriyavidyarthisangh.POJO.SingleGroupMember;
import com.example.rastriyavidyarthisangh.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGroupMembersList extends RecyclerView.Adapter<AdapterGroupMembersList.AdapterGroupMembersListViewHolder> {
    private static final String TAG = "tag agm";
    private Context mContext;
    private List<SingleGroupMember> mGroupMembersList;

    public AdapterGroupMembersList(Context mContext, List<SingleGroupMember> singleGroupMemberList) {
        this.mContext = mContext;
        this.mGroupMembersList = singleGroupMemberList;
    }

    @NonNull
    @Override
    public AdapterGroupMembersListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = li.inflate(R.layout.list_group_member, parent, false);
        return new AdapterGroupMembersListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterGroupMembersListViewHolder holder, int position) {
        SingleGroupMember singleGroupMember = mGroupMembersList.get(position);

        holder.tvName.setText(singleGroupMember.getName());
        if(TextUtils.isEmpty(singleGroupMember.getCollege())){
            holder.tvCollege.setText(mContext.getString(R.string.empty_college_group_member));
            holder.tvCollege.setTextColor(mContext.getResources().getColor(R.color.color_empty_college_group_member));
        } else{
            holder.tvCollege.setText(singleGroupMember.getCollege());
        }
        holder.tvMemberSince.setText(singleGroupMember.getDate_joined().substring(6));
        if(TextUtils.isEmpty(singleGroupMember.getProfile_picture_url())){
            holder.ivProfilePicture.setImageDrawable(mContext.getDrawable(R.drawable.empty_profile_picture));
        }else {
            Glide.with(mContext).load(singleGroupMember.getProfile_picture_url()).into(holder.ivProfilePicture);
        }
    }

    @Override
    public int getItemCount() {
        if(mGroupMembersList == null) return 0;
        return mGroupMembersList.size();
    }

    class AdapterGroupMembersListViewHolder extends RecyclerView.ViewHolder{
        TextView tvCollege, tvMemberSince, tvName;
        CircleImageView ivProfilePicture;
        public AdapterGroupMembersListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCollege = itemView.findViewById(R.id.tvCollegeValue);
            tvMemberSince = itemView.findViewById(R.id.tvMemberSinceValue);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
        }
    }

    public void addSingleGroupMember(SingleGroupMember groupMember){
        mGroupMembersList.add(mGroupMembersList.size(), groupMember);
        notifyItemInserted(mGroupMembersList.size()-1);
    }

}

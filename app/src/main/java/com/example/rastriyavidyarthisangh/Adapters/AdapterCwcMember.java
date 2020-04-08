package com.example.rastriyavidyarthisangh.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rastriyavidyarthisangh.POJO.SingleCwcMember;
import com.example.rastriyavidyarthisangh.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterCwcMember extends RecyclerView.Adapter<AdapterCwcMember.AdapterCwcMemberViewHolder> {

    private Context mContext;
    private List<SingleCwcMember> mCwcMemberList;

    public AdapterCwcMember(Context mContext, List<SingleCwcMember> mCwcMemberList) {
        this.mContext = mContext;
        this.mCwcMemberList = mCwcMemberList;
    }

    @NonNull
    @Override
    public AdapterCwcMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = li.inflate(R.layout.list_cwc_member, parent, false);
        return new AdapterCwcMemberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCwcMemberViewHolder holder, int position) {
        final SingleCwcMember singleCwcMember = mCwcMemberList.get(position);

        holder.tvName.setText(singleCwcMember.getName());
        holder.tvDesignation.setText(singleCwcMember.getDesignation());

        if(TextUtils.isEmpty(singleCwcMember.getPhone_number())
                || singleCwcMember.getPhone_number().substring(0, 11).equalsIgnoreCase("+9199999999")){
            holder.tvPhoneNumber.setText(mContext.getString(R.string.empty_college_group_member));
            holder.tvPhoneNumber.setTextColor(mContext.getResources().getColor(R.color.color_empty_college_group_member));
            holder.tvPhoneNumber.setEnabled(false);
        }else {
            holder.tvPhoneNumber.setEnabled(true);
            holder.tvPhoneNumber.setText(singleCwcMember.getPhone_number());
            holder.tvPhoneNumber.setTextColor(mContext.getResources().getColor(R.color.color_contact_ids));
            holder.tvPhoneNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendIntentToWhatsApp(singleCwcMember.getPhone_number());
                }
            });
        }
        if(TextUtils.isEmpty(singleCwcMember.getEmail_id())){
            holder.tvEmailId.setText(mContext.getString(R.string.empty_college_group_member));
            holder.tvEmailId.setTextColor(mContext.getResources().getColor(R.color.color_empty_college_group_member));
            holder.tvEmailId.setEnabled(false);
        }else {
            holder.tvEmailId.setEnabled(true);
            holder.tvEmailId.setText(singleCwcMember.getEmail_id());
            holder.tvEmailId.setTextColor(mContext.getResources().getColor(R.color.color_contact_ids));
            holder.tvEmailId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendIntentToEmailId(singleCwcMember.getEmail_id());
                }
            });
        }
        if(TextUtils.isEmpty(singleCwcMember.getFacebook_id())){
            holder.tvFacebookId.setText(mContext.getString(R.string.empty_college_group_member));
            holder.tvFacebookId.setTextColor(mContext.getResources().getColor(R.color.color_empty_college_group_member));
            holder.tvFacebookId.setEnabled(false);
        }else {
            holder.tvFacebookId.setEnabled(true);
            holder.tvFacebookId.setText(singleCwcMember.getFacebook_id());
            holder.tvFacebookId.setTextColor(mContext.getResources().getColor(R.color.color_contact_ids));
            holder.tvFacebookId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendIntentToFacebook(singleCwcMember.getFacebook_id());
                }
            });
        }
        if(TextUtils.isEmpty(singleCwcMember.getProfile_picture_url())){
            holder.ivProfilePicture.setImageDrawable(mContext.getDrawable(R.drawable.empty_profile_picture));
        }else {
            Glide.with(mContext).load(singleCwcMember.getProfile_picture_url()).into(holder.ivProfilePicture);
        }

    }

    @Override
    public int getItemCount() {
        if(mCwcMemberList == null) return 0;
        return mCwcMemberList.size();
    }

    class AdapterCwcMemberViewHolder extends RecyclerView.ViewHolder{
        TextView tvName, tvDesignation, tvPhoneNumber, tvEmailId, tvFacebookId;
        CircleImageView ivProfilePicture;
        public AdapterCwcMemberViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvDesignation = itemView.findViewById(R.id.tvDesignationOfMember);
            tvPhoneNumber = itemView.findViewById(R.id.tvMobileNumberValue);
            tvEmailId = itemView.findViewById(R.id.tvEmailIdValue);
            tvFacebookId = itemView.findViewById(R.id.tvFacebookIdValue);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
        }
    }

    public void addSingleCwcMember(SingleCwcMember cwcMember){
        mCwcMemberList.add(mCwcMemberList.size(), cwcMember);
        notifyItemInserted(mCwcMemberList.size()-1);
    }

    private void sendIntentToWhatsApp(String phoneNumber){
        Uri uri = Uri.parse("https://wa.me/"+phoneNumber);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(i);
    }

    private void sendIntentToEmailId(String emailId){
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("mailto:"+emailId);
        i.setData(uri);
        mContext.startActivity(i);
    }

    private void sendIntentToFacebook(String facebookUrl){
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo("com.facebook.katana", 0);

            if (info.enabled) {
                Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
            else {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
            }
        } catch (PackageManager.NameNotFoundException e) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
        }
    }

}

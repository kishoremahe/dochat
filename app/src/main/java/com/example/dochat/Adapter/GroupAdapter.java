package com.example.dochat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dochat.GroupMessageActivity;
import com.example.dochat.Model.Group;
import com.example.dochat.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupHolder> {

    private Context context;
    private List<Group> groups;

    public GroupAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
    }

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.group_card_view,parent,false);
        return new GroupHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull GroupHolder holder, final int position) {

        String s;

        if(groups.size()==0){
            s="hai";
        }
        else{

            holder.CardviewForGroup.setVisibility(View.VISIBLE);
            final Group group=groups.get(position);
            holder.GroupName.setText(group.getGroup_name());

            if(group.getImageurl().equals("default")){
                holder.ProfileImageForGroup.setImageResource(R.drawable.profile_image);
            }
            else{
                Glide.with(context).load(group.getImageurl()).into(holder.ProfileImageForGroup);
            }

            holder.CardviewForGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Group group1=groups.get(position);
                    Intent groupMsgIntent=new Intent(context,GroupMessageActivity.class);
                    groupMsgIntent.putExtra("GroupName",group1.getGroup_name());
                    context.startActivity(groupMsgIntent);

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(groups.size()>0){
            return groups.size();
        }
        else{
            return 1;
        }

    }
}
class GroupHolder extends RecyclerView.ViewHolder{

    final public CircleImageView ProfileImageForGroup;
    final public TextView GroupName;
    final public CardView CardviewForGroup;

    public GroupHolder(@NonNull View itemView) {
        super(itemView);

        ProfileImageForGroup=itemView.findViewById(R.id.profile_image_for_group);
        GroupName=itemView.findViewById(R.id.name_group);
        CardviewForGroup=itemView.findViewById(R.id.card_view_for_group);
    }
}

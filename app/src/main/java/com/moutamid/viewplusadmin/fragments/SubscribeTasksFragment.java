package com.moutamid.viewplusadmin.fragments;

import static android.view.LayoutInflater.from;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.viewplusadmin.R;
import com.moutamid.viewplusadmin.databinding.FragmentSubscribeTasksBinding;
import com.moutamid.viewplusadmin.models.SubscribeTaskModel;
import com.moutamid.viewplusadmin.utils.Utils;

import java.util.ArrayList;

public class SubscribeTasksFragment extends Fragment {

    ArrayList<SubscribeTaskModel> viewsModelArrayList = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;
    private FragmentSubscribeTasksBinding b;


    private ProgressDialog progressDialog;

    private int count = 0;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSubscribeTasksBinding.inflate(inflater, container, false);

        count = 0;

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        new Thread(() ->
                Utils.databaseReference().child("subscribe_tasks").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            viewsModelArrayList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                viewsModelArrayList.add(dataSnapshot.getValue(SubscribeTaskModel.class));
                                updateProgress(count++);
                            }
                            requireActivity().runOnUiThread(() -> initRecyclerView());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                })).start();

        b.createTaskBtnSubscribe.setOnClickListener(view -> {
            SubscribeTaskModel model = new SubscribeTaskModel();
            model.setTaskKey("");
            showDialog(model, "Create", true);
        });

        return b.getRoot();
    }

    private void updateProgress(int count) {
        requireActivity().runOnUiThread(()
                -> progressDialog.setMessage("Loading... (" + count + ")"));
    }

    private void initRecyclerView() {
        progressDialog.dismiss();
        conversationRecyclerView = b.recyclerViewTasksSubscribe;
        conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new RecyclerViewAdapterMessages();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);
        conversationRecyclerView.setItemViewCacheSize(20);
        conversationRecyclerView.setAdapter(adapter);

    }

    private void showDialog(SubscribeTaskModel model, String saveBtnText, boolean isCreating) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tasks_subscribe);
        dialog.setCancelable(true);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        EditText total, current, completed, posterid, videourl, thumbnail, taskkey;

        total = dialog.findViewById(R.id.total_et_task_subscribe);
        current = dialog.findViewById(R.id.current_et_task_subscribe);
        completed = dialog.findViewById(R.id.completed_et_task_subscribe);
        posterid = dialog.findViewById(R.id.poster_id_et_task_subscribe);
        videourl = dialog.findViewById(R.id.video_url_et_task_subscribe);
        thumbnail = dialog.findViewById(R.id.thumbnail_et_task_subscribe);
        taskkey = dialog.findViewById(R.id.task_key_et_task_subscribe);

        if (!isCreating) {
            total.setText(model.getTotalSubscribesQuantity() + "");
            current.setText(model.getCurrentSubscribesQuantity() + "");
            completed.setText(model.getCompletedDate() + "");
            posterid.setText(model.getPosterUid() + "");
            videourl.setText(model.getVideoUrl() + "");
            thumbnail.setText(model.getThumbnailUrl() + "");
            taskkey.setText(model.getTaskKey() + "");
        }

        Button button = dialog.findViewById(R.id.save_btn_task_subscribe);
        button.setText(saveBtnText);

        if (isCreating) {
            model.setTaskKey(Utils.databaseReference().child("subscribe_tasks").push().getKey());
            taskkey.setText(model.getTaskKey());
            current.setText("0");
            completed.setText("error");
        }

        dialog.findViewById(R.id.save_btn_task_subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                model.setTotalSubscribesQuantity(total.getText().toString());
                model.setCompletedDate(completed.getText().toString());
                model.setCurrentSubscribesQuantity(Integer.parseInt(current.getText().toString()));
                model.setPosterUid(posterid.getText().toString());
                model.setVideoUrl(videourl.getText().toString());
                model.setThumbnailUrl(thumbnail.getText().toString());
                model.setTaskKey(taskkey.getText().toString());

                Utils.databaseReference().child("subscribe_tasks").child(model.getTaskKey())
                        .setValue(model);

                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(layoutParams);
    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public RecyclerViewAdapterMessages.ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_task_item, parent, false);
            return new RecyclerViewAdapterMessages.ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerViewAdapterMessages.ViewHolderRightMessage holder, int position) {

            SubscribeTaskModel model = viewsModelArrayList.get(position);

            with(requireContext())
                    .asBitmap()
                    .load(model.getThumbnailUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.color.browser_actions_bg_grey)
                            .error(R.color.browser_actions_bg_grey)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.imageView);

            holder.posterid.setText("POSTER ID: " + model.getPosterUid());
            holder.total.setText("TOTAL VIEWS: " + model.getTotalSubscribesQuantity());
            holder.current.setText("CURRENT VIEWS: " + model.getCurrentSubscribesQuantity());
            holder.completed.setText("COMPLETED: " + model.getCompletedDate());

            holder.editBtn.setOnClickListener(view -> {
                showDialog(model, "Save", false);
            });

            holder.deleteBtn.setOnClickListener(view -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Are you sure?")
                        .setMessage("Do you really want to delete this task?")
                        .setPositiveButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setNegativeButton("Yes", (dialogInterface, i) ->
                                Utils.databaseReference().child("subscribe_tasks").child(model.getTaskKey())
                                        .removeValue())
                        .show();
            });

            holder.viewBtn.setOnClickListener(view -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(model.getVideoUrl()));
                startActivity(i);
            });

        }

        @Override
        public int getItemCount() {
            if (viewsModelArrayList == null)
                return 0;
            return viewsModelArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            TextView posterid, total, current, completed;
            ImageView editBtn, deleteBtn, viewBtn, imageView;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                imageView = v.findViewById(R.id.image);
                posterid = v.findViewById(R.id.poster_id_tv_task);
                total = v.findViewById(R.id.total_tv_task);
                current = v.findViewById(R.id.current_tv_task);
                editBtn = v.findViewById(R.id.editBtn_task);
                deleteBtn = v.findViewById(R.id.deleteBtn_task);
                viewBtn = v.findViewById(R.id.viewBtn);
                completed = v.findViewById(R.id.completed_tv_task);

            }
        }

    }

}
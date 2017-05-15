package com.nikhilbawane.dnbs;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.NoticeViewHolder> {

    List<Notice> notices;
    String role;
    Context context;
    View view;

    public static class NoticeViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        int id = -1;
        TextView noticeTitle;
        TextView noticeDesc;
        TextView noticeTag;
        TextView noticeDate;
        RatingBar noticePriority;
        ImageView noticeDelete;

        NoticeViewHolder(View itemView, final String userRole) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            noticeTitle = (TextView) itemView.findViewById(R.id.notice_title);
            noticeDesc = (TextView) itemView.findViewById(R.id.notice_desc);
            noticeTag = (TextView) itemView.findViewById(R.id.notice_tag);
            noticeDate = (TextView) itemView.findViewById(R.id.notice_date);
            noticePriority = (RatingBar) itemView.findViewById(R.id.notice_priority);
            noticeDelete = (ImageView) itemView.findViewById(R.id.notice_delete);

            cv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (noticeDesc.getVisibility() == View.VISIBLE) {
                        //noticeDesc.setVisibility(View.GONE);
                        collapse(noticeDesc);
                    }

                    else {
                        //noticeDesc.setVisibility(View.VISIBLE);
                        expand(noticeDesc);
                    }
                }
            });
        }
    }

    public RVAdapter(List<Notice> notices, View view, Context context, String role){
        this.notices = notices;
        this.view = view;
        this.context = context;
        this.role = role;
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    @Override
    public NoticeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_notice, viewGroup, false);
        NoticeViewHolder pvh = new NoticeViewHolder(v, role);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final NoticeViewHolder noticeViewHolder, int i) {
        noticeViewHolder.id = notices.get(i).id;
        noticeViewHolder.noticeTitle.setText(notices.get(i).title);
        noticeViewHolder.noticeDesc.setText(notices.get(i).description);
        noticeViewHolder.noticeDate.setText(notices.get(i).date);
        noticeViewHolder.noticePriority.setRating(notices.get(i).priority);

        // Set tag to D if the Notice is not a Circular
        if(notices.get(i).tag.equals("U")) {
            noticeViewHolder.noticeTag.setText(notices.get(i).tag);
        }
        else {
            noticeViewHolder.noticeTag.setText("D");
        }

        if(!role.equals("administrator")) {
            noticeViewHolder.noticeDelete.setVisibility(View.GONE);
            noticeViewHolder.noticeDelete.setEnabled(false);
            noticeViewHolder.noticeDelete.invalidate();
        }

        noticeViewHolder.noticeDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Home().deleteNotice(view, noticeViewHolder.id, context, role);
            }
        });

        // Hides the description by default and when notice is scrolled out of view
        noticeViewHolder.noticeDesc.setVisibility(View.GONE);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

}

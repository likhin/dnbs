/*
 * Copyright (C) 2016 Nikhil Bawane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nikhilbawane.dnbs;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nikhil on 28/6/15.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.NoticeViewHolder> {

    List<Notice> notices;

    public static class NoticeViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView noticeTitle;
        TextView noticeDesc;
        TextView noticeTag;
        TextView noticeDate;
        RatingBar noticePriority;

        NoticeViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            noticeTitle = (TextView) itemView.findViewById(R.id.notice_title);
            noticeDesc = (TextView) itemView.findViewById(R.id.notice_desc);
            noticeTag = (TextView) itemView.findViewById(R.id.notice_tag);
            noticeDate = (TextView) itemView.findViewById(R.id.notice_date);
            noticePriority = (RatingBar) itemView.findViewById(R.id.notice_priority);

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

    public RVAdapter(List<Notice> notices){
        this.notices = notices;
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    @Override
    public NoticeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_notice, viewGroup, false);
        NoticeViewHolder pvh = new NoticeViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(NoticeViewHolder noticeViewHolder, int i) {
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

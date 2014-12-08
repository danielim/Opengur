package com.kenny.openimgur.adapters;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kenny.openimgur.R;
import com.kenny.openimgur.classes.CustomLinkMovement;
import com.kenny.openimgur.classes.ImgurComment;
import com.kenny.openimgur.classes.ImgurListener;
import com.kenny.openimgur.ui.TextViewRoboto;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends ImgurBaseAdapter {
    private LayoutInflater mInflater;

    private ImgurListener mListener;

    private int mSelectedIndex = -1;

    private String mOP;

    public CommentAdapter(Context context, List<ImgurComment> comments, ImgurListener listener) {
        super(context, comments, false);
        mInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    @Override
    protected DisplayImageOptions getDisplayOptions() {
        return null;
    }

    @Override
    public ArrayList<ImgurComment> retainItems() {
        return new ArrayList<ImgurComment>(getAllItems());
    }

    /**
     * Removes all items from list and ImgurListener is removed
     */
    public void destroy() {
        clear();
        mListener = null;
    }

    @Override
    public ImgurComment getItem(int position) {
        return (ImgurComment) super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommentViewHolder holder;
        final ImgurComment comment = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.comment_item, parent, false);
            holder = new CommentViewHolder(convertView);
            holder.replies.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onViewRepliesTap(view);
                    }
                }
            });

            holder.comment.setMovementMethod(CustomLinkMovement.getInstance(mListener));
        } else {
            holder = (CommentViewHolder) convertView.getTag();
        }

        holder.comment.setText(comment.getComment());
        holder.author.setText(constructSpan(comment, holder.author.getContext()));
        Linkify.addLinks(holder.comment, Linkify.WEB_URLS);

        if (comment.getReplyCount() <= 0) {
            holder.replies.setVisibility(View.GONE);
        } else {
            holder.replies.setVisibility(View.VISIBLE);
            holder.replies.setText(convertView.getContext().getString(R.string.comment_replies, comment.getReplyCount()));
        }

        convertView.setBackgroundColor(position == mSelectedIndex ?
                convertView.getResources().getColor(R.color.comment_bg_selected) :
                convertView.getResources().getColor(R.color.comment_bg_default));

        return convertView;
    }

    /**
     * Sets the currently selected item. If the item selected is the one that is already selected, it is deselected
     *
     * @param index
     * @return If the selected item was already selected
     */
    public boolean setSelectedIndex(int index) {
        boolean wasSelected = mSelectedIndex == index;
        mSelectedIndex = wasSelected ? -1 : index;
        notifyDataSetChanged();

        return wasSelected;
    }

    /**
     * Creates the spannable object for the authors name, points, and time
     *
     * @param comment
     * @param context
     * @return
     */
    private Spannable constructSpan(ImgurComment comment, Context context) {
        CharSequence date = getDateFormattedTime(comment.getDate() * 1000L, context);
        String author = comment.getAuthor();
        StringBuilder sb = new StringBuilder(author);
        boolean isOp = isOP(author);
        int spanLength = author.length();

        if (isOp) {
            // TODO Other languages for OP?
            sb.append(" OP");
            spanLength += 3;
        }

        sb.append(" ").append(comment.getPoints()).append(" ").append(context.getString(R.string.points))
                .append(" : ").append(date);
        Spannable span = new SpannableString(sb.toString());

        int color = context.getResources().getColor(R.color.notoriety_positive);
        if (comment.getPoints() < 0) {
            color = context.getResources().getColor(R.color.notoriety_negative);
        }

        if (isOp) {
            span.setSpan(new ForegroundColorSpan(color), author.length() + 1, spanLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        span.setSpan(new ForegroundColorSpan(color), spanLength, sb.length() - date.length() - 2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return span;
    }

    private CharSequence getDateFormattedTime(long commentDate, Context context) {
        long now = System.currentTimeMillis();
        long difference = System.currentTimeMillis() - commentDate;

        return (difference >= 0 && difference <= DateUtils.MINUTE_IN_MILLIS) ?
                context.getResources().getString(R.string.moments_ago) :
                DateUtils.getRelativeTimeSpanString(
                        commentDate,
                        now,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE);
    }

    public void setOP(String op) {
        mOP = op;
    }

    private boolean isOP(String user) {
        if (!TextUtils.isEmpty(mOP)) {
            return mOP.equals(user);
        }

        return false;
    }

    private static class CommentViewHolder {
        TextViewRoboto author, comment;

        Button replies;

        public CommentViewHolder(View view) {
            comment = (TextViewRoboto) view.findViewById(R.id.comment);
            author = (TextViewRoboto) view.findViewById(R.id.author);
            replies = (Button) view.findViewById(R.id.replies);
            view.setTag(this);
        }
    }
}

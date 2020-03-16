package eir220.cse216.lehigh.edu.phase1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mIndex;
        TextView mText;
        TextView upVotes;
        Button mlike;
        Button mdislike;

        ViewHolder(View itemView) {
            super(itemView);
            this.upVotes = (TextView) itemView.findViewById(eir220.cse216.lehigh.edu.phase1.R.id.numLikes);
            this.mText = (TextView) itemView.findViewById(eir220.cse216.lehigh.edu.phase1.R.id.listItemText);
            this.mlike = (Button) itemView.findViewById(eir220.cse216.lehigh.edu.phase1.R.id.upVote);
            this.mdislike = (Button) itemView.findViewById(eir220.cse216.lehigh.edu.phase1.R.id.downVote);

        }
    }

    private ArrayList<Datum> mData;
    private LayoutInflater mLayoutInflater;

    ItemListAdapter(Context context, ArrayList<Datum> data) {
        mData = data;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(eir220.cse216.lehigh.edu.phase1.R.layout.list_items, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Datum d = mData.get(position);
        holder.upVotes.setText("Likes: " + Integer.toString(d.getUpVotes()));
        holder.mText.setText(d.mText);

        // Attach a click listener to the view we are configuring
        final View.OnClickListener listener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mClickListener.onClick(d, view);
            }
        };
        holder.upVotes.setOnClickListener(listener);
        holder.mText.setOnClickListener(listener);
        holder.mlike.setOnClickListener(listener);
        holder.mdislike.setOnClickListener(listener);
    }

    interface ClickListener{
        void onClick(Datum d, View v);
    }


    private ClickListener mClickListener;
    ClickListener getClickListener() {return mClickListener;}
    void setClickListener(ClickListener c) { mClickListener = c;}



}
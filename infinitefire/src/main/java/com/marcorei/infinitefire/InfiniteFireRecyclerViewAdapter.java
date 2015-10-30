package com.marcorei.infinitefire;


import android.support.v7.widget.RecyclerView;

/**
 * This is an abstract Adapter on which you can base your own.
 * It supports some convenience code but does neither marshal your data into types nor does it populate your view holders.
 */
public abstract class InfiniteFireRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    protected InfiniteFireArray snapshots;
    protected int indexOffset;
    protected int indexAppendix;

    /**
     * Use this constructor if you got either multiple FirebaseInfiniteArrays that you want to merge into one RecyclerView or if you want to add custom headers or footer which will also get their data from firebase.
     * This constructor does NOT add Listeners to anything, so you have to do that yourself.
     *
     * @param numHeaders number of headers this RecyclerView adds to its length.
     * @param numFooters number of footers this RecyclerView adds to its length.
     */
    public InfiniteFireRecyclerViewAdapter(
            final int numHeaders,
            final int numFooters
    ) {
        this.indexOffset = numHeaders;
        this.indexAppendix = numFooters;
    }

    /**
     * Use this constructor if you intent to use only one FirebaseInfinite with this Adapter.
     * A listener will be added to snapshots.
     *
     * @param snapshots the single InfiniteFireArray from which tis adapter will get its data. If you want to use multiple Arrays, please use InfiniteFireRecyclerViewAdapter(numHeaders, numFooters)
     * @param numHeaders number of headers this RecyclerView adds to its length.
     * @param numFooters number of footers this RecyclerView adds to its length.
     */
    public InfiniteFireRecyclerViewAdapter(
            InfiniteFireArray snapshots,
            final int numHeaders,
            final int numFooters) {
        this.snapshots = snapshots;
        this.indexOffset = numHeaders;
        this.indexAppendix = numFooters;

        this.snapshots.setOnChangedListener(new InfiniteFireArray.OnChangedListener() {
            @Override
            public void onChanged(InfiniteFireArray.OnChangedListener.EventType type, int index, int oldIndex) {
                switch(type) {
                    case Added:
                        notifyItemInserted(index + indexOffset);
                        break;
                    case Changed:
                        notifyItemChanged(index + indexOffset);
                        break;
                    case Removed:
                        notifyItemRemoved(index + indexOffset);
                        break;
                    case Moved:
                        notifyItemMoved(oldIndex + indexOffset, index + indexOffset);
                        break;
                    case Reset:
                        notifyDataSetChanged();
                        break;
                    default:
                        throw new IllegalStateException("Incomplete case statement");
                }
            }
        });
    }

    public void clear() {
        snapshots.setOnChangedListener(null);
        snapshots = null;
    }

    @Override
    public int getItemCount() {
        return snapshots.getCount() + indexOffset + indexAppendix;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}


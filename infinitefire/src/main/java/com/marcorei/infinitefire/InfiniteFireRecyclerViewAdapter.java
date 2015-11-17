/* (c) 2015 Markus Riegel
 * license: MIT
 */
package com.marcorei.infinitefire;

import android.support.v7.widget.RecyclerView;

/**
 * This is an abstract {@link android.support.v7.widget.RecyclerView.Adapter Adapter} on which you can base your own.
 */
public abstract class InfiniteFireRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    protected InfiniteFireArray<?> snapshots;
    protected int indexOffset;
    protected int indexAppendix;

    /**
     * Use this constructor if you got either multiple InfiniteFireArrays that you want to merge into one RecyclerView or if you want to add custom headers or footers.
     * This constructor does not add Listeners to anything, so you have to do that yourself.
     *
     * @param numHeaders Number of headers this RecyclerView adds to its length.
     * @param numFooters Number of footers this RecyclerView adds to its length.
     */
    public InfiniteFireRecyclerViewAdapter(
            final int numHeaders,
            final int numFooters
    ) {
        this.indexOffset = numHeaders;
        this.indexAppendix = numFooters;
    }

    /**
     * Use this constructor if you intent to use only one InfiniteFireArray with this Adapter.
     * A {@link com.marcorei.infinitefire.InfiniteFireArray.OnChangedListener OnChangeListener} will be added to snapshots.
     *
     * @param snapshots The single InfiniteFireArray from which this Adapter will get its data. If you want to use multiple Arrays, use {@link InfiniteFireRecyclerViewAdapter#InfiniteFireRecyclerViewAdapter(int, int)}.
     * @param numHeaders Number of headers this Adapter adds to its length.
     * @param numFooters Number of footers this Adapter adds to its length.
     */
    public InfiniteFireRecyclerViewAdapter(
            InfiniteFireArray<?> snapshots,
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

    /**
     * Removes the Listener.
     */
    public void clear() {
        snapshots.setOnChangedListener(null);
        snapshots = null;
    }

    /**
     * @return Get the item count including headers and footers.
     */
    @Override
    public int getItemCount() {
        return snapshots.getCount() + indexOffset + indexAppendix;
    }

    /**
     * @param position Position of the item.
     * @return Position of the item because there are headers and footers for which we can't get a proper hash code.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }
}


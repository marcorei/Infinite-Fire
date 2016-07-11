package com.marcorei.infinitefiredemo.ui.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.marcorei.infinitefire.InfiniteFireArray;
import com.marcorei.infinitefire.InfiniteFireRecyclerViewAdapter;
import com.marcorei.infinitefiredemo.R;
import com.marcorei.infinitefiredemo.model.Chat;

/**
 * This is an example how to implement a recycler view that uses a InfiniteFireArray as data source.
 * The list shows items as simple text messages in a linear layout.
 *
 * The InfiniteFireArray is set to reverse order so that new messages appear at the bottom.
 * Also, it is set to not keep the order.
 * This example is very similar to what you would want in a chat.
 */
public class InfiniteRecyclerViewLinearActivity extends AppCompatActivity{

    public static final String TAG = InfiniteRecyclerViewGridActivity.class.getSimpleName();

    InfiniteFireArray<Chat> array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_linear);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("chat");


        // setup for the text input area

        final String name = "InfiniteFire User";
        final Button buttonSend = (Button) findViewById(R.id.button_send);
        final EditText editText = (EditText) findViewById(R.id.edit_text);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Chat chat = new Chat(name, editText.getText().toString());
                buttonSend.setEnabled(false);
                ref.push().setValue(chat, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebaseError != null) {
                            Log.d(TAG, "firebase error", firebaseError.toException());
                        }
                        buttonSend.setEnabled(true);
                    }
                });
                editText.setText("");
            }
        });


        // setup for the firebase array, wiring to the adapter and view

        final Query query = ref.orderByKey();
        array = new InfiniteFireArray<>(
                Chat.class,
                query,
                20,
                20,
                false,
                false
        );
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(array);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(false);

        array.addOnLoadingStatusListener(new InfiniteFireArray.OnLoadingStatusListener() {
            @Override
            public void onChanged(EventType type) {
                switch(type) {
                    case LoadingContent:
                        adapter.setInitiallyLoading(false);
                        adapter.setLoadingMore(true);
                        break;
                    case LoadingNoContent:
                        adapter.setInitiallyLoading(true);
                        adapter.setLoadingMore(false);
                        break;
                    case Done:
                        adapter.setInitiallyLoading(false);
                        adapter.setLoadingMore(false);
                        break;
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0) {
                    return;
                }
                if(layoutManager.findLastVisibleItemPosition() < array.getCount() - 20) {
                    return;
                }
                array.more();
            }
        });
    }

    @Override
    protected void onDestroy() {
        array.cleanup();
        super.onDestroy();
    }

    /**
     * The adapter in this example holds the logic how to convert snapshots into pojos.
     * It also inflates the layout resources.
     */
    public static class RecyclerViewAdapter extends InfiniteFireRecyclerViewAdapter<Chat> {

        public static final int VIEW_TYPE_HEADER = 0;
        public static final int VIEW_TYPE_CONTENT = 1;
        public static final int VIEW_TYPE_FOOTER = 2;

        /**
         * This is the view holder for the chat messages.
         */
        public static class ChatHolder extends RecyclerView.ViewHolder {
            public TextView nameView, textView;

            public ChatHolder(View itemView) {
                super(itemView);
                nameView = (TextView) itemView.findViewById(android.R.id.text2);
                textView = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }

        /**
         * This is the view holder for the simple header and footer of this example.
         */
        public static class LoadingHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            }
        }

        private boolean initiallyLoading = true;
        private boolean loadingMore = false;

        /**
         * @param snapshots data source for this adapter.
         */
        public RecyclerViewAdapter(InfiniteFireArray snapshots) {
            super(snapshots, 1, 1);
        }

        /**
         * @return initial loading status.
         */
        public boolean isInitiallyLoading() {
            return initiallyLoading;
        }

        /**
         * This loading status has nothing to do with firebase real-time functionality.
         * It reflects the loading procedure of the first "fresh" data set after a change to the query.
         *
         * @param initiallyLoading adjust the status the initial loading procedure.
         */
        public void setInitiallyLoading(boolean initiallyLoading) {
            if(initiallyLoading == this.initiallyLoading) return;
            if(initiallyLoading && this.isLoadingMore()) {
                this.setLoadingMore(false);
            }
            this.initiallyLoading = initiallyLoading;
            notifyItemChanged(0);
        }

        /**
         * @return status of load-more loading procedures
         */
        public boolean isLoadingMore() {
            return loadingMore;
        }

        /**
         * This loading status has nothing to do with firebase real-time functionality.
         * It reflects the loading procedure of the first "fresh" data set after a change to the query.
         *
         * @param loadingMore adjust the status of additional loading procedures.
         */
        public void setLoadingMore(boolean loadingMore) {
            if(loadingMore == this.isLoadingMore()) return;
            if(loadingMore && this.isInitiallyLoading()) {
                this.setInitiallyLoading(false);
            }
            this.loadingMore = loadingMore;
            notifyItemChanged(getItemCount() - 1);
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0) {
                return VIEW_TYPE_HEADER;
            }
            else if(position == getItemCount() - 1) {
                return VIEW_TYPE_FOOTER;
            }
            return VIEW_TYPE_CONTENT;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            View view;
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_CONTENT:
                    view = inflater.inflate(android.R.layout.two_line_list_item, parent, false);
                    viewHolder = new ChatHolder(view);
                break;
                case VIEW_TYPE_FOOTER:
                case VIEW_TYPE_HEADER:
                    view = inflater.inflate(R.layout.list_item_loading, parent, false);
                    viewHolder = new LoadingHolder(view);
                    break;
                default: throw new IllegalArgumentException("Unknown type");
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            switch(viewType) {
                case VIEW_TYPE_CONTENT:
                    Chat chat = snapshots.getItem(position - indexOffset).getValue();
                    if(chat == null) {
                        chat = new Chat("N/A","N/A");
                    }
                    ChatHolder contentHolder = (ChatHolder) holder;
                    contentHolder.nameView.setText(chat.getName());
                    contentHolder.textView.setText(chat.getText());
                    break;
                case VIEW_TYPE_HEADER:
                    LoadingHolder headerHolder = (LoadingHolder) holder;
                    headerHolder.progressBar.setVisibility((isInitiallyLoading()) ? View.VISIBLE : View.GONE);
                    break;
                case VIEW_TYPE_FOOTER:
                    LoadingHolder footerHolder = (LoadingHolder) holder;
                    footerHolder.progressBar.setVisibility((isLoadingMore()) ? View.VISIBLE : View.GONE);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type");
            }
        }
    }
}

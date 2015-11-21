package com.marcorei.infinitefiredemo.ui.activity;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.Query;
import com.marcorei.infinitefire.InfiniteFireArray;
import com.marcorei.infinitefire.InfiniteFireRecyclerViewAdapter;
import com.marcorei.infinitefiredemo.R;
import com.marcorei.infinitefiredemo.model.Chat;

/**
 * This is an example how to implement a recycler view that uses a InfiniteFireArray as data source.
 * The grid shows the fist letter of the chat message.
 * You could use this for an image gallery for example.
 *
 * The InfiniteFireArray is set to reverse order to show newest letters at the top.
 * This one is set to maintain the order.
 * That means, new messages will not spawn at the top but existing messages will update.
 * Pull-to-refresh shows the new messages.
 */
public class InfiniteRecyclerViewGridActivity extends AppCompatActivity {

    public static final String TAG = InfiniteRecyclerViewGridActivity.class.getSimpleName();

    InfiniteFireArray<Chat> array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_grid);

        final Firebase ref = new Firebase(getResources().getString(R.string.firebase_base_url));


        // setup for the text input area

        final String name = "InfiniteFire User";
        final Button buttonSend = (Button) findViewById(R.id.button_send);
        final EditText editText = (EditText) findViewById(R.id.edit_text);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Chat chat = new Chat(name, editText.getText().toString());
                buttonSend.setEnabled(false);
                ref.push().setValue(chat, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
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
                18,
                18,
                false,
                true
        );
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                array.reset();
            }
        });
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(array);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setReverseLayout(false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getSpanSize(position);
            }
        });
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(false);

        array.addOnLoadingStatusListener(new InfiniteFireArray.OnLoadingStatusListener() {
            @Override
            public void onChanged(EventType type) {
                switch (type) {
                    case LoadingContent:
                        adapter.setLoadingMore(true);
                        break;
                    case LoadingNoContent:
                        adapter.setLoadingMore(false);
                        break;
                    case Done:
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoadingMore(false);
                        break;
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy < 0) {
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

        public static final int VIEW_TYPE_CONTENT = 1;
        public static final int VIEW_TYPE_FOOTER = 2;

        /**
         * This is the view holder for the chat messages.
         */
        public static class LetterHolder extends RecyclerView.ViewHolder {
            public TextView textView;

            public LetterHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.text);
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

        private boolean loadingMore = false;

        /**
         * @param snapshots data source for this adapter.
         */
        public RecyclerViewAdapter(InfiniteFireArray snapshots) {
            super(snapshots, 0, 1);
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
            this.loadingMore = loadingMore;
            notifyItemChanged(getItemCount() - 1);
        }

        @Override
        public int getItemViewType(int position) {
            if(position == getItemCount() - 1) {
                return VIEW_TYPE_FOOTER;
            }
            return VIEW_TYPE_CONTENT;
        }

        public int getSpanSize(int position) {
            if(position == getItemCount() - 1) {
                return 3;
            }
            return 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            View view;
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_CONTENT:
                    view = inflater.inflate(R.layout.list_item_letter, parent, false);
                    viewHolder = new LetterHolder(view);
                    break;
                case VIEW_TYPE_FOOTER:
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
                        chat = new Chat("-","-");
                    }
                    LetterHolder contentHolder = (LetterHolder) holder;
                    String text = chat.getText();
                    if(text.length() == 0) {
                        text = "-";
                    }
                    text = text.substring(0,1);
                    if(text.equals(" ")) {
                        text = "-";
                    }
                    contentHolder.textView.setText(text);
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

package com.jjq.mytoolbar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjq.mytoolbar.bean.Model;
import com.jjq.mytoolbar.view.DividerItemDecoration;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.recycler)
    RecyclerView recyclerView;
    @Bind(R.id.layout_toolbar_my_container)
    RelativeLayout layoutToolBarBackground;
    @Bind(R.id.text_toolbar_index)
    TextView centerText;

    private ArrayList<Model> modelList = new ArrayList<>();
    private MyRecyclerAdapter adapter;
    private LinearLayoutManager layoutManager;
    private int itemIndex;
    private ToolBarBackgroundController toolBarBackgroundController;
    private int anchorHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTranslucentWindows(this);
        ButterKnife.bind(this);
        layoutManager = new LinearLayoutManager(this.getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST,
                R.drawable.devide_line_gray, 0));
        initHead();
        initData();
        initView();
    }

    private void initHead() {
        layoutToolBarBackground.setBackgroundColor(Color.TRANSPARENT);
        toolBarBackgroundController = new ToolBarBackgroundController(layoutToolBarBackground);
    }

    public class ToolBarBackgroundController {

        private View layoutToolbar;

        public ToolBarBackgroundController(View layoutToolbar) {
            this.layoutToolbar = layoutToolbar;
            layoutToolbar.setBackgroundColor(Color.TRANSPARENT);
        }

        public void setTransparent(boolean needTransparent) {
            if (needTransparent) {
                //变透明
                centerText.setVisibility(View.GONE);
            } else {
                layoutToolbar.setBackgroundColor(getResources().getColor(R.color.base));
                centerText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setTranslucentWindows(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        } else return 0;
    }

    private void initData() {
        for (int i = 0; i < 20; i++) {
            Model model = new Model();
            model.setName("jjq" + i);
            model.setDesc("哈哈哈哈哈哈哈哈");
            modelList.add(model);
        }
    }

    private void initView() {
        if (adapter == null) {
            adapter = new MyRecyclerAdapter();
        } else {
            adapter.notifyDataSetChanged();
        }
        adapter.initData(false);
        adapter.appendData(modelList);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new OnScrollColorChangeListener());
    }

    private class OnScrollColorChangeListener extends RecyclerView.OnScrollListener {

        private boolean isTrans = true;
        private int y = 0;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (anchorHeight != 0) {
                y += dy;
                boolean needTrans = y <= anchorHeight;
                if (needTrans != isTrans) {
                    isTrans = needTrans;
                    toolBarBackgroundController.setTransparent(needTrans);
                } else {
                    if (y / anchorHeight < 1) {
                        layoutToolBarBackground.setBackgroundColor(getResources().getColor(R.color.base));
                        layoutToolBarBackground.getBackground().setAlpha((int) ((float) y / anchorHeight * 255));
                    }
                }
            }
        }
    }

    private class MyRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int TYPE_HEADER = 0x1000;
        private final int TYPE_NORMAL = 0x2000;
        private final int TYPE_FOOTER = 0x3000;
        private final int TYPE_EMPTY = 0x4000;
        private final int TYPE_THEME = 0x5000;
        private ArrayList<MyItemInfo> itemInfos;
        private boolean needFooter = false;
        private boolean hasFooter = false;

        public class MyItemInfo {
            int type;
            Model model;

            public MyItemInfo(int type, Model model) {
                this.type = type;
                this.model = model;
            }
        }

        public MyRecyclerAdapter() {
            itemInfos = new ArrayList<>();
        }

        public void initData(boolean needFooter) {
            this.needFooter = needFooter;
            this.hasFooter = false;
            int oldCount = itemInfos.size();
            itemInfos.clear();
            this.notifyItemRangeRemoved(0, oldCount);
            itemInfos.add(new MyItemInfo(TYPE_HEADER, null));
            //itemInfos.add(new MyItemInfo(TYPE_FOOTER, null));
            //this.notifyItemRangeInserted(0, 2);
        }

        public void appendData(ArrayList<Model> models) {
            int oldCount = itemInfos.size();
            if (hasFooter) {
                itemInfos.remove(oldCount - 1);
                this.notifyItemRemoved(oldCount - 1);
                oldCount--;
            }
            int size = models.size();
            for (int i = 0; i < size; i++) {
                itemInfos.add(new MyItemInfo(TYPE_NORMAL, models.get(i)));
            }

            this.notifyItemRangeInserted(oldCount + 1, size);
            if (needFooter) {
                itemInfos.add(new MyItemInfo(TYPE_FOOTER, null));
                this.notifyItemInserted(itemInfos.size() - 1);
                hasFooter = true;
            }
        }

        public void removeFooter() {
            int oldCount = itemInfos.size();
            itemInfos.remove(oldCount - 1);
            notifyItemRemoved(oldCount - 1);
        }

        public void appendEmptyView() {
            int oldCount = itemInfos.size();
            if (hasFooter) {
                itemInfos.remove(oldCount - 1);
                this.notifyItemRemoved(oldCount - 1);
                oldCount--;
            }
            itemInfos.add(new MyItemInfo(TYPE_EMPTY, null));
            notifyItemRangeInserted(oldCount, 1);
        }

        @Override
        public int getItemViewType(int position) {
            return itemInfos.get(position).type;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = null;
            switch (viewType) {
                case TYPE_HEADER:
                    view = inflater.inflate(R.layout.layout_main_recycler_head, parent, false);
                    return new MyHeaderItemHolder(view, MainActivity.this);
                case TYPE_NORMAL:
                    view = inflater.inflate(R.layout.layout_list_item, parent, false);
                    return new NormalViewHolder(view);
                case TYPE_EMPTY:
                    return null;
                case TYPE_FOOTER:
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            switch (viewHolder.getItemViewType()) {
                case TYPE_NORMAL:
                    NormalViewHolder normalViewHolder = (NormalViewHolder) viewHolder;
                    normalViewHolder.setContent(itemInfos.get(i).model, i);
                    break;
                case TYPE_HEADER:
                    MyHeaderItemHolder headerViewHolder = (MyHeaderItemHolder) viewHolder;
                    headerViewHolder.setContent();
                    break;
                case TYPE_FOOTER:
                case TYPE_EMPTY:
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return itemInfos.size();
        }

        private class EmptyItemHolder extends RecyclerView.ViewHolder {
            public EmptyItemHolder(View itemView) {
                super(itemView);
            }
        }

        private class MyHeaderItemHolder extends RecyclerView.ViewHolder {
            private Context context;
            private ImageView imageView;

            public MyHeaderItemHolder(View itemView, Context context) {
                super(itemView);
                this.context = context;
                imageView = (ImageView) itemView.findViewById(R.id.img_main_recycler_head_banner);
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        anchorHeight = imageView.getMeasuredHeight() - layoutToolBarBackground.getMeasuredHeight();
                    }
                });
            }

            //填充头部内容
            public void setContent() {

            }
        }

        private class NormalViewHolder extends RecyclerView.ViewHolder {
            private Model model;
            private TextView nameView;
            private TextView descView;

            public NormalViewHolder(View itemView) {
                super(itemView);
                nameView = (TextView) itemView.findViewById(R.id.text_list_item_name);
                descView = (TextView) itemView.findViewById(R.id.text_list_item_desc);
                itemView.setOnClickListener(new OnItemClickListener());
            }

            public void setContent(Model model, int index) {
                this.model = model;
                nameView.setText(model.getName());
                descView.setText(model.getDesc());
                itemIndex = index;

            }

            private class OnItemClickListener implements View.OnClickListener {
                @Override
                public void onClick(View v) {

                }
            }
        }

        private class FooterViewHolder extends RecyclerView.ViewHolder {

            public FooterViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

}

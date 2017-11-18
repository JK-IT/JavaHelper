package jkit.looptime;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public class LoopFragController extends Fragment {
    private static final String TAG = "LoopFragment";
    private RecyclerView mRecyclerView;

    public LoopFragController() {
        // Required empty public constructor
    }


    public static LoopFragController newInstance() {
        return new LoopFragController();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void setupAdapter() {
        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);
        List<ResolveInfo> chosenActivities = new Vector<ResolveInfo>();
        Collections.sort(activities, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo resolveInfo, ResolveInfo t1) {
                PackageManager pm = getActivity().getPackageManager();
                return String.CASE_INSENSITIVE_ORDER.compare(resolveInfo.loadLabel(pm).toString(), t1.loadLabel(pm).toString());
            }
        });
        for (int i = 0; i < activities.size(); i++)
        {
            ResolveInfo ri = activities.get(i);
            if(ri.loadLabel(pm).toString().equals("Phone") || ri.loadLabel(pm).toString().equals("Messaging") || ri.loadLabel(pm).toString().equals("Messenger") || ri.loadLabel(pm).toString().equals("Settings") || ri.loadLabel(pm).toString().equals("Clock"))
            {
                chosenActivities.add(ri);
                Log.d(TAG, chosenActivities.size() + " size ");
            }

        }
        mRecyclerView.setAdapter(new ActivityAdapter(chosenActivities));
    }
    private class ActivityHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private ResolveInfo mResolveInfo;
        private TextView mNameTextView;
        private ImageView mImageView;
        public ActivityHolder(LayoutInflater inflater, ViewGroup parent)
        {
            super(inflater.inflate(R.layout.list_item, parent, false));
            //mNameTextView.setOnClickListener(this);
            mNameTextView = (TextView)itemView.findViewById(R.id.textView);
            mImageView = (ImageView)itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        public void bindActivity(ResolveInfo resolveInfo)
        {
            mResolveInfo = resolveInfo;
            PackageManager pm = getActivity().getPackageManager();
            String appName = mResolveInfo.loadLabel(pm).toString();
            mNameTextView.setText(appName);
            Drawable icon = mResolveInfo.loadIcon(pm);
            mImageView.setImageDrawable(icon);
        }

        @Override
        public void onClick(View view)
        {
            ActivityInfo activityInfo = mResolveInfo.activityInfo;
            Intent i = new Intent(Intent.ACTION_MAIN).setClassName(activityInfo.applicationInfo.packageName, activityInfo.name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder>
    {
        private  final List<ResolveInfo> mActivities;
        public ActivityAdapter(List<ResolveInfo> activities)
        {
            mActivities = activities;
        }
        @Override
        public ActivityHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            //View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ActivityHolder(layoutInflater, parent);
        }
        @Override
        public void onBindViewHolder(ActivityHolder holder, int position)
        {
            ResolveInfo resolveInfo = mActivities.get(position);
            holder.bindActivity(resolveInfo);
        }
        @Override
        public int getItemCount()
        {
            return mActivities.size();
        }

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.loop_fragment, container, false);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.app_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setupAdapter();
        return v;
    }



}

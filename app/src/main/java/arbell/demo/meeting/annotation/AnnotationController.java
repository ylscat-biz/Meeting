package arbell.demo.meeting.annotation;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import arbell.demo.meeting.R;

/**
 * Created at 2015/10/27.
 *
 * @author YinLanShan
 */
public class AnnotationController implements View.OnClickListener {
    private AnnotationAdapter mAnotAdatper;
    private LocalAnnotationAdapter mLocalAdapter;
    private ListView mListView;
    private View mRemote, mLocal;

    public AnnotationController(View panel) {
        mListView = (ListView)panel.findViewById(R.id.annotation_list);
        mRemote = panel.findViewById(R.id.annotation_share);
        mLocal = panel.findViewById(R.id.annotation_local);

        LayoutInflater inflater = LayoutInflater.from(panel.getContext());
        mAnotAdatper = new AnnotationAdapter(inflater);
        mLocalAdapter = new LocalAnnotationAdapter(inflater);

        mRemote.setOnClickListener(this);
        mLocal.setOnClickListener(this);
        onClick(mRemote);
    }

    public void refresh() {
        mAnotAdatper.refresh();
        mLocalAdapter.refresh();
    }

    @Override
    public void onClick(View v) {
        if(v == mRemote) {
            mListView.setAdapter(mAnotAdatper);
            mLocal.setSelected(false);
            v.setSelected(true);
        }
        else {
            mListView.setAdapter(mLocalAdapter);
            mRemote.setSelected(false);
            v.setSelected(true);
        }
    }
}

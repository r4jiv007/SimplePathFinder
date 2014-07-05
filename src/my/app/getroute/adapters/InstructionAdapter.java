package my.app.getroute.adapters;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.my.getroute.R;

public class InstructionAdapter extends BaseAdapter {

	private List<String> mInstructionList;
	private Context mContext;

	public InstructionAdapter(List<String> list, Context context) {
		mInstructionList = list;
		mContext = context;
	}

	@Override
	public int getCount() {
		return mInstructionList.size();
	}

	@Override
	public Object getItem(int position) {
		return mInstructionList.get(position);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int arg0, View view, ViewGroup arg2) {
		LayoutInflater mInfalter = LayoutInflater.from(mContext);
		view = mInfalter.inflate(R.layout.instruction_list_item, null);
		TextView mTextView = (TextView) view.findViewById(R.id.tvInstruction);
		mTextView.setText(Html.fromHtml(mInstructionList.get(arg0).trim()));
		return view;
	}
}

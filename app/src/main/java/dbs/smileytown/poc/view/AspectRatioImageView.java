package dbs.smileytown.poc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AspectRatioImageView extends ImageView {

	public AspectRatioImageView(Context context) {
		super(context);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		try {
			int iv_width, iv_height;

				iv_width =  MeasureSpec.getSize(widthMeasureSpec);
				iv_height = iv_width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();


			setMeasuredDimension(iv_width, iv_height);
		
		} catch (Exception e) {
			setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		}
	}

	@Override
	public boolean isInEditMode() {
		return true;
	} 
	
}
